package ngat.oss.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ngat.phase2.IPublishedSystemProperties;

/** Provides a query surface for telescope status requests..*/
public interface IStatusModel extends Remote {

	/** 
	 * @return version The minimum version number of a client that is allowed to access the services.
	 * @throws RemoteException
	 */
	public IPublishedSystemProperties getPublishedSystemProperties() throws RemoteException;
	
}
