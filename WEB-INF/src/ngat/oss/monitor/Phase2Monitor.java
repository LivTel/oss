package ngat.oss.monitor;

import ngat.oss.listeners.Phase2ModelUpdateListener;
import java.rmi.*;

/** Provides a means for clients to register for updates when the Phase2Model changes.
 * @author snf
 *
 */
public interface Phase2Monitor extends Remote {

	/**
	 * Register a phase2ModelUpdateListener if not already registered.
	 * @param l An instance of Phase2ModelUpdateListener to de-register.
	 */
	public void addPhase2UpdateListener(Phase2ModelUpdateListener l) throws RemoteException;
	
	/**
	 * Remove a registered phase2ModelUpdateListener if registered.
	 * @param l An instance of Phase2ModelUpdateListener to register.
	 */
	public void removePhase2UpdateListener(Phase2ModelUpdateListener l) throws RemoteException;
	
}
