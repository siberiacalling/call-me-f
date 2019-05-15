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

class StationServant extends StationPOA
{
	Map<String, TubeCallback> mp = new HashMap<String, TubeCallback>();

	public int register (TubeCallback objRef, String phoneNum)
	{
		mp.put(phoneNum, objRef);
		System.out.println("New connection: " + phoneNum);
		return (1);
	}

	public int sendSMS (String fromNum, String toNum, String message)
	{
		System.out.println("Message from "+fromNum+" to "+toNum);

		if (mp.containsKey(toNum))
		{
			mp.get(toNum).sendSMS(fromNum, message);
		}
		else
		{
			String mes = "Message delivered " + toNum + ".";
			try 
			{
				mes = new String(mes.getBytes("UTF-8"), "ISO-8859-1");
			} 
			catch(UnsupportedEncodingException uee) 
			{
				uee.printStackTrace();
			}
			System.out.println("Message delivered to:" + toNum + ".");
			mp.get(fromNum).sendSMS("SERVER", mes);
		}
		return (1);
	}
}

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

			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
			Station sref = StationHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			String name = "BaseStation";
			NameComponent path[] = ncRef.to_name( name );
			ncRef.rebind(path, sref);

			System.out.println("Waiting connection...");

			orb.run();
		} 
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			e.printStackTrace(System.out);
		}
	}
}
