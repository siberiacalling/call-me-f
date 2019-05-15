package ru.bmstu.rk6.cell.impl;

import ru.bmstu.rk6.cell.stub.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ORB orb = ORB.init(args,null);
			
			org.omg.CORBA.Object rootPOARef = orb.resolve_initial_references("RootPOA");
			POA rootPOA  = POAHelper.narrow(rootPOARef);
			rootPOA.the_POAManager().activate();			
		
			org.omg.CORBA.Object ncRef = orb.resolve_initial_references("NameService");
			NamingContextExt nc = NamingContextExtHelper.narrow(ncRef);
			
			DataBaseImpl implPOA = new DataBaseImpl();

			org.omg.CORBA.Object implRef = rootPOA.servant_to_reference(implPOA);
			DataBase impl = DataBaseHelper.narrow(implRef);
						
			String name = "DataBase";
			NameComponent orbName[] = nc.to_name(name);
			nc.rebind(orbName, impl);
			
		    System.out.println("DataBase superServer is ready");

	        // wait for invocations from clients
	        orb.run();	        
	        
		}catch(Exception ex){
			ex.printStackTrace();			
		}	

	}

}
