/**
 * 
 */
package ngat.oss.listeners;

import java.rmi.RemoteException;

/** This interface allows external classes to receive information when an AccessModel is changed
 * in any way. Implementors should register with the model they wish to monitor.
 * @author snf
 *
 */
public interface AccessModelUpdateListener {

	/**
	 * Notifies observers that an AccessPermission has been added to the observed AccessModel.
	 * @param pid The ID of the new AccessPermission.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void accessPermissionAdded(long pid) throws RemoteException;
	
	/**
	 * Notifies observers that an AccessPermission has been deleted from the observed AccessModel.
	 * @param pid The ID of the AccessPermission deleted.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void accessPermissionDeleted(long pid) throws RemoteException;
	
	/** 
	 * Notifies observers that an AccessPermission in the observed AccessModel has been modified in some way.
	 * @param pid The ID of the AccessPermission  that was modified.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void accessPermissionUpdated(long pid) throws RemoteException;
	
	
	/**
	 * Notifies observers that an User has been added to the observed AccessModel.
	 * @param pid The ID of the new User.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void userAdded(long pid) throws RemoteException;
	
	/**
	 * Notifies observers that an User has been deleted from the observed AccessModel.
	 * @param pid The ID of the User deleted.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void userDeleted(long pid) throws RemoteException;
	
	/** 
	 * Notifies observers that an User in the observed AccessModel has been modified in some way.
	 * @param pid The ID of the User  that was modified.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void userUpdated(long pid) throws RemoteException;
	
	
}
