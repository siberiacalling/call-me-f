package ru.bmstu.rk6.cell.impl;

import ru.bmstu.rk6.cell.stub.*;
import java.util.HashMap;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class StationImpl extends StationPOA {
	
	private String stationName;
	private DataBase superStation;
	private NamingContextExt nameContext;
	private HashMap<String, Tube> tubes = new HashMap<String, Tube>();		
	
	public StationImpl(NamingContextExt nameContext, String stationName){
		this.nameContext = nameContext;
		try{
			this.superStation = DataBaseHelper.narrow(nameContext.resolve_str("DataBase"));
			this.stationName = stationName;
			this.superStation.registerStation(stationName);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean registerTube(String number){	
		try {
			Tube phone = TubeHelper.narrow(nameContext.resolve_str(number));
			this.tubes.put(number, phone);				

			String message = String.format("New tube registered with number: @%s", number.trim());			
			System.out.println(message);		
			
			this.superStation.registerTube(this.stationName, number);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;		
	}
		
	@Override
	public boolean sendMessage(String fromNumber, String toNumber, String text){
		String message = String.format("Searching for tube @%s ...", toNumber.trim());		
		System.out.println(message);
		
		if (this.tubes.keySet().contains(toNumber)){
			try {
				Tube phone = this.tubes.get(toNumber);
				phone.showMessage(fromNumber, text);
								
				message = String.format("Tube @%s found in local cell.", toNumber.trim());		
				System.out.println(message);
				message = String.format("Processing local message from @%s to @%s. Text:'%s'", fromNumber.trim(), toNumber.trim(), text.trim());
				System.out.println(message);
								
				return true;
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
		else{
			String stationName = this.superStation.getStationByTube(toNumber, this.stationName);
			if (null != stationName){
				message = String.format("Tube @%s found on @%s station.", toNumber.trim(), stationName.trim());
				System.out.println(message);
				try {
					Station toStation = StationHelper.narrow(this.nameContext.resolve_str(stationName));
					toStation.sendInterMessage(fromNumber, toNumber, this.stationName, text);
					message = String.format("Forwarding to station @%s message from @%s to @%s. Text:'%s'", stationName.trim(), fromNumber.trim(), toNumber.trim(), text.trim());
					System.out.println(message);
					return true;
				} catch (Exception e) {				
					e.printStackTrace();
				}				
			}			
		}
		message = String.format("Tube @%s not found.", toNumber.trim());
		System.out.println(message);
		return false;
	}

	@Override
	public boolean sendInterMessage(String fromNumber, String toNumber,
			String fromStation, String text) {
		String message;
		if (this.tubes.keySet().contains(toNumber)){
			try {
				Tube phone = this.tubes.get(toNumber);
				phone.showMessage(fromNumber, text);
								
				message = String.format("Processing message from station @%s from @%s to @%s. Text:'%s'",fromStation.trim(), fromNumber.trim(), toNumber.trim(), text.trim());
				System.out.println(message);
								
				return true;
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
		message = "OOOps!???.";
		System.out.println(message);
		return false;
	}

}
