package ngat.oss.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ngat.phase2.ILock;

/** An entity which manages Phase2 object  locks.
 * @author nrc
 *
 */
public interface ILockingModel extends Remote {
	
	/**
	 * Lock a group object for editing
	 * @param groupID The ID of the group to lock
	 * @param clientID The ID of the client locking the object
	 * @return The key required to unlock the object in the future
	 * @throws RemoteException If any part of the operation fails
	 */
	public long lockGroup(long groupID, String clientID) throws RemoteException;
	
	/**
	 * Unlock the group to make available to other clients for editing
	 * @param groupID The id of the group to unlock
	 * @param key The key needed to perform the unlock
	 * @return Whether the unlocking procedure was successful
	 * @throws RemoteException If any part of the operation fails 
	 */
	public boolean unlockGroup(long groupID, int key) throws RemoteException;
	
	/**
	 * Provide the clientRef (i.e. the identity) of a client who holds the lock of a group
	 * @param groupID The id of the group who's lock's clientRef is required
	 * @return The clientRef
	 * @throws RemoteException If any part of the operation fails 
	 */
	public ILock getGroupLock(long groupID) throws RemoteException;
	
}

