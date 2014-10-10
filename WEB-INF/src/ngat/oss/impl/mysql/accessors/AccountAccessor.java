package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.TransactionTypes;
import ngat.oss.reference.AccountModelTypes;
import ngat.phase2.IAccessPermission;
import ngat.phase2.IAccount;
import ngat.phase2.IProgram;
import ngat.phase2.IProposal;
import ngat.phase2.ISemester;
import ngat.phase2.ITag;
import ngat.phase2.IUser;
import ngat.phase2.XAccount;
import ngat.phase2.XTransaction;
import ngat.phase2.util.SemesterAccountEntry;

import org.apache.log4j.Logger;

public class AccountAccessor {
	
	/*	
	ACCOUNT;
		 id					int
		 sid				int
		 ownerType	int
		 ownerId		int
		 allocated		double
		 consumed	double
	*/
	
	static Logger logger = Logger.getLogger(AccountAccessor.class);
	
	/*
	 * The account type that this accessor is associated with.
	 */
	private int accountType;
	
	//statements
	
	public static final String INSERT_ACCOUNT_SQL = 						
		"insert into ACCOUNT (" +
		"sid, ownerType, ownerId, allocated, consumed, chargeable" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?)";

	public static final String GET_ACCOUNT_SQL = 							
		"select " +
		"id, sid, ownerType, ownerId, allocated, consumed, chargeable " +
		"from " +
		"ACCOUNT " +
		"where id=?";
	
	public static final String GET_ACCOUNT_OWNER_ID_SQL = 							
		"select " +
		"ownerId " +
		"from " +
		"ACCOUNT " +
		"where id=?";
	
	public static final String FIND_ACCOUNT_SQL = 							
	    "select " + 
	    "id, sid, ownerType, ownerId, allocated, consumed, chargeable " +
	    "from ACCOUNT "+
	    "where ownerId=? "+
	    "and sid=? "+
	    "and ownerType=?";
	
	public static final String LIST_ACCOUNTS_OF_SEMESTER_SQL = 							
		"select " +
		"id, sid, ownerType, ownerId, allocated, consumed, chargeable " +
		"from " +
		"ACCOUNT " +
		"where sid=? " + 
		"and ownerType=?";
	
	public static final String LIST_SIDS_OF_OWNER_SQL =
		"select " +
		"distinct sid " +//distinct, because it's a sid for each account otherwise.
		"from " +
		"ACCOUNT " +
		"where ownerId=? " +
		"and ownerType=? ";
	
	public static final String DEL_ACCOUNT_SQL = 							
		"delete from ACCOUNT where id = ?";
	
	public static final String DEL_ACCOUNTS_OF_OWNER_SQL = 							
		"delete from ACCOUNT where oid = ? and ownerType = ?";
	
	public static final String UPDATE_ACCOUNT_SQL =					
		"update ACCOUNT "+
		"set " +
		"sid=?,"+
		"ownerType=?,"+
		"ownerId=?,"+
		"allocated=?,"+
		"consumed=?,"+
		"chargeable=?,"+
		"where id=?";

	public static final String UPDATE_ACCOUNT_CONSUMED_SQL =					
		"update ACCOUNT "+
		"set " +
		"consumed=consumed + ? "+
		"where id=?";
	
	public static final String UPDATE_ACCOUNT_ALLOCATED_SQL =					
		"update ACCOUNT "+
		"set " +
		"allocated=allocated + ? "+
		"where id=?";
	
	/**
	 * 
	 * @param accountType one of AccountModelTypes.PROPOSAL_ACCOUNT_TYPE | AccountModelTypes.TAG_ACCOUNT_TYPE
	 */
	public AccountAccessor(int accountType) {
		this.accountType = accountType;
	}
	
	/** Public static methods *******************************************************************/
	/*
	public static ArrayList getAccountDescriptionsList() {
		
		ArrayList<AccountDescription> accountDescriptions = new ArrayList<AccountDescription>();
			accountDescriptions.add(new AccountDescription(AccountTypes.ALLOCATION_NAME, AccountTypes.ALLOCATION_DESC));
			
		return accountDescriptions;
	}
	*/
	
	/** Public methods *******************************************************************/
	public long addAccount(Connection connection, long ownerId, long semesterId, IAccount account) throws Exception {
		PreparedStatement stmt = null; 
		try {
			//prepare statement
			stmt = connection.prepareStatement(INSERT_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, semesterId);
			stmt.setInt(2, accountType);
			stmt.setLong(3, ownerId);
			//stmt.setString(4, account.getName());
			//stmt.setString(5, account.getDescription());
			stmt.setDouble(4, account.getAllocated());
			stmt.setDouble(5, account.getConsumed());
			stmt.setBoolean(6, account.isChargeable());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_ACCOUNT_SQL, true);
			return id;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public void addAllAccountsBetweenSemesters(Connection connection, long ownerId, long startSemesterId, long endSemesterId) throws Exception {
		//ArrayList accountDescriptions = AccountAccessor.getAccountDescriptionsList();
		
		for (int semesterId = (int)startSemesterId;  semesterId <= (endSemesterId); semesterId++) {
			//Iterator sdi = accountDescriptions.iterator();
			//while (sdi.hasNext()) {
				//AccountDescription seeingDescription = (AccountDescription)sdi.next();
				//String accountName = seeingDescription.getName();
				//String accountDescription = seeingDescription.getDescription();
				XAccount account = new XAccount();
				account.setChargeable(true);
				//account.setName(accountName);
				//account.setDescription(accountDescription);
				addAccount(connection, ownerId, semesterId, account);
			//}	
		}
	}
	
	public void deleteAccount(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_ACCOUNT_SQL, true);
			if (numRows ==0) {
				throw new Exception("No rows updated");
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	

	public long modifyAllocation(Connection connection, long accountID, double amount, String comment, String clientRef) throws Exception  {
		PreparedStatement stmt = null;
		try {
			XTransaction transaction = new XTransaction(new Date().getTime(), amount, clientRef, comment, TransactionTypes.ALLOCATION_TIME_TRANSACTION);
			TransactionAccessor transactionAccessor = new TransactionAccessor();
			long transactionId = transactionAccessor.addTransaction(connection, accountID, transaction);
			
			stmt = connection.prepareStatement(UPDATE_ACCOUNT_ALLOCATED_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, amount);
			stmt.setLong(2, accountID);
			
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_ACCOUNT_ALLOCATED_SQL, false);
			if (numRows ==0) {
				throw new Phase2Exception("No rows updated");
			}
			return transactionId;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}

	public long modifyConsumed(Connection connection, long accountID, double amount, String comment, String clientRef) throws Exception  {
		PreparedStatement stmt = null;
		try {
			XTransaction transaction = new XTransaction(new Date().getTime(), amount, clientRef, comment, TransactionTypes.CONSUMED_TIME_TRANSACTION);
			TransactionAccessor transactionAccessor = new TransactionAccessor();
			long transactionId = transactionAccessor.addTransaction(connection, accountID, transaction);
			
			stmt = connection.prepareStatement(UPDATE_ACCOUNT_CONSUMED_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, amount);
			stmt.setLong(2, accountID);
			
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_ACCOUNT_CONSUMED_SQL, false);
			if (numRows ==0) {
				throw new Phase2Exception("No rows updated");
			}
			return transactionId;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public void deleteAccountsOfOwner(Connection connection, long oid) throws Exception  {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_ACCOUNTS_OF_OWNER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, oid);
			stmt.setInt(2, accountType);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_ACCOUNTS_OF_OWNER_SQL, false);
			if (numRows ==0) {
				throw new Phase2Exception("No rows updated");
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public IAccount getAccount(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_ACCOUNT_SQL);
			IAccount account = null;
			if (resultSet.next()) {
				account = getAccountFromResultSetCursor(resultSet);
			}
			return account;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public long getAccountOwnerID(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_ACCOUNT_OWNER_ID_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_ACCOUNT_OWNER_ID_SQL);
			long aoid = -1;
			if (resultSet.next()) {
				aoid = resultSet.getLong(1);
			} else {
				throw new Exception("No owner of account : " + id  + ", type = " + accountType);
			}
			return aoid;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public IAccount findAccount(Connection connection, long ownerId, long semesterId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(FIND_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, ownerId);
			stmt.setLong(2, semesterId);
			stmt.setInt(3, accountType);
	
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, FIND_ACCOUNT_SQL);
			IAccount account = null;
			if (resultSet.next()) {
				account = getAccountFromResultSetCursor(resultSet);
			}
			return account;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public List listAccountsOfSemester(Connection connection, long semesterId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {

			stmt = connection.prepareStatement(LIST_ACCOUNTS_OF_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, semesterId);
			stmt.setLong(2, accountType);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_ACCOUNTS_OF_SEMESTER_SQL);
			ArrayList list = new ArrayList<IAccount>();
			while (resultSet.next()) {
				IAccount account = getAccountFromResultSetCursor(resultSet);
				list.add(account);
			}
			return list;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public List listSemestersForWhichOwnerHasAccounts(Connection connection, long ownerId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LIST_SIDS_OF_OWNER_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, ownerId);
			stmt.setLong(2, accountType);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_SIDS_OF_OWNER_SQL);
			
			SemesterAccessor semesterAccessor = new SemesterAccessor();
			
			ArrayList list = new ArrayList<ISemester>();
			while (resultSet.next()) {
				long sid = resultSet.getLong(1);
				ISemester semester = semesterAccessor.getSemester(connection, sid);
				list.add(semester);
			}
			return list;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	//utility method to return account line entries to the gui
	public List listAccountEntriesOfSemester(Connection connection, long semesterId) throws Exception {
		
		ProposalAccessor proposalAccessor = new ProposalAccessor();
		TagAccessor tagAccessor = new TagAccessor();
		ProgrammeAccessor programmeAccessor = new ProgrammeAccessor();
		AccessPermissionAccessor accessPermissionAccessor = new AccessPermissionAccessor();
		UserAccessor userAccessor = new UserAccessor();
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			
			stmt = connection.prepareStatement(LIST_ACCOUNTS_OF_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, semesterId);
			stmt.setLong(2, accountType);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_ACCOUNTS_OF_SEMESTER_SQL);
			ArrayList semesterAccountEntriesList = new ArrayList<SemesterAccountEntry>();
			while (resultSet.next()) {
				// id, sid, ownerType, ownerId, allocated, consumed, chargeable 
				long accountId = resultSet.getLong(1);
				long ownerId = resultSet.getLong(4); 
				//String accountName = resultSet.getString(5);//needed
				double allocated = resultSet.getDouble(5);//needed
				double consumed = resultSet.getDouble(6); //needed
				IAccount account = makeAccountFromFields(accountId, semesterId, this.accountType, ownerId, allocated, consumed, true);
				if (this.accountType == AccountModelTypes.PROPOSAL_ACCOUNT_TYPE) {
					//dealing with proposal accounts of the semester
					IProposal proposal = proposalAccessor.getProposal(connection, ownerId);
					if (proposal != null) {
						String proposalName = proposal.getName(); //needed
						long programmeId = proposalAccessor.getProgrammeIdOfProposal(connection, proposal.getID());
						IProgram program = programmeAccessor.getProgramme(connection, programmeId);
						String programmeName = program.getName(); //needed
						
						List accessPermissionsOfProposal = accessPermissionAccessor.listUserPermissionsOnProposal(connection,proposal.getID());
						Iterator i = accessPermissionsOfProposal.iterator();
						IAccessPermission piAccessPermission = null;
						while (i.hasNext()) {
							IAccessPermission accessPermission = (IAccessPermission) i.next();
							if (accessPermission.getUserRole() == IAccessPermission.PRINCIPLE_INVESTIGATOR_ROLE) {
								piAccessPermission = accessPermission;
							}
						}
						if (piAccessPermission != null) {
							long userId = piAccessPermission.getUserID();
							IUser user = userAccessor.getUser(connection, userId);
							String piName = user.getName();
							SemesterAccountEntry semesterAccountEntry = new SemesterAccountEntry(account, proposalName, programmeName, piName);
							semesterAccountEntriesList.add(semesterAccountEntry);
						} else {
							String piName = "n/a";
							SemesterAccountEntry semesterAccountEntry = new SemesterAccountEntry(account, proposalName, programmeName, piName);
							semesterAccountEntriesList.add(semesterAccountEntry);
						}
					}
				} else {
					//dealing with tag accounts of the semester
					ITag tag = tagAccessor.getTag(connection, ownerId);
					String tagName = tag.getName();
					
					SemesterAccountEntry semesterAccountEntry = new SemesterAccountEntry(account, tagName, null, null);
					semesterAccountEntriesList.add(semesterAccountEntry);
				}
			}
			return semesterAccountEntriesList;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	/* Private  methods ******************************************************************/

	/**
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	
	private IAccount makeAccountFromFields(long id, long sid, int ownerType, long ownerId, double allocated, double consumed, boolean chargeable) {
		
		XAccount account = new XAccount();
		account.setID(id);
		//account.setName(name);
		//account.setDescription(description);
		account.setAllocated(allocated);
		account.setChargeable(chargeable);
		account.setConsumed(consumed);
		
		return account;
	}
	
	private IAccount getAccountFromResultSetCursor(ResultSet resultSet) throws Exception {

		/*
		public static final String LIST_ACCOUNTS_OF_OWNER_SQL = 							
		"select " +
		"id, sid, ownerType, ownerId, allocated, consumed, chargeable " +
		"from " +
		"ACCOUNT " +
		"where ownerId=? " +
		"and sid=? " + 
		"and ownerType=?";
		 */
		
		XAccount account = new XAccount();
	
		// id, sid, ownerType, ownerId, allocated, consumed, chargeable
		long id;
		long sid;;
		int ownerType;
		long ownerId;
		
		double allocated;
		double consumed;
		boolean chargeable;
		
		id = resultSet.getLong(1);
		sid = resultSet.getLong(2);
		ownerType = resultSet.getInt(3);
		ownerId = resultSet.getLong(4);
		allocated = resultSet.getDouble(5);
		consumed = resultSet.getDouble(6);
		chargeable = resultSet.getBoolean(7);
		
		account.setID(id);
		account.setAllocated(allocated);
		account.setChargeable(chargeable);
		account.setConsumed(consumed);
		
		return account;
	}	
}
