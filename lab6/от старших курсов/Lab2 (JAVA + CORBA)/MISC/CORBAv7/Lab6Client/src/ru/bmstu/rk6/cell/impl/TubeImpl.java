package ru.bmstu.rk6.cell.impl;
import ru.bmstu.rk6.cell.stub.*;

public class TubeImpl extends TubePOA {

	@Override
	public void showMessage(String fromNumber, String text) {
		String message = String.format("@%s: '%s'", fromNumber.trim(), text.trim());
		System.out.println(message);
	}		

}
