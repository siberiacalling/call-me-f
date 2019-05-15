package ru.bmstu.rk6.cell.impl;

import ru.bmstu.rk6.cell.stub.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server {

	private static String number;
	private static String stationName;
	private static Station station;
	private static ORB orb;
	private static Thread listener;
	
	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		try {
			System.out.println("Enter your number:");
			byte num[] = new byte[256];
			System.in.read(num);			
			Server.number = new String(num);
			
			System.out.println("Enter station name:");
			num = new byte[256];
			System.in.read(num);			
			Server.stationName = new String(num);
			
			Server.orb = ORB.init(args,null);
			
			org.omg.CORBA.Object ncRef = orb.resolve_initial_references("NameService");
			NamingContextExt nc = NamingContextExtHelper.narrow(ncRef);
			
			org.omg.CORBA.Object rootPOARef = orb.resolve_initial_references("RootPOA");
			POA rootPOA  = POAHelper.narrow(rootPOARef);
			rootPOA.the_POAManager().activate();
			
			TubeImpl implPOA = new TubeImpl();

			org.omg.CORBA.Object implRef = rootPOA.servant_to_reference(implPOA);
			Tube impl = TubeHelper.narrow(implRef);
			
			String name = Server.number;
			NameComponent orbName[] = nc.to_name(name);
			nc.rebind(orbName, impl);				
			
			Server.station = StationHelper.narrow(nc.resolve_str(Server.stationName));
			Server.station.registerTube(Server.number);
			
			Runnable shadowListener = new Runnable() {	
				@Override
				public void run() {
					Server.orb.run();					
				}
			};			
			Server.listener = new Thread(shadowListener);
			Server.listener.start();
			
			for(;;){
				System.out.println("Enter command: (1 - Enter phone number; 2 - exit )");
				byte[] command = new byte[256];
				System.in.read(command);
				
				if ('1' == command[0]){
					byte []number = new byte[256];
					byte []message = new byte[1024];
					
					System.out.println("Enter phone number:");
					System.in.read(number);
					System.out.println("Enter Message:");
					System.in.read(message);		

					Server.station.sendMessage(Server.number, new String(number), new String(message));
				}
				if ('2' == command[0])
					break;				
			}
			Server.listener.stop();
		}
		catch(Exception ex){
			ex.printStackTrace();			
		}	
	}

}
