/**
 * 
 */
package ngat.oss.listeners;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ngat.phase2.IGroup;
import ngat.phase2.IProposal;
import ngat.phase2.ISequenceComponent;
import ngat.phase2.ITarget;

/** This interface allows external classes to receive information when a Phase2Model is changed
 * in any way. Implementors should register with the model they wish to monitor.
 * @author snf
 *
 */
public interface Phase2ModelUpdateListener extends Remote {

	/**
	 * Notifies observers that a proposal has been added to the observed Phase2Model.
	 * @param pid The ID of the new proposal.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void proposalAdded(IProposal proposal) throws RemoteException;
	
	/**
	 * Notifies observers that a proposal has been deleted from the observed Phase2Model.
	 * @param pid The ID of the proposal deleted.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void proposalDeleted(long pid) throws RemoteException;
	
	/** 
	 * Notifies observers that a proposal in the observed Phase2Model has been modified in some way.
	 * @param pid The ID of the proposal  that was modified.
	 * @throws RemoteException If anything goes wrong.
	 */
	public void proposalUpdated(IProposal proposal) throws RemoteException;
	
	/**
	 * Notifies observers that a group in the observed Phase2Model has been added.
	 * @param pid The ID of the proposal to which the group was added.
	 * @param group The group added.
	 * @throws RemoteException
	 */
	public void groupAdded(long pid, IGroup group) throws RemoteException;
	
	/**
	 * Notifies observers that a group in the observed Phase2Model has been deleted.
	 * @param id The ID of the group deleted.
	 * @throws RemoteException
	 */
	public void groupDeleted(long id) throws RemoteException;
	
	/**
	 * Notifies observers that a group in the observed Phase2Model has been updated.
	 * @param group Ther group that has been updated
	 * @throws RemoteException
	 */
	public void groupUpdated(IGroup group) throws RemoteException;
	
	/**
	 * Notifies observers that a target in the observed Phase2Model has been added.
	 * @param pid The ID of the programme to which the group was added.
	 * @param target The target added.
	 * @throws RemoteException
	 */
	public void targetAdded(long pid, ITarget target) throws RemoteException;
	
	/**
	 * Notifies observers that a target in the observed Phase2Model has been deleted.
	 * @param id The ID of the target deleted.
	 * @throws RemoteException
	 */
	public void targetDeleted(long tid) throws RemoteException;
	
	/**
	 * Notifies observers that a target in the observed Phase2Model has been updated.
	 * @param target The target that has been updated
	 * @throws RemoteException
	 */
	public void targetUpdated( ITarget target) throws RemoteException;
	
	/**
	 * Notifies observers that an observation sequence has been added to a group in the observed Phase2Model.
	 * @param gid The ID of the group to which the sequence was added.
	 * @param sequence The sequence added
	 * @throws RemoteException
	 */
	public void groupObsSequenceAdded(long gid, ISequenceComponent sequence) throws RemoteException;
	
	/**
	 * Notifies observers that an observation sequence has been deleted from a group in the observed Phase2Model.
	 * @param gid The id of the group that the sequence was removed from.
	 * @throws RemoteException
	 */
	public void groupObsSequenceDeleted(long gid) throws RemoteException;
	
	/**
	 * Notifies observers that an observation sequence has been updated in the observed Phase2Model.
	 * @param gid The id of the group the sequence belongs to
	 * @param sequence The new sequence.
	 * @throws RemoteException
	 */
	public void groupObsSequenceUpdated(long gid, ISequenceComponent sequence) throws RemoteException;
}
