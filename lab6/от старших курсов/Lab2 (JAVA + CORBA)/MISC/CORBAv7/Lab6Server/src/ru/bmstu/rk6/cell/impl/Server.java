package ru.bmstu.rk6.cell.impl;

import ru.bmstu.rk6.cell.stub.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server {
	
	private static String stationName;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Enter station name:");
			byte num[] = new byte[256];
			System.in.read(num);			
			Server.stationName = new String(num);
			
			ORB orb = ORB.init(args,null);
			
			org.omg.CORBA.Object rootPOARef = orb.resolve_initial_references("RootPOA");
			POA rootPOA  = POAHelper.narrow(rootPOARef);
			rootPOA.the_POAManager().activate();			
		
			org.omg.CORBA.Object ncRef = orb.resolve_initial_references("NameService");
			NamingContextExt nc = NamingContextExtHelper.narrow(ncRef);
			
			StationImpl implPOA = new StationImpl(nc, Server.stationName);

			org.omg.CORBA.Object implRef = rootPOA.servant_to_reference(implPOA);
			Station impl = StationHelper.narrow(implRef);
						
			NameComponent orbName[] = nc.to_name(Server.stationName);
			nc.rebind(orbName, impl);
			
		    System.out.println("Server is ready");

	        // wait for invocations from clients
	        orb.run();	        
	        
		}catch(Exception ex){
			ex.printStackTrace();			
		}	

	}

}
