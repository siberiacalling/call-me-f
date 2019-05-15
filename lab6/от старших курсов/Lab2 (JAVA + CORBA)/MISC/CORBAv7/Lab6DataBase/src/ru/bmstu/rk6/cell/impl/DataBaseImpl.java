package ru.bmstu.rk6.cell.impl;

import java.util.HashMap;
import ru.bmstu.rk6.cell.stub.*;

public class DataBaseImpl extends DataBasePOA {		
	private HashMap<String, String> tubesInStations = new HashMap<String, String>();//key - tubeNumber, value - stationName
		
	@Override
	public boolean registerStation(String stationName) {
		String message = String.format("New station registered with name: @%s", stationName.trim());
		System.out.println(message);		
		return false;
	}

	@Override
	public boolean registerTube(String stationName, String tubeName) {
		this.tubesInStations.put(tubeName, stationName);
		String message = String.format("Tube @%s registered on station @%s", tubeName.trim(), stationName.trim());
		System.out.println(message);
		return false;
	}

	@Override
	public String getStationByTube(String tubeName, String receiverName) {
		String stationName = null;
		
		String message = String.format("Station @%s requesting for tube @%s ...",receiverName ,tubeName.trim());
		System.out.println(message);
		
		stationName =  this.tubesInStations.get(tubeName);
		if (null == stationName){
			message = String.format("Tube @%s is not registered at any station", tubeName.trim());
		}
		else{
			message = String.format("Tube @%s is found on @%s station", tubeName.trim(), stationName.trim());
		}
		System.out.println(message);	
		
		return stationName;
	}

}
