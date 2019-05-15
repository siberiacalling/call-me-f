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

// �����, ����������� IDL-��������� ������� �������
class StationServant extends StationPOA {
	// ������ �������������� ���� ���� ���������� �����
	// ������ ���� ������ ��� "����� - ��������� ������"
	//TubeCallback tubeRef;
	//String tubeNum;
	Map<String, TubeCallback> mp = new HashMap<String, TubeCallback>();

	// ����� ����������� ������ � ������� �������
	public int register (TubeCallback objRef, String phoneNum) {
		//tubeRef = objRef;
		//tubeNum = phoneNum;
		mp.put(phoneNum, objRef);
		System.out.println("�������: ���������������� ������ "+phoneNum);
		return (1);
	};

	// ����� ��������� ��������� �� ������ � ������
	public int sendSMS (String fromNum, String toNum, String message) {
		System.out.println("�������: ������ "+fromNum+" �������� ��������� "+toNum);
		// ����� ������ ���� ����� ��������� ������ �� ������ toNum
		//tubeRef.sendSMS(fromNum, message);
		if (mp.containsKey(toNum))
		{
			mp.get(toNum).sendSMS(fromNum, message);
		}
		else
		{

			String mes = "�� ����������������� ������ " + toNum + ".";
			try 
			{
				mes = new String(mes.getBytes("UTF-8"), "ISO-8859-1");
			} 
			catch(UnsupportedEncodingException uee) 
			{
				uee.printStackTrace();
			}
			System.out.println("��������� � �������������������� ������ " + toNum + ".");
			mp.get(fromNum).sendSMS("SERVER", mes);
		}
		return (1);
	};
};

// �����, ����������� ������ ������� �������
public class StationServer {

	public static void main(String args[]) {
		try{
			// �������� � ������������� ORB
			ORB orb = ORB.init(args, null);

			// ��������� ������ � ������������� POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// �������� �������� ��� CORBA-������� "������� �������" 
			StationServant servant = new StationServant();

			// ��������� ��������� ������ �� �������
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
			Station sref = StationHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// ���������� ��������� ������ � ������
			String name = "BaseStation";
			NameComponent path[] = ncRef.to_name( name );
			ncRef.rebind(path, sref);

			System.out.println("������ ����� � ���� ...");

			// �������� ��������� �� �������� (������)
			orb.run();
		} 
		catch (Exception e) {
			System.err.println("������: " + e);
			e.printStackTrace(System.out);
		};
	};
};
