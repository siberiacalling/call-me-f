import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.util.HashMap;

public class StationServer
{
    public static void main(String args[])
    {
        try
        {
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            StationServant servant = new StationServant();

            // Получение объектной ссылки на сервант
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            Station sref = StationHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Связывание объектной ссылки с именем
            String name = "BaseStation";
            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, sref);

            System.out.println("Station server is ready and waiting for messages ...");

            // Ожидание обращений от клиентов (трубок)
            orb.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

// Класс, реализующий IDL-интерфейс базовой станции
class StationServant extends StationPOA
{
    private HashMap<String, TubeCallback> hm = new HashMap<String, TubeCallback>();
    private HashMap<String, String> hm_money = new HashMap<String, String>();

    // Метод регистрации трубки в базовой станции
    public void register (TubeCallback objRef, String phoneNum)
    {
        hm.put(phoneNum, objRef);
	hm_money.put(phoneNum, "100");
	
        System.out.println("Server: find phone "+phoneNum+" balans: "+hm_money.get(phoneNum));
	
    }

    // Метод пересылки сообщения от трубки к трубке
    public void sendSMS (String fromNum, String toNum, String message)
    {
	if( !hm.containsKey(toNum) )
	{
	  System.out.println("Server: phone "+fromNum+" can't send sms to "+toNum+" ("+toNum+" not found!)");
	  return;
	}
        
        // Здесь должен быть поиск объектной ссылки по номеру toNum
        
	int balans = Integer.decode( hm_money.get(fromNum) );
	int len = message.length();
	
	for(Character c : message.toCharArray()){
	    if (Character.isLetter(c)){
		len++;
	    } 
	}
	  
	if( balans==0 )
	{
	    System.out.println("Server: phone "+fromNum+" can't send sms to "+toNum+" (balans=0)");
	    return;
	}

	if(balans<len)
	{
	    message = message.substring(0,balans);
	    balans = 0;
	}
	else
	    balans = balans-len;

	hm_money.put(fromNum, Integer.toString(balans) );
	
	hm.get(toNum).sendSMS(fromNum, message);
	System.out.println("Server: phone "+fromNum+" send sms to "+toNum);
	System.out.println("Server: phone "+fromNum+" (balans="+hm_money.get(fromNum)+")");
	
    }
}

