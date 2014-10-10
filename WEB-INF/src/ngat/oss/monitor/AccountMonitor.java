package ngat.oss.monitor;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ngat.oss.listeners.AccountingModelUpdateListener;

/**
 * 
 */


 /** Provides a means for clients to register for updates when the AccountingModel changes.
 * @author snf
 *
 */
public interface AccountMonitor extends Remote {
	
	/**
	 * Register a accountModelUpdateListener if not already registered.
	 * @param l An instance of accountModelUpdateListener to de-register.
	 */
	public void addAccountUpdateListener(AccountingModelUpdateListener l) throws RemoteException;
	
	/**
	 * Remove a registered accountModelUpdateListener if registered.
	 * @param l An instance of accountModelUpdateListener to register.
	 */
	public void removeAccountUpdateListener(AccountingModelUpdateListener l) throws RemoteException;
	
}
