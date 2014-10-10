package ngat.oss.util;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Whatever {

	public static void main(String[] args) {

       try {
           String where = args[0];
           System.out.println("looking at services on " + where);
           String[] which = Naming.list("rmi://"+where);
           System.out.println("... Naming.list returns: " +which);
           for (int i = 0; i < which.length; i++ ){
        	   String hostName = getHostNameFromListElement(which[i]);
        	   String bindingName = getBindingNameFromListElement(which[i]);
        	   String objectType = getClassNameOfRmiObject(bindingName, hostName);
               System.out.println("host:" +hostName + ", binding name:" +bindingName + ", object type:" + objectType);
           }
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }
   }
	
	private static String getHostNameFromListElement(String listElement) {
		
		String hostName = "";
		listElement = listElement.substring(2, listElement.length());
		int colonCharInt = (int)':';
		int posOfColon = listElement.indexOf(colonCharInt);
		hostName = listElement.substring(0, posOfColon);
		
		return hostName;
	}
	
	private static String getBindingNameFromListElement(String listElement) {
		
		String bindingName = "";
		listElement = listElement.substring(2, listElement.length());
		int colonCharInt = (int)':';
		int posOfColon = listElement.indexOf(colonCharInt);
		bindingName = listElement.substring(posOfColon + 1, listElement.length());
		int slashCharInt = (int)'/';
		int posOfSlash = bindingName.indexOf(slashCharInt);
		bindingName = bindingName.substring(posOfSlash + 1, bindingName.length());
		
		return bindingName;
	}
	
	private static String getClassNameOfRmiObject(String what, String where) throws MalformedURLException, RemoteException, NotBoundException {
		Object whatsit = Naming.lookup("rmi://"+where+"/"+what);
		return whatsit.getClass().getName();
	}
}
