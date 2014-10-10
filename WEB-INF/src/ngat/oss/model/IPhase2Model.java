package ngat.oss.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.phase2.IGroup;
import ngat.phase2.IInstrumentConfig;
import ngat.phase2.IProgram;
import ngat.phase2.IProposal;
import ngat.phase2.IRevision;
import ngat.phase2.ISequenceComponent;
import ngat.phase2.ITag;
import ngat.phase2.ITarget;

/**
 * A Phase2Model provides a view of the background database holding the Phase2
 * observation specifications, accounting information, execution history and QOS
 * measures. The basic Phase2Model provides accessors to return objects which
 * implement the Phase2 readonly interfaces and mutators which allow these
 * objects to be passed in to update the database. This interface is intended to
 * provide a database-implementation neutral view.
 */
public interface IPhase2Model extends Remote  {

	/**
	 * Creates a new group in the database.
	 * 
	 * @param propID
	 *            TODO
	 * @param group
	 *            the new group to insert in the database.
	 * 
	 * @return the ID of the new group.
	 * @throws RemoteException
	 *             if the group cannot be created in the database.
	 */
	public long addGroup(long propID, IGroup group) throws RemoteException;

	/**
	 * Creates a new programme in the database.
	 * 
	 * @param programme
	 *            the new programme to insert in the database.
	 * @return the ID of the new programme.
	 * @throws RemoteException
	 *             if the programme cannot be created in the database..
	 */
	public long addProgramme(IProgram programme) throws RemoteException;

	/**
	 * Creates a new proposal in the database.
	 * 
	 * @param proposal
	 *            the new proposal to insert in the database.
	 * @return the ID of the new proposal.
	 * @throws RemoteException
	 *             if the proposal cannot be created in the database.
	 */
	public long addProposal(long tagId, long progId, IProposal proposal) throws RemoteException;

	/**
	 * Creates a new TAG in the database.
	 * 
	 * @param tag
	 *            the new TAG to insert in the database.
	 * @return the ID of the new TAG.
	 * @throws RemoteException
	 *             if the TAG cannot be created in the database..
	 */
	
	public long addTag(ITag tag) throws RemoteException;

	/**
	 * Creates a new target in the database.
	 * 
	 * @param progID
	 *            the id of the programme to add the target to
	 * @param target
	 *            the new target to insert in the database.
	 * 
	 * @return the ID of the new target.
	 * @throws RemoteException
	 *             if the target cannot be created in the database.
	 */
	public long addTarget(long progID, ITarget target) throws RemoteException;

	/**
	 * Creates a new config in the database.
	 * 
	 * @param progID
	 *            the id of the programme to add the target to
	 * @param config
	 *            The new instrument configuration.
	 * @return the ID of the new config.
	 * @throws RemoteException
	 *             if the config cannot be created in the database.
	 */
	public long addInstrumentConfig(long progID, IInstrumentConfig config) throws RemoteException;

	/**
	 * 
	 * @param groupID
	 * @param sequenceComponent
	 * @return
	 * @throws RemoteException
	 */
	public long addObservationSequence(long groupID, ISequenceComponent sequenceComponent) throws RemoteException;
	
	/**
	 * Create a new proposal revsion.
	 * @param propID The ID of the proposal.
	 * @param time Time when revision occurred.
	 * @param editor The editor (a user or system name or some combo).
	 * @param comment What was done.
	 * @throws RemoteException
	 */
	public void addRevision(long propID, IRevision revision) throws RemoteException;
	
	/**
	 * Changes the Tag id of the proposal with the given id
	 * @param proposalId The proposal to alter
	 * @param tagId The new tag id of the proposal
	 * @throws RemoteException
	 */
	public void changeTagOfProposal(long proposalId, long tagId) throws RemoteException;
	
	/**
	 *  Changes the Programme id of the proposal with the given id
	 * @param proposalId The proposal to alter
	 * @param progId The new programme id of the proposal
	 * @throws RemoteException
	 */
	public void changeProgrammeOfProposal(long proposalId, long progId) throws RemoteException; 
	
	/**
	 * Removes a group from the database.
	 * 
	 * @param groupID
	 *            The ID of the group.
	 * @throws RemoteException
	 *             if the group cannot be found or deleted.
	 */
	public void deleteGroup(long groupID) throws RemoteException;
	
	/**
	 * Removes a programme from the database.
	 * 
	 * @param programID
	 *            The ID of the programme.
	 * @throws RemoteException
	 *             if the program cannot be found or deleted.
	 */
	public void deleteProgramme(long programID) throws RemoteException;

	/**
	 * Removes a proposal from the database.
	 * 
	 * @param propID
	 *            The ID of the proposal.
	 * @throws RemoteException
	 *             if the proposal cannot be found or deleted.
	 */
	public void deleteProposal(long propID) throws RemoteException;

