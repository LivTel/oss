package ngat.oss.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.phase2.IAccessPermission;
import ngat.phase2.ILoginRecord;
import ngat.phase2.IUser;
import ngat.phase2.IVersion;

/** Provides access permission information for access to the Phase2 and Accounting databases.*/
public interface IAccessModel extends Remote {

	/** 
	 * @return version The minimum version number of a client that is allowed to access the services.
	 * @throws RemoteException
	 */
	public IVersion getMinimumClientVersionNumber() throws RemoteException;
	
	/**
	 *  Implementors should return the id of the user who is the PI on this proposal
	 * @param proposalID 
	 * @return The id of the PI user
	 * @throws RemoteException
	 */
	public long getProposalPI(long proposalID) throws RemoteException;
	
	/** Implementors should return a list of AccessPermissions for the specified user.
	 * @param userID The user for which access permissions are required.
	 * @return A list of access permissions.
	 * @throws RemoteException
	 */
	public List listAccessPermissionsOfUser(long userID) throws RemoteException;

	/** Implementors should return a list of AccessPermissions for the specified  proposal.
	 * @param proposalID The proposal for which access permissions are required.
	 * @return A list of access permissions.
	 * @throws RemoteException
	 */
	public List listAccessPermissionsOnProposal(long proposalID) throws RemoteException;

    /** Implementors should return an AccessPermission for the speciifed user on the specified proposal.
     *  This should be null if the user has no access permission for the specified proposal.
     * @param userID The user for which access permissions are required.
     * @param proposalID The proposal for which access permissions are required.
     * @throws RemoteException
     */
    public IAccessPermission getAccessPermission(long userID, long proposalID) throws RemoteException;

    /** Return the User with the specified name.
     * @param name The user whose name we know.
     * @return The user entry.
     */
    public IUser findUser(String name) throws RemoteException;
	
    /**
     * Return whether a user with that userName exists already
     * @param name
     * @return 
     * @throws RemoteException
     */
    public boolean userExists(String name) throws RemoteException;
    
    /**Implementors should return a list of all Users.
     * @return A list of Users.
     * @throws RemoteException
     */
    public List listUsers() throws RemoteException;
	
    /** Implementors should return the User identified by the ID.
     * @param userID The ID of the User.
     * @return The User specified by userID.
     * @throws RemoteException
     */
    public IUser getUser(long userID) throws RemoteException;
    
    /** Implementors should return the User if the username and password supplied are valid and authenticated
     * @param username The supplied username.
     * @param password Their password
     * @param ignoreThis literally ignore this, it is here just to change the RPC specification of the authenticate method
     * @return Ths User object or null
     * @throws RemoteException
     */
    public IUser authenticate(String username, String password, String ignoreThis) throws RemoteException;
    
    /** Add a new permission.*/
    public long addPermission(IAccessPermission perm) throws RemoteException;

    /** Revoke a specified permission (by aid).*/
    public void revokePermission(long aid) throws RemoteException;

    /** Update the supplied permission.*/
    public void updatePermission(IAccessPermission perm) throws RemoteException;
    
    /** Add a new user.*/
    public long addUser(IUser user) throws RemoteException;

    /** Delete a user.*/
    public void deleteUser(long uid) throws RemoteException;

    /** Update a user's details.*/
    public void updateUser(IUser user) throws RemoteException;

    /** receive the details of the client type (i.e. OS, Java version) invoking the service **/
    public void receiveLoginRecord(ILoginRecord loginRecord) throws RemoteException;
    
}
