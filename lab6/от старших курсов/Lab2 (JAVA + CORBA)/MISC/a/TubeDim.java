import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;
import java.util.StringTokenizer;


class TubeCallbackServant extends TubeCallbackPOA
{
	String myNum;

	TubeCallbackServant (String num)
	{
		myNum = num;
	}

	public int sendSMS(String fromNum, String message)
	{
		String br = "";
		try
		{
			br = new String (message.getBytes("ISO-8859-1"),"UTF-8" );
		}
		catch(UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
		}
		System.out.println(myNum+": Message from "+fromNum+": "+br);
		return (0);
	}

	public String getNum()
	{
		return (myNum);
	}
}

class ORBThread extends Thread
{
	ORB myOrb;

	ORBThread(ORB orb)
	{
		myOrb = orb;
	}

	public void run()
	{
		myOrb.run();
	}
}

public class TubeDim
{
	public static void main(String args[])
	{
		try
		{
			String myNum = "Dim";
			ORB orb = ORB.init(args, null);

			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();
			TubeCallbackServant listener  = new TubeCallbackServant(myNum);
			rootPOA.activate_object(listener);
			TubeCallback ref = TubeCallbackHelper.narrow(rootPOA.servant_to_reference(listener));

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContext ncRef = NamingContextHelper.narrow(objRef);
			NameComponent nc = new NameComponent("BaseStation", "");
			NameComponent path[] = {nc};
			Station stationRef = StationHelper.narrow(ncRef.resolve(path));

			stationRef.register(ref, myNum);
			System.out.println("Registration terminated..");

			ORBThread orbThr = new ORBThread(orb);
			orbThr.start();

			BufferedReader inpt  = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while (true)
			{
				msg = inpt.readLine();
				String delims = "[\\[\\]]+";
				String[] token = msg.split(delims);
				if (token.length < 3)
				{
					System.out.println("2.");
				}
				else
				{	
					msg = token[2].trim();
					String toknext, tokbuf = "";
					//StringTokenizer tokmsg = new StringTokenizer(msg, " \t\n");
					//while(tokmsg.hasMoreTokens())
					String[] tokmsg = msg.split("\\s");
					for( int x=0; x<tokmsg.length; x++)
					{
					  
					  toknext = tokmsg[x];
					  if(toknext.equals("22"))
					    toknext = "А";
					  if(toknext.equals("222"))
					    toknext = "Б";
					  if(toknext.equals("2222"))
					    toknext = "В";
					  if(toknext.equals("22222"))
					    toknext = "Г";
					  if(toknext.equals("33"))
					    toknext = "Д";
					  if(toknext.equals("333"))
					    toknext = "Е";
					  if(toknext.equals("3333"))
					    toknext = "Ж";
					  if(toknext.equals("33333"))
					    toknext = "З";
					  if(toknext.equals("44"))
					    toknext = "И";
					  if(toknext.equals("444"))
					    toknext = "Й";
					  if(toknext.equals("4444"))
					    toknext = "К";
					  if(toknext.equals("44444"))
					    toknext = "Л";
					  if(toknext.equals("55"))
					    toknext = "М";
					  if(toknext.equals("555"))
					    toknext = "Н";
					  if(toknext.equals("5555"))
					    toknext = "О";
					  if(toknext.equals("55555"))
					    toknext = "П";
					  if(toknext.equals("66"))
					    toknext = "Р";
					  if(toknext.equals("666"))
					    toknext = "С";
					  if(toknext.equals("6666"))
					    toknext = "Т";
					  if(toknext.equals("66666"))
					    toknext = "У";
					  if(toknext.equals("77"))
					    toknext = "Ф";
					  if(toknext.equals("777"))
					    toknext = "Х";
					  if(toknext.equals("7777"))
					    toknext = "Ц";
					  if(toknext.equals("77777"))
					    toknext = "Ч";
					  if(toknext.equals("88"))
					    toknext = "Ш";
					  if(toknext.equals("888"))
					    toknext = "Щ";
					  if(toknext.equals("8888"))
					    toknext = "Ъ";
					  if(toknext.equals("88888"))
					    toknext = "Ы";
					  if(toknext.equals("99"))
					    toknext = "Ь";
					  if(toknext.equals("999"))
					    toknext = "Э";
					  if(toknext.equals("9999"))
					    toknext = "Ю";
					  if(toknext.equals("99999"))
					    toknext = "Я";
					  if(toknext.equals("00"))
					    toknext = " ";
					  if(toknext.equals("**"))
					    toknext = ".";
					  if(toknext.equals("***"))
					    toknext = ",";
					  tokbuf = tokbuf+toknext;
					}
					
					try 
					{
						tokbuf = new String(tokbuf.getBytes("UTF-8"), "ISO-8859-1");
					}
					catch(UnsupportedEncodingException uee)
					{
						uee.printStackTrace();
					}
					stationRef.sendSMS(myNum, token[1], tokbuf);
				}
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