	/**
     * Delete a TAG.
     * @param tagID The TAG's ID.
     * @throws Phase2Exception
     */
    public void deleteTag(long tagID) throws RemoteException;
    
	/**
	 * Removes a target from the database.
	 * 
	 * @param targetID
	 *            The ID of the target.
	 * @throws RemoteException
	 *             if the target cannot be found or deleted.
	 */
	public void deleteTarget(long targetID) throws RemoteException;

	/**
	 * Removes a config from the database.
	 * 
	 * @param configID
	 *            The ID of the Config.
	 * @throws RemoteException
	 *             If the config cannot be found or deleted.
	 */
	public void deleteInstrumentConfig(long configID) throws RemoteException;

	/**
	 * Return the group with the specified name in the proposal
	 * @param name
	 * @param proposalId
	 * @return
	 * @throws RemoteException
	 */
	public long findIdOfGroupInProposal(String name, long proposalId) throws RemoteException;
	
	 /** Return the tag with the specified name.
     * @param name The tag whose name we know.
     * @return The tag entry.
     */
    public ITag findTag(String name) throws RemoteException;
    
    /** Return the target with the specified name on the specified programme.
     * @param programId The ID of the program which contains the target
     * @param name The target name
     * @return The tag entry.
     */
    public ITarget findTarget(long programId, String targetName)  throws RemoteException;
    
    /**
     * Find a named program (note that names are mutable).
     * @param name
     * @return
     * @throws RemoteException
     */
    public IProgram findProgram(String name) throws RemoteException;
    
     /**
     * Find a named proposal (note that names are mutable).
     * @param name
     * @return
     * @throws RemoteException
     */
    public IProposal findProposal(String name) throws RemoteException;
    
    /**
     * Returns whether a group has an Observation Sequence associated with it
     * @param groupID
     * @return
     * @throws RemoteException
     */
    public boolean groupHasObservationSequence(long groupID) throws RemoteException;
    
    /**
     * Returns whether a proposal with the given name exists on the programme with the given prog id
     * @param proposalName
     * @param progId
     * @return
     * @throws RemoteException
     */
    public boolean proposalExists(String proposalName, long progId) throws RemoteException;
    
    /**
     * Returns whether a group with the given name exists on the proposal with the given proposal id
     * @param groupName
     * @param progId
     * @return
     * @throws RemoteException
     */
    public boolean groupExists(String groupName, long proposalId) throws RemoteException;
    
    /**
     * 
     * @param gid
     * @return
     * @throws RemoteException
     */
    public long findProposalIdOfGroup(long gid) throws RemoteException;
    
	/**
	 * Returns a group with the specified ID.
	 * 
	 * @param groupID
	 *            The ID of the group.
	 * @throws RemoteException
	 *             if the group cannot be found.
	 */
	public IGroup getGroup(long groupID) throws RemoteException;

	/**
	 * Returns the number of active | inactive groups in a proposal
	 * @param proposalID The id of the proposal to interrogate
	 * @param active Want active | inactive group count
	 * @return number active | inactive
	 * @throws RemoteException
	 */
	public int getNumberOfGroups(long proposalID, boolean active) throws RemoteException;
	
	/**
	 * Returns a observation sequence with the specified ID.
	 * @param sequenceID The ID of the observation sequence.
	 * @return The observation sequence.
	 * @throws RemoteException if the sequence cannot be found.
	 */
	public ISequenceComponent getObservationSequence(long sequenceID) throws RemoteException;

	/**
	 * Returns a observation sequence of the group with the specified ID.
	 * @param groupID The ID of the group to which the sequence belongs.
	 * @return The observation sequence.
	 * @throws RemoteException if the sequence cannot be found.
	 */
	public ISequenceComponent getObservationSequenceOfGroup(long groupID) throws RemoteException;
	
	/**
	 * Returns a program with the specified ID.
	 * 
	 * @param programID
	 *            The ID of the program.
	 * @throws RemoteException
	 *             if the program cannot be found..
	 */
	public IProgram getProgramme(long programID) throws RemoteException;

	/**
	 * Get the program containing the specified proposal
	 * @param propID
	 * @return
	 * @throws RemoteException
	 */
    public IProgram getProgrammeOfProposal(long propID) throws RemoteException;
    
    /**
     * Get the program containing the specified group
     * @param groupID
     * @return
     * @throws RemoteException
     */
    public IProgram getProgrammeOfGroup(long groupID) throws RemoteException;
    
	/**
	 * Returns a proposal with the specified ID.
	 * 
	 * @param propID
	 *            The ID of the proposal.
	 * @throws RemoteException
	 *             if the proposal cannot be found.
	 */
	public IProposal getProposal(long propID) throws RemoteException;

