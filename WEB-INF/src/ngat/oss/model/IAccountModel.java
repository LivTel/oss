package ngat.oss.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import ngat.phase2.IAccount;
import ngat.phase2.ISemester;
import ngat.phase2.ISemesterPeriod;
import ngat.phase2.ITransaction;

/**
 * @author snf / nrc
 * 
 */
public interface IAccountModel extends Remote {

	/**
	 * Creates a new account in the database.
	 * 
	 * @param ownerId
	 *            the ID account owner (prop ID or tag ID etc )
	 * @param semesterId
	 *            the ID of the semester that the account pertains to
	 * @param account
	 *            the new account to insert in the database.
	 * 
	 * @return the ID of the new account.
	 * @throws RemoteException
	 *             if the account cannot be created in the database.
	 */
	public long addAccount(long ownerId, long semesterId, IAccount account) throws RemoteException;

	/**
	 * Adds all the accounts for the owner for all the semesters between startSemesterId and endSemesterId
	 * @param ownerId
	 * @param startSemesterId
	 * @param endSemesterId
	 * @throws RemoteException
	 */
	public void addAllAccountsBetweenSemesters(long ownerId, long startSemesterId, long endSemesterId) throws RemoteException;
	
	/**
	 * 
	 * @param accountID
	 * @param amount
	 * @param comment
	 * @param clientRef
	 * @return
	 */
	public long modifyAllocation(long accountID, double amount, String comment, String clientRef) throws RemoteException;
	
	/**
	 * 
	 * @param accountID
	 * @param amount
	 * @param comment
	 * @param clientRef
	 * @return
	 */
	public long modifyConsumed(long accountID, double amount, String comment, String clientRef) throws RemoteException;

	/**
	 * Removes an account from the database.
	 * 
	 * @param accountID
	 *            The ID of the account.
	 * @throws RemoteException
	 *             if the account cannot be found or deleted.
	 */
	public void deleteAccount(long accountID) throws RemoteException;

	/**
	 * Returns an account with the specified ID.
	 * 
	 * @param accountID
	 *            The ID of the account.
	 * @throws RemoteException
	 *             if the account cannot be found.
	 */
	public IAccount getAccount(long accountID) throws RemoteException;

	/**
	 * Returns the id of the owner of the account (i.e. the id of the TAG or Proposal related to the account)
	 * 
	 * @param accountID
	 *            The ID of the account.
	 * @throws RemoteException
	 *             if the account cannot be found.
	 */
	public long getAccountOwnerID(long accountID) throws RemoteException;

	/**
	 * Returns the semester with the given id
	 * @param id if of required semester
	 * @return the semester object
	 * @throws RemoteException
	 */
	public ISemester getSemester(long id)  throws RemoteException;
	
	/**
	 * Returns the semester that a given date is within
	 * @param dateTime The date within the required semester
	 * @return the semester objects which overlap this date, wrapped in a SemesterPeriod object
	 * @throws RemoteException if objects shaped like pears are located anywhere within the operation
	 */
	public ISemesterPeriod getSemesterPeriodOfDate(long dateTime)  throws RemoteException;
	
	/**
	 * Returns an account that adheres to the specified parameters
	 * @param ownerId
	 * 				The id of the owner of the account
	 * @param semesterId
	 * 				The id of the semester of the account
	 * @return
	 * 				The required account
	 * @throws RemoteException
	 */
	public IAccount findAccount(long ownerId, long semesterId) throws RemoteException;
	
	/**
	 * Returns a transaction with the specified ID
	 * 
	 * @param transID
	 *            The ID of the transaction.
	 * @throws RemoteException
	 *             if the transaction cannot be found.
	 */
	public ITransaction getTransaction(long transID) throws RemoteException;

	/**
	 * Returns a list of accounts relating to the specified semester.
	 * 
	 * @param semesterId
	 * 				The ID of the semester 
	 * @return
	 * 				List containing the required accounts
	 * @throws RemoteException
	 *             if the owner is unknown.
	 */
	public List listAccountsOfSemester(long semesterId) throws RemoteException;
	
	/**
	 * Returns a list of accounts relating to the specified semester.
	 * 
	 * @param semesterId
	 * 				The ID of the semester 
	 * @return
	 * 				List containing the required SemesterAccountEntry objects
	 * @throws RemoteException
	 *             if it goes pear
	 */
	public List listAccountEntriesOfSemester(long semesterId) throws Exception;
	
	/**
	 * Returns a list of semesters, the first one of which contains the specified date-time
	 * the last one is the last stored semester
	 * @param dateTime The start time
	 * @return List of semesters
	 * @throws RemoteException
	 */
	public List listSemestersFromDate(long dateTime) throws RemoteException;
	
	/**
	 * Returns a list of semesters for which the owner has accounts
	 * @param ownerId
	 *				The ID of the owner.
	 * @return
	 * 				List containing the required semesters
	 */
	public List listSemestersForWhichOwnerHasAccounts(long ownerId) throws RemoteException;
	
	/**
	 * Returns a list of transactions for the specified accounting period.
	 * 
	 * @param accountID
	 *            The ID of the account.
	 * @throws RemoteException
	 *             if the account cannot be found.
	 */
	public List listTransactions(long accountID) throws RemoteException;
	
	/**
	 * 
	 * @param ownerID
	 * @throws RemoteException
	 */
	public void deleteAccountsOfOwner(long ownerID) throws RemoteException;
	
	/**
	 * 
	 * @param id
	 * @throws RemoteException
	 */
	public void deleteSemester(long id)  throws RemoteException;
	
	/**
	 * 
	 * @param aid
	 * @throws RemoteException
	 */
	public void deleteAccountTransactions(long aid) throws RemoteException;
	
	/**
	 * 
	 * @param id
	 * @throws RemoteException
	 */
	public void deleteTransaction(long id)  throws RemoteException;
	
	/**
	 * 
	 * @param semester
	 * @return
	 * @throws RemoteException
	 */
	public long addSemester(ISemester semester) throws RemoteException;
}
