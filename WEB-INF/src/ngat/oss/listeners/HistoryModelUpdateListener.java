package ngat.oss.listeners;

import java.rmi.RemoteException;

/**
 * @author snf
 *
 */
public interface HistoryModelUpdateListener {

	/**
	 * Notifies observers that a HistoryItem has been added to the observed HistoryModel.
	 * @param gid The ID of the group for which the HistoryItem was added.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void historyItemAdded(long gid) throws RemoteException;
	

}
