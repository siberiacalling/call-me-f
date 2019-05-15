import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;

import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

// Класс, реализующий IDL-интерфейс базовой станции
class StationServant extends StationPOA {
	// Вместо представленных ниже двух переменных здесь
	// должен быть список пар "номер - объектная ссылка"
	//TubeCallback tubeRef;
	//String tubeNum;
	Map<String, TubeCallback> mp = new HashMap<String, TubeCallback>();

	// Метод регистрации трубки в базовой станции
	public int register (TubeCallback objRef, String phoneNum) {
		//tubeRef = objRef;
		//tubeNum = phoneNum;
		mp.put(phoneNum, objRef);
		System.out.println("Станция: зарегистрирована трубка "+phoneNum);
		return (1);
	};

	// Метод пересылки сообщения от трубки к трубке
	public int sendSMS (String fromNum, String toNum, String message) {
		System.out.println("Станция: трубка "+fromNum+" посылает сообщение "+toNum);
		// Здесь должен быть поиск объектной ссылки по номеру toNum
		//tubeRef.sendSMS(fromNum, message);
		if (mp.containsKey(toNum))
		{
			mp.get(toNum).sendSMS(fromNum, message);
		}
		else
		{

			String mes = "Не зарегистрированая трубка " + toNum + ".";
			try 
			{
				mes = new String(mes.getBytes("UTF-8"), "ISO-8859-1");
			} 
			catch(UnsupportedEncodingException uee) 
			{
				uee.printStackTrace();
			}
			System.out.println("Обращение к незарегистрированной трубке " + toNum + ".");
			mp.get(fromNum).sendSMS("SERVER", mes);
		}
		return (1);
	};
};

// Класс, реализующий сервер базовой станции
public class StationServer {

	public static void main(String args[]) {
		try{
			// Создание и инициализация ORB
			ORB orb = ORB.init(args, null);

			// Получение ссылки и активирование POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// Создание серванта для CORBA-объекта "базовая станция" 
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

			System.out.println("Сервер готов и ждет ...");

			// Ожидание обращений от клиентов (трубок)
			orb.run();
		} 
		catch (Exception e) {
			System.err.println("Ошибка: " + e);
			e.printStackTrace(System.out);
		};
	};
};