	/**
	 * Get the proposal containing the specified group
	 * @param groupID
	 * @return
	 * @throws RemoteException
	 */
	public IProposal getProposalOfGroup(long groupID) throws RemoteException;
	
	/**
	 * Returns a TAG with the specified ID.
	 * 
	 * @param tagID
	 *            The ID of the TAG.
	 * @throws RemoteException
	 *             if the TAG cannot be found.
	 */
	public ITag getTag(long tagID) throws RemoteException;

	/** Implementors should return the ID of the tag who owns (is PI) for the specified proposal.
     * @param proposalID The proposal for which the owner is required.
     * @return The ID of the proposal's owner.
     * @throws Phase2Exception
     */ 
    public ITag getTagOfProposal(long propId) throws RemoteException;	
    
    /**
     * 
     * @param tid
     * @return
     * @throws Exception
     */
    public ITarget getTarget(long tid) throws RemoteException;
    
    /**
     * 
     * @param cid
     * @return
     * @throws Exception
     */
	public IInstrumentConfig getInstrumentConfig(long cid) throws RemoteException;
    	
	/**
	 * Returns a list of all groups belonging to the specified proposal
	 * that are either active or inactive
	 * @param propID
	 *            The ID of the proposal.
	 * @param includeInactiveGroups 
	 * 			   If true inactive groups are also shown alongside active ones, else only active groups are displayed            
	 * @throws RemoteException
	 *             if the proposal cannot be found.
	 */
	public List listGroups(long propID, boolean includeInactiveGroups) throws RemoteException;
	
	/**
	 * Returns a list of all groups that have a fixed timing constraint, and that are alive in the P2
	 * @throws RemoteException
	 */
    public List listActiveFixedGroups() throws RemoteException;
    
	/**
	 * Returns a list of all groups belonging to the specified proposal
	 * that are active and have not expired.
	 * @param propID
	 *            The ID of the proposal.
	 * @throws RemoteException
	 */
    public List listActiveUnexpiredGroups(long propID) throws RemoteException;
	
	/**
	 * Returns a list of all the groups who's observation sequences have as a component the target specified
	 * @param target The target to search for
	 * @return List of IGroups
	 * @throws RemoteException if something goes wrong
	 */
	public List listGroupsUsingTarget(ITarget target) throws RemoteException;
	
	/**
	 * Returns a list of all the groups which contain timing constraints of the specified type
	 * @param timingConstraintType one of GroupTypes.*
	 * @return List of IGroups
	 * @throws RemoteException if something goes wrong
	 */
	public List listGroupsWithTimingConstraintOfType(int timingConstraintType) throws RemoteException;
	
	/**
	 * Returns a list of all the groups who's observation sequences have as a component the instrument config specified
	 * @param instrumentConfig The instrument config to search for
	 * @return List of IGroups
	 * @throws RemoteException if something goes wrong
	 */
	public List listGroupsUsingInstrumentConfig(IInstrumentConfig instrumentConfig) throws RemoteException;
	 
	
	/**
	 * Returns a list of configs belonging to the specified proposal .
	 * 
	 * @param progID
	 *            The ID of the programme.
	 * @throws RemoteException
	 *             if the proposal cannot be found.
	 */
	
	/* NOT USED:
	 * public List listInstrumentConfigs() throws RemoteException;
	 */
	
	/**
	 * Returns a list of revisions belonging to the specified proposal .
	 * 
	 * @param propID
	 *            The ID of the proposal.
	 * @throws RemoteException
	 *             if the proposal cannot be found.
	 */
	public List listRevisions(long propID) throws RemoteException;
	
	/** Implementors should return a list of Proposals for the specified program.
     * The time covered by the proposal is identified by its activation and expiry dates.
     * @param progID the ID of the program.
     * @return A list of access permissions.
     * @throws Phase2Exception
     */
    public List listProposalsOfProgramme(long progID) throws RemoteException;
    
    /** Implementors should return a list of Proposals for the specified tag.
     * The time covered by the proposal is identified by its activation and expiry dates.
     * @param tagID the ID of the tag.
     * @return A list of access permissions.
     * @throws Phase2Exception
     */
    public List listProposalsOfTag(long tagID) throws RemoteException;
    
    /**
     * Lists the names of all proposals in the system
     * @return List of Strings
     * @throws RemoteException
     */
    public List listProposalNames(boolean limitToProposalsWithoutPIs) throws RemoteException;
    
	/**
	 * Returns a list of programs.
	 * @return list of programmes
	 * @throws RemoteException
	 */
	public List listProgrammes() throws RemoteException;
		
	/**
	 * Returns a list of programmes that the user belongs to
	 * @param uid
	 * @return 
	 * @throws RemoteException
	 */
	public List listProgrammesOfUser(long uid) throws RemoteException;
	
	/**
	 * Returns a list of TAGs
	 * @return list of Tags
	 * @throws RemoteException
	 */
	public List listTags() throws RemoteException;

