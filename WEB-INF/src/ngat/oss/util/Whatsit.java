package ngat.oss.util;

import java.rmi.*;

public class Whatsit {
	
	public static void main(String[] args) {
		try {
			String where = args[0];
			String what  = args[1];
			
			Object whatsit = Naming.lookup("rmi://"+where+"/"+what);
			System.err.println(what+" isa "+whatsit.getClass().getName());
		} catch (Exception e) {
			
			e.printStackTrace();
			return;
		}
	}
}
