import Cell.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.io.*;

// лМБУУ ЧЩЪПЧБ ФЕМЕЖПООПК ФТХВЛЙ
class TubeCallbackServant extends TubeCallbackPOA {
	String myNum;	// оПНЕТ ФТХВЛЙ

	// лПОУФТХЛФПТ ЛМБУУБ
	TubeCallbackServant (String num) {
		myNum = num;
	};

	// нЕФПД ПВТБВПФЛЙ РТЙОСФПЗП УППВЭЕОЙС
	public int sendSMS(String fromNum, String message) {
		String br = "";
		try {
			br = new String (message.getBytes("ISO-8859-1"),"UTF-8" );
		} catch(UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		System.out.println(myNum+": РТЙОСФП УППВЭЕОЙЕ ПФ "+fromNum+": "+br);
		return (0);
	};

	// нЕФПД, ЧПЪЧТБЭБАЭЙК ОПНЕТ ФТХВЛЙ
	public String getNum() {
		return (myNum);
	};
};

// лМБУУ, ЙУРПМШЪХЕНЩК ДМС УПЪДБОЙС РПФПЛБ ХРТБЧМЕОЙС
class ORBThread extends Thread {
	ORB myOrb;

	// лПОУФТХЛФПТ ЛМБУУБ
	ORBThread(ORB orb) {
		myOrb = orb;
	};

	// нЕФПД ЪБРХУЛБ РПФПЛБ
	public void run() {
		myOrb.run();
	};
};

// лМБУУ, ЙНЙФЙТХАЭЙК ФЕМЕЖПООХА ФТХВЛХ
public class TubeAndrey {

	public static void main(String args[]) {
		try {
			String myNum = "Andrey";	// оПНЕТ ФТХВЛЙ
			// уПЪДБОЙЕ Й ЙОЙГЙБМЙЪБГЙС ORB
			ORB orb = ORB.init(args, null);

			//уПЪДБОЙЕ УЕТЧБОФБ ДМС IDL-ЙОФЕТЖЕКУБ TubeCallback
			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();
			TubeCallbackServant listener  = new TubeCallbackServant(myNum);
			rootPOA.activate_object(listener);
			// рПМХЮЕОЙЕ УУЩМЛЙ ОБ УЕТЧБОФ
			TubeCallback ref = TubeCallbackHelper.narrow(rootPOA.servant_to_reference(listener));

			// рПМХЮЕОЙЕ ЛПОФЕЛУФБ ЙНЕОПЧБОЙС
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContext ncRef = NamingContextHelper.narrow(objRef);

			// рТЕПВТБЪПЧБОЙЕ ЙНЕОЙ ВБЪПЧПК УФБОГЙЙ Ч ПВЯЕЛФОХА УУЩМЛХ
			NameComponent nc = new NameComponent("BaseStation", "");
			NameComponent path[] = {nc};
			Station stationRef = StationHelper.narrow(ncRef.resolve(path));

			// тЕЗЙУФТБГЙС ФТХВЛЙ Ч ВБЪПЧПК УФБОГЙЙ
			stationRef .register(ref, myNum);
			System.out.println("фТХВЛБ ЪБТЕЗЙУФТЙТПЧБОБ ВБЪПЧПК УФБОГЙЕК");

			// ъБРХУЛ ORB Ч ПФДЕМШОПН РПФПЛЕ ХРТБЧМЕОЙС
			// ДМС РТПУМХЫЙЧБОЙС ЧЩЪПЧПЧ ФТХВЛЙ
			ORBThread orbThr = new ORBThread(orb);
			orbThr.start();

			// вЕУЛПОЕЮОЩК ГЙЛМ ЮФЕОЙС УФТПЛ У ЛМБЧЙБФХТЩ Й ПФУЩМЛЙ ЙИ
			// ВБЪПЧПК УФБОГЙЙ
			BufferedReader inpt  = new BufferedReader(new InputStreamReader(System.in));
			String msg;
			while (true)
			{
				msg = inpt.readLine();
				String delims = "[\\[\\]]+";
				String[] token = msg.split(delims);
				if (token.length < 3)
				{
					System.out.println("пЫЙВЛБ ЖПТНБФБ УППВЭЕОЙС. оЕПВИПДЙНП: [ОПНЕТ БВПОЕОФБ] УППВЭЕОЙЕ");
				}
				else
				{
					msg = token[2].trim();
					try 
					{
						msg = new String(msg.getBytes("UTF-8"), "ISO-8859-1");
					} 
					catch(UnsupportedEncodingException uee)
					{
						uee.printStackTrace();
					}
					stationRef.sendSMS(myNum, token[1], msg);
				}
				// пВТБФЙФЕ ЧОЙНБОЙЕ: ОПНЕТ РПМХЮБФЕМС 7890 Ч ПРЙУБООПК ТБОЕЕ
				// ТЕБМЙЪБГЙЙ ВБЪПЧПК УФБОГЙЙ ТПМЙ ОЕ ЙЗТБЕФ
			}

		} 
		catch (Exception e)
		{
			e.printStackTrace();
		};


	};

};