	/**
	 * Returns a list of targets belonging to the specified programme .
	 * 
	 * @param progID
	 *            The ID of the programme.
	 * @throws RemoteException
	 *             if the programme cannot be found..
	 */
	public List listTargets(long progID) throws RemoteException;
	
	/**
	 * Returns a complete list of all Timing Constraints
	 * @return List of ITimingConstraints
	 * @throws RemoteException if problem
	 */
	public List listTimingConstraints()  throws RemoteException;
	
	/**
	 * Returns a complete list of all Timing Constraints of the specified type
	 * @param type the specified type of timing constraint required
	 * @return List of ITimingConstraints
	 * @throws RemoteException if problem
	 */
	public List listTimingConstraintsOfType(int type)  throws RemoteException;
	
	/**
	 * Returns a complete list of all Timing Constraints of the specified type who's endTime is after (>=) cutOffTime
	 * @param type the specified type of timing constraint required
	 * @param cutOffTime the cut off time, we only want timing constraints with an end time after this (or equal to this) time 
	 * @return List of ITimingConstraints
	 * @throws RemoteException if problem
	 */
	public List listTimingConstraintsOfTypeEndingAfter(int type, long cutOffTime)  throws RemoteException;
	
	/**
	 * Returns a list of instrument configs belonging to a specified programme
	 * @param progID
	 * @return List of InstrumentConfigs
	 * @throws RemoteException
	 */
	public List listInstrumentConfigs(long progID) throws RemoteException;

	/**
	 * Updates the details in the database for a group.
	 * 
	 * @param group
	 *            the group details to update.
	 * @param keyID
	 *            key to access the group.
	 * @throws RemoteException
	 *             if the group cannot be found or updated.
	 */
	public void updateGroup(IGroup group, long keyID) throws RemoteException;
	
	/**
	 * Updates whether the group with the specified id is urgent or not
	 * @param groupId The id of the group
	 * @param isUrgent Whether the group should be urgent or not
	 * @param keyID The key used to update the group
	 * @throws RemoteException if the update is a failure
	 */
	public void updateGroupUrgency(long groupId, boolean isUrgent, long keyID) throws RemoteException;
	
	/**
	 * Updates the details in the database for a specific instrument config which is part of a sequence.
	 * 
	 * 
	 * @param config
	 *            the config details to update.
	 * @param keyID
	 *            key to access the config.
	 * @throws RemoteException
	 *             if the config cannot be found or updated.
	 */
	public void updateInstrumentConfig(IInstrumentConfig instConfig, long keyID) throws RemoteException;
	
	/**
	 * 
	 * @param groupID
	 * @param sequence
	 * @param keyID
	 * @throws RemoteException
	 */
	
	public void updateObservationSequenceOfGroup(long groupID, ISequenceComponent sequence, long keyID) throws RemoteException;
	
	/**
	 * Deleted the observation sequence of group with specified id
	 * @param groupID
	 * @throws RemoteException
	 */
	public void deleteObservationSequenceOfGroup(long groupID) throws RemoteException;
	
	/**
	 * Updates the details in the database for a program.
	 * 
	 * @param program
	 *            the program details to update.
	 * @param keyID
	 *            key to access the program.
	 * @throws RemoteException
	 *             if the program cannot be found or updated.
	 */
	public void updateProgramme(IProgram program, long keyID) throws RemoteException;

	/**
	 * Updates the details in the database for a proposal.
	 * 
	 * @param proposal
	 *            the proposal details to update.
	 * @param keyID
	 *            key to access the proposal.
	 * @throws RemoteException
	 *             if the proposal cannot be found or updated.
	 */
	public void updateProposal(IProposal prop, long keyId) throws RemoteException;

	/**
	 * Updates the details in the database for a TAG.
	 * 
	 * @param tag
	 *            the TAG details to update.
	 * @param keyID
	 *            key to access the TAG.
	 * @throws RemoteException
	 *             if the TAG cannot be found or updated..
	 */
	public void updateTag(ITag tag, long keyID) throws RemoteException;

	/**
	 * 
	 * @param config
	 * @param keyId
	 * @throws Exception
	 */
	public void updateDetectorConfig(IInstrumentConfig config, long keyId) throws Exception;
	/**
	 * Updates the details in the database for a target.
	 * 
	 * @param target
	 *            the target details to update.
	 * @param keyID
	 *            key to access the target.
	 * @throws RemoteException
	 *             if the target cannot be found or updated.
	 */
	public void updateTarget(ITarget target, long keyID) throws RemoteException;

    /**
     * Returns a list of available linkages between groups in the specified proposal.
    * @param proposalID The ID of the proposal.
     * @return list of linkages
     * @throws RemoteException
     */
    public List listLinkages(long proposalID) throws RemoteException;
    
    
}
