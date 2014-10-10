package ngat.oss.util;

import java.rmi.*;

public class What {

	/** outputs to console the names of all RMI bindable objects on the specified host
	 * e.g. output:
	 		[0] //ltdev1:1099/GateKeeper
	 		[1] //ltdev1:1099/AccessModel
	 		[2] //ltdev1:1099/Phase2Model
	 *
	 * @param args
	 */ 
   public static void main(String[] args) {

       try {
           String where = args[0];
           String[] which = Naming.list("rmi://"+where);
           for (int i = 0; i < which.length; i++ ){
               System.err.println("["+i+"] "+which[i]);
           }
       } catch (Exception e) {
           e.printStackTrace();
           return;
       }
   }
}