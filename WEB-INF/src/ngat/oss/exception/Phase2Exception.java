package ngat.oss.exception;

import java.rmi.RemoteException;

public class Phase2Exception extends RemoteException {

    /** Create a RemoteException with no message and the given code.
     */
    public Phase2Exception() {
	super();
    }

    /** Create a RemoteException with the given message and  code.
     * @param message The error message.
     */
    public Phase2Exception(String message) {
	super(message);
    }
    
    /** Create a RemoteException wrapping the given exception.
     * @param e An enclosed exception.
     */
    public Phase2Exception(Exception e) {
    this(e.getMessage());	   	
    }

    /** Create a RemoteException wrapping the given throwable.
     * @param t An enclosed throwable.
     */
    public Phase2Exception(Throwable t) {
    this(t.getMessage());	   	
    }
    
    /** Returns a readable description of the exception
     * in the form: RemoteException: <code> : <message>
     */
    public String toString() {
	return "Phase2Exception : "+getMessage();
    }

}
