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
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.reference.AccountModelTypes;
import ngat.phase2.IGroup;
import ngat.phase2.IProposal;
import ngat.phase2.ISemester;
import ngat.phase2.XProposal;

import org.apache.log4j.Logger;

public class ProposalAccessor  {
	
	static Logger logger = Logger.getLogger(ProposalAccessor.class);
	
	/*
	 PROPOSAL
	 id 							int
	 pid							int
	 tid							int
	 name 						String
	 title							String
	 priority 						int
	 activation 				Date
	 expiry 						Date 
	 sci 							String
	 allowUrgentGroups	boolean
	 priorityOffset				double
	 enabled					boolean
	 allowFixedGroups  	boolean
	 typeCode					String
	*/
	
	//statements
	public static final String INSERT_PROPOSAL_SQL = 		
		"insert into PROPOSAL (" +
		"pid, tid, name, title, priority, activation, expiry, sci, allowUrgentGroups, priorityOffset, enabled, allowFixedGroups, typeCode" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?, ?, ?, ? , ? , ?, ?, ?)";
	
	public static final String GET_PROPOSAL_SQL = 							
		"select " +
		"id, pid, tid, name, title, priority, activation, expiry, sci, allowUrgentGroups, priorityOffset, enabled, allowFixedGroups, typeCode " +
		"from " +
		"PROPOSAL " +
		"where id=?";
	
	public static final String GET_PROPOSAL_ID_SQL = 							
		"select " +
		"id " +
		"from " +
		"PROPOSAL " +
		"where pid=? " + 
		"and name=?";
	
	public static final String GET_PROPOSAL_ID_OF_GROUP_SQL = 
		"select " +
		"pid " +
		"from " +
		"OBSERVATION_GROUP " +
		"where id=?";
	
	public static final String GET_PROG_ID_OF_PROPOSAL_SQL = 
		"select " +
		"pid " +
		"from " +
		"PROPOSAL " +
		"where id=?";
	
	public static final String GET_TAG_ID_OF_PROPOSAL_SQL = 
		"select " +
		"tid " +
		"from " +
		"PROPOSAL " +
		"where id=?";
	
	public static final String FIND_PROPOSAL_ID_SQL = 							
		"select " +
		"id " +
		"from " +
		"PROPOSAL " +
		"where name=?";
	
	public static final String LIST_PROPOSAL_NAMES_SQL = 
		"select " +
		"id, name " +
		"from " + 
		"PROPOSAL " +
		"order by name";
	
	public static final String LIST_PROPOSAL_SQL_T = 							
		"select " +
		"id, pid, tid, name, title, priority, activation, expiry, sci, allowUrgentGroups, priorityOffset, enabled, allowFixedGroups, typeCode " +
		"from " + 
		"PROPOSAL " + 
		"where tid=? " +
		"order by name";
	
	public static final String LIST_PROPOSAL_SQL_P = 							
		"select " +
		"id, pid, tid, name, title, priority, activation, expiry, sci, allowUrgentGroups, priorityOffset, enabled, allowFixedGroups, typeCode " +
		"from " + 
		"PROPOSAL " + 
		"where pid=? " +
		"order by name";
	
	public static final String DEL_PROPOSAL_SQL = 								
		"delete from PROPOSAL where id = ?";

	public static final String UPDATE_PROPOSAL_SQL =						
		"update PROPOSAL "+
		"set " +
		"name=?," +
		"title=?,"+
		"priority=?," +
		"activation=?," +
		"expiry=?," +
		"sci=?," +
		"allowUrgentGroups=?," +
		"priorityOffset=?," +
		"enabled=?," +
		"allowFixedGroups=?," +
		"typeCode=?"+
		" where id=?";

	public static final String CHANGE_TAG_OF_PROPOSAL_SQL = 
		"update PROPOSAL "+
		"set " +
		"tid=?" +
		" where id=?";
	
	public static final String CHANGE_PROGRAMME_OF_PROPOSAL_SQL = 
		"update PROPOSAL "+
		"set " +
		"pid=?" +
		" where id=?";
	
	/** Public methods *******************************************************************/
	
	public long addProposal(Connection connection, long tagId, long progId, IProposal proposal) throws Exception {
		PreparedStatement stmt = null;
		try {
			//table fields
			String name, sciAbstract, title, typeCode;
			long activationDate, expiryDate;
			int priority;
			double priorityOffset;
			boolean enabled, allowUrgentGroups, allowFixedGroups;
			
			//load values
			name 						= proposal.getName();
			title 							= proposal.getTitle();
			priority 						= proposal.getPriority();
			activationDate 			= proposal.getActivationDate();
			expiryDate 				= proposal.getExpiryDate();
			sciAbstract 				= proposal.getScienceAbstract();
			allowUrgentGroups		= proposal.allowsUrgentGroups();
			priorityOffset				= proposal.getPriorityOffset();
			enabled						= proposal.isEnabled();
			allowFixedGroups		= proposal.allowsFixedGroups();
			typeCode					= proposal.getTypeCode();
			
			//sci, allowUrgentGroups, priorityOffset, enabled
			stmt = connection.prepareStatement(INSERT_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS); 
			stmt.setLong(1, progId);
			stmt.setLong(2, tagId);
			stmt.setString(3, name);
			stmt.setString(4, title);
			stmt.setInt(5, priority);
			stmt.setLong(6, activationDate);
			stmt.setLong(7, expiryDate);
			stmt.setString(8, sciAbstract);
			stmt.setBoolean(9, allowUrgentGroups);
			stmt.setDouble(10, priorityOffset);
			stmt.setBoolean(11, enabled);
			stmt.setBoolean(12, allowFixedGroups);
			stmt.setString(13, typeCode);
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_PROPOSAL_SQL, true);
			
			//dont add any accounts or anything
			
			//add all new accounts for the new proposal for the required semesters
			//i.e. an account for each semester occuring in the life time of the proposal
			//SemesterAccessor semesterAccessor = new SemesterAccessor();
			//ISemester activationSemester = semesterAccessor.getSemesterOfDate(connection, activationDate);
			//ISemester expirySemester = semesterAccessor.getSemesterOfDate(connection, expiryDate);
			/*
			 * no longer do this on proposal addition
			long activationSemesterID = activationSemester.getID();
			long expirySemesterID = expirySemester.getID();
			
			AccountAccessor proposalAccountAccessor = new AccountAccessor(AccountModelTypes.PROPOSAL_ACCOUNT_TYPE);
			proposalAccountAccessor.addAllAccountsBetweenSemesters(connection, id, activationSemesterID, expirySemesterID);
			*/
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
	
	
	
	public void changeTagOfProposal(Connection connection, long proposalId, long tagId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(CHANGE_TAG_OF_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, tagId);
			stmt.setLong(2, proposalId);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, CHANGE_TAG_OF_PROPOSAL_SQL, true);
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
	
	
	public void changeProgrammeOfProposal(Connection connection, long proposalId, long progId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(CHANGE_PROGRAMME_OF_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, progId);
			stmt.setLong(2, proposalId);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, CHANGE_PROGRAMME_OF_PROPOSAL_SQL, true);
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
	
	
	/**
	 * 
	 * @param pid
	 * @throws Exception 
	 * @throws Exception
	 */
	public void deleteProposal(Connection connection, long pid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_PROPOSAL_SQL, true);
			if (numRows ==0) {
				ConnectionPool.getInstance().surrenderConnection(connection);
				throw new Phase2Exception("No rows updated");
			}
			
			//get groups of proposal
			GroupAccessor groupAccessor = new GroupAccessor();
			ArrayList groupsList = groupAccessor.listGroups(connection, pid, true);
			Iterator i = groupsList.iterator();
			while (i.hasNext()) {
				IGroup group = (IGroup)i.next();
				//delete Group (and hence, all objects below group)
				groupAccessor.deleteGroup(connection, group.getID());
			}
	
			//delete AccessPermissions related to Proposal
			new AccessPermissionAccessor().revokeAllPermissionsOfProposal(connection, pid);
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
	
	/**
	 * 
	 * @param pid
	 * @return
	 * @throws SQLException
	 */
	public IProposal getProposal(Connection connection, long pid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_PROPOSAL_SQL);
			XProposal proposal = null;
			if (resultSet.next()) {
				proposal = getProposalFromResultSetCursor(resultSet);
			}
			return proposal;
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
	
	
	public IProposal getProposalOfGroup(Connection connection, long gid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_PROPOSAL_ID_OF_GROUP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_PROPOSAL_ID_OF_GROUP_SQL);
			XProposal proposal = null;
			if (resultSet.next()) {
				long pid = resultSet.getLong(1);
				return getProposal(connection, pid);
			}
			return proposal;
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
	
	public long getProgrammeIdOfProposal(Connection connection, long propId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_PROG_ID_OF_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, propId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_PROG_ID_OF_PROPOSAL_SQL);
			long programmeId = -1;
			if (resultSet.next()) {
				programmeId = resultSet.getLong(1);
			}
			return programmeId;
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
	
	public boolean proposalExists(Connection connection, String proposalName, long progId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_PROPOSAL_ID_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, progId);
			stmt.setString(2, proposalName);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_PROPOSAL_ID_SQL);
			if (resultSet == null) {
				return false;
			}
			return resultSet.next();
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
	
	public long getTagIdOfProposal(Connection connection, long propId) throws Exception {
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_TAG_ID_OF_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, propId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TAG_ID_OF_PROPOSAL_SQL);
			long tagId = -1;
			if (resultSet.next()) {
				tagId = resultSet.getLong(1);
			}
			return tagId;
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
	
	public IProposal findProposal(Connection connection, String name) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(FIND_PROPOSAL_ID_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, FIND_PROPOSAL_ID_SQL);
			if (resultSet.next()) {
				long id = resultSet.getLong(1);
				return getProposal(connection, id);
			}
			return null;
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
	
	public ArrayList listProposalsOfTag(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LIST_PROPOSAL_SQL_T, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_PROPOSAL_SQL_T);
			
			ArrayList proposalsList = new ArrayList();
			while (resultSet.next()) {
				IProposal proposal = getProposalFromResultSetCursor(resultSet);
				proposalsList.add(proposal);
			}
			return proposalsList;
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
	
	public ArrayList listProposalNames(Connection connection, boolean limitToProposalsWithoutPIs) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LIST_PROPOSAL_NAMES_SQL, Statement.RETURN_GENERATED_KEYS);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_PROPOSAL_NAMES_SQL);
			
			ArrayList proposalsList = new ArrayList();
			if (!limitToProposalsWithoutPIs) {
				while (resultSet.next()) {
					long proposalId =  resultSet.getLong(1);
					String proposalName = resultSet.getString(2);
					proposalsList.add(proposalName);
				}
			} else {
				//include proposals without a PI only
				AccessPermissionAccessor accessPermissionAccessor = new AccessPermissionAccessor();
				while (resultSet.next()) {
					long proposalId =  resultSet.getLong(1);
					String proposalName = resultSet.getString(2);
					
					long proposalPi = accessPermissionAccessor.getProposalPI(connection, proposalId);
					if (proposalPi == -1) {
						//proposal has no PI, add it to list
						proposalsList.add(proposalName);
					}
				}
			}
			return proposalsList;
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
	
	public ArrayList listProposalsOfProgramme(Connection connection, long pid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LIST_PROPOSAL_SQL_P, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_PROPOSAL_SQL_P);
			ArrayList proposalsList = new ArrayList();
			while (resultSet.next()) {
				IProposal proposal = getProposalFromResultSetCursor(resultSet);
				proposalsList.add(proposal);
			}
			return proposalsList;
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
	
	/**
	 * Updates the proposal with the specified key. 
	 * Other data entities such as Linkage are un-affected
	 * @param connection The database connection to use for the operation
	 * @param proposal The new Proposal
	 * @param keyId The LockingModel key allowing updating
	 * @throws Exception If anything goes wrong during the process
	 */
	public void updateProposal(Connection connection, IProposal proposal, long keyId) throws Exception {
		logger.info("updateProposal(Connection connection, " + proposal + ", " + keyId + ")");
		PreparedStatement stmt = null;
		try {
			/*
			IProposal proposalInDB = getProposal(connection, proposal.getID());
			if (proposalInDB.getExpiryDate() != proposal.getExpiryDate()) {
				logger.info("expiry date of proposal has changed");
				SemesterAccessor semesterAccessor =	new SemesterAccessor();
				AccountAccessor accountAccessor = new AccountAccessor(AccountModelTypes.PROPOSAL_ACCOUNT_TYPE);
				
				//proposal expiry date has changed, check if it's changed semester.
				//if it has, add the required accounts for those semesters.
				ISemester originalProposalExpirySemester = semesterAccessor.getSemesterOfDate(connection, proposalInDB.getExpiryDate());
				ISemester newProposalExpirySemester = semesterAccessor.getSemesterOfDate(connection, proposal.getExpiryDate());
				if (newProposalExpirySemester == null) {
					throw new Exception("Required Expiry Semester for date: " +  new Date(proposal.getExpiryDate()) + " doesn't exist, this may require an administrator fix");
				}
				if (originalProposalExpirySemester == null) {
					throw new Exception("Original Expiry Semester for date: " +  new Date(proposalInDB.getExpiryDate()) + " doesn't exist, this may require an administrator fix");
				}
				if (newProposalExpirySemester.getID() > originalProposalExpirySemester.getID()) {
					logger.info("the new expiry date moves the lifetime of the proposal " + proposal.getName() + " into at least one new semester");
					
					List semesters = accountAccessor.listSemestersForWhichOwnerHasAccounts(connection, proposal.getID());
					Iterator i = semesters.iterator();
					boolean proposalHasAccounts;
					long highestSemesterForWhichOwnerAlreadyHasAccountsId = 0;
					if (semesters.size() == 0) {
						proposalHasAccounts = false;
					} else {
						proposalHasAccounts = true;
						while (i.hasNext()) {
							ISemester semester = (ISemester) i.next();
							if (semester.getID() > highestSemesterForWhichOwnerAlreadyHasAccountsId) {
								highestSemesterForWhichOwnerAlreadyHasAccountsId = semester.getID();
							}
						}
					}
					if (proposalHasAccounts) {
						logger.info("the proposal already has accounts up until semester: " + semesterAccessor.getSemester(connection, highestSemesterForWhichOwnerAlreadyHasAccountsId).getName());
						if (highestSemesterForWhichOwnerAlreadyHasAccountsId < newProposalExpirySemester.getID()) {
							long startSemesterId = semesterAccessor.getSemester(connection, highestSemesterForWhichOwnerAlreadyHasAccountsId).getID();
							startSemesterId += 1;
							ISemester startSemester = semesterAccessor.getSemester(connection, startSemesterId);
							long endSemesterId = newProposalExpirySemester.getID();
							logger.info("creating all accounts for semesters " + startSemester.getName() + " to " + newProposalExpirySemester.getName() + " for proposal " + proposal.getName());
							accountAccessor.addAllAccountsBetweenSemesters(connection, proposal.getID(), startSemesterId, endSemesterId);
							logger.info("accounts created");
						}
					} else {
						//for proposals entered pre-gui checks, this may be necessary
						//create all accounts for lifetime of proposal
						ISemester startSemester = semesterAccessor.getSemesterOfDate(connection, proposal.getActivationDate());
						ISemester endSemester = semesterAccessor.getSemesterOfDate(connection, proposal.getExpiryDate());
						long startSemesterId = startSemester.getID();
						long endSemesterId = endSemester.getID();
						logger.info("the proposal had no accounts, creating all accounts for proposal " + proposal.getName() + " from " +startSemester.getName() + " to " + endSemester.getName() );
						accountAccessor.addAllAccountsBetweenSemesters(connection, proposal.getID(), startSemesterId, endSemesterId);
						logger.info("accounts created");
					}
				}	
			}
			*/
			stmt = connection.prepareStatement(UPDATE_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, proposal.getName());
			stmt.setString(2, proposal.getTitle());
			stmt.setLong(3, proposal.getPriority());
			stmt.setLong(4, proposal.getActivationDate());
			stmt.setLong(5, proposal.getExpiryDate());
			stmt.setString(6, proposal.getScienceAbstract());
			stmt.setBoolean(7, proposal.allowsUrgentGroups());
			stmt.setDouble(8, proposal.getPriorityOffset());
			stmt.setBoolean(9, proposal.isEnabled());
			stmt.setBoolean(10, proposal.allowsFixedGroups());
			stmt.setString(11, proposal.getTypeCode());
			
			stmt.setLong(12, proposal.getID());
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_PROPOSAL_SQL, true);
			
			logger.info("/updateProposal()");
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
	
	/** Private  methods *******************************************************************/
	private XProposal getProposalFromResultSetCursor(ResultSet resultSet) throws Exception {
		//table fields
		String name, sciAbstract, title, typeCode;
		long id, pid, tid, activationDate, expiryDate;
		int priority;
		double priorityOffset;
		boolean allowUrgentGroups, enabled, allowFixedGroups;
		
		id 							= resultSet.getLong(1);
		pid						= resultSet.getLong(2);
		tid							= resultSet.getLong(3);
		name 					= resultSet.getString(4);
		title						= resultSet.getString(5);
		priority 					= resultSet.getInt(6);
		activationDate		= resultSet.getLong(7);
		expiryDate				= resultSet.getLong(8);
		sciAbstract			= resultSet.getString(9);
		allowUrgentGroups	= resultSet.getBoolean(10);
		priorityOffset			= resultSet.getDouble(11);
		enabled					= resultSet.getBoolean(12);
		allowFixedGroups	= resultSet.getBoolean(13);
		typeCode				= resultSet.getString(14);
		
		//build proposal
		XProposal proposal = new XProposal();
		proposal.setID(id);
		proposal.setName(name);
		proposal.setTitle(title);
		proposal.setPriority(priority);
		proposal.setActivationDate(activationDate);
		proposal.setExpiryDate(expiryDate);
		proposal.setScienceAbstract(sciAbstract);
		proposal.setAllowUrgentGroups(allowUrgentGroups);
		proposal.setPriorityOffset(priorityOffset);
		proposal.setEnabled(enabled);
		proposal.setAllowFixedGroups(allowFixedGroups);
		proposal.setTypeCode(typeCode);
		return proposal;
	}
	
}
