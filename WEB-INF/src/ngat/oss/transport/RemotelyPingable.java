package ngat.oss.transport;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotelyPingable extends Remote {

	/**
	 * 
	 * @return Some sort of message to indicate that the Pingable object is alive.
	 */
	public void ping() throws RemoteException;
	
}
