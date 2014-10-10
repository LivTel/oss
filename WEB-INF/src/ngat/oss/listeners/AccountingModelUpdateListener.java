/**
 * 
 */
package ngat.oss.listeners;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ngat.phase2.IAccount;

/**This interface allows external classes to receive information when an AccountingModel is changed
 * in any way. Implementors should register with the model they wish to monitor.
 * @author snf
 *
 */
public interface AccountingModelUpdateListener extends Remote {
	/**
	 * Notifies observers that an account has been added to the observed AccountingModel.
	 * @param aid The ID of the new account.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void accountAdded(IAccount account, long ownerId, long semesterId) throws RemoteException;
	
	/**
	 * Notifies observers that an account has been deleted from the observed AccountingModel.
	 * @param aid The ID of the account deleted.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void accountDeleted(long aid) throws RemoteException;
	
	/** 
	 * Notifies observers that an account in the observed AccountingModel has been modified in some way.
	 * @param aid The ID of the account  that was modified.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void accountUpdated(IAccount account) throws RemoteException;
}
