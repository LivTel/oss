package ngat.oss.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import ngat.phase2.IExecutionFailureContext;

/**
 * 
 */

/**
 * @author snf
 * 
 */
public interface IHistoryModel extends Remote {

	/**
	 * Returns a list of history-items belonging to the specified group .
	 * 
	 * @param groupID
	 *            The ID of the group.
	 * @throws RemoteException
	 */
	public List listHistoryItems(long groupID) throws RemoteException;

	/**
	 * 
	 * @param groupID
	 * @return
	 * @throws RemoteException
	 */
	public long addHistoryItem(long groupID) throws RemoteException;

	/**
	 * 
	 * @param histID
	 * @param cstat
	 * @param ctime
	 * @param efc
	 * @param qosStats
	 * @throws RemoteException
	 */
	public void updateHistory(long histID, int cstat, long ctime, IExecutionFailureContext efc, Set qosStats) throws RemoteException;
	
	/**
	 * 
	 * @param histID
	 * @param expID
	 * @param expTime
	 * @param fileName
	 * @throws RemoteException
	 */
	public void addExposureUpdate(long histID, long expID, long expTime, String fileName) throws RemoteException;
	
	/**
	 * 
	 * @param histID
	 * @return
	 * @throws RemoteException
	 */
	public List listExposureItems(long histID) throws RemoteException;
	
	/**
	 * 
	 * @param histID
	 * @return
	 * @throws RemoteException
	 */
	public List listQosItems(long histID) throws RemoteException;

}
