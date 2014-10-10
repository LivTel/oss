package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.reference.AccountModelTypes;
import ngat.phase2.ISemester;
import ngat.phase2.ITag;
import ngat.phase2.XAccount;
import ngat.phase2.XSemesterPeriod;
import ngat.phase2.XTag;

import org.apache.log4j.Logger;

public class TagAccessor  {
	
	static Logger logger = Logger.getLogger(TagAccessor.class);
	
	/*
	TAG
	 	id
	 	name
	*/
	
	//statements
	public static final String INSERT_TAG_SQL = 		
		"insert into TAG (" +
		"name" + 
		") values (" + 
		"?)";
	
	public static final String GET_TAG_SQL = 							
		"select " +
		"id, name " +
		"from " +
		"TAG " +
		"where id=?";
	
	public static final String LIST_TAGS_SQL = 							
		"select " +
		"id, name " +
		"from " + 
		"TAG " + 
		"order by name";
	
	public static final String FIND_TAG_SQL = 							
		"select " +
		"id, name " +
		"from " +
		"TAG " +
		"where name=?";
	
	public static final String DEL_TAG_SQL = 								
		"delete from TAG where id = ?";
	
	public static final String UPDATE_TAG_SQL =						
		"update TAG "+
		"set " +
		"name=?" +
		" where id=?";
																										
	/** Public methods ******************************************************************
	 * @throws Exception */
	
	
	/**
	 * adds the TAG to the database, also adds semester accounts from now until the end of time
	 */
	public long addTag(Connection connection, ITag tag) throws Exception {
		
		PreparedStatement stmt = null;
		
		try {
			//table fields
			String name;
			
			//load values
			name = tag.getName();
			stmt = connection.prepareStatement(INSERT_TAG_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setString(1, name);
			
			//execute query
			long tagId = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_TAG_SQL, true);
			
			//if (!addAccounts) {
				//don't add accounts!
				return tagId;
			//}
			/*
			//now add accounts
			AccountAccessor accountAccessor = new AccountAccessor(AccountModelTypes.TAG_ACCOUNT_TYPE);
			
			//get relevant semesterIDs
			SemesterAccessor semesterAccessor = new SemesterAccessor();
			XSemesterPeriod semesterPeriod = (XSemesterPeriod)semesterAccessor.getSemesterPeriodOfDate(connection, new Date().getTime());
			
			int firstSemesterID = (int) semesterPeriod.getFirstSemester().getID();

			for (int semId=firstSemesterID; semId < SemesterAccessor.MAX_SEMESTER_ID; semId++) {
				XAccount account = new XAccount();
				account.setChargeable(true);
				accountAccessor.addAccount(connection, tagId, semId, account);
			}
			return tagId;
			*/
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
	
	public ITag getTag(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_TAG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TAG_SQL);
			XTag tag = null;
			if (resultSet.next()) {
				tag = getTagFromResultSetCursor(resultSet);			
			}
			return tag;
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
	
	public ITag findTag(Connection connection, String name) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(FIND_TAG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, FIND_TAG_SQL);
			XTag tag = null;
			if (resultSet.next()) {
				tag = getTagFromResultSetCursor(resultSet);
			}
			return tag;
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
	
	public void deleteTag(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_TAG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_TAG_SQL, true);
			ConnectionPool.getInstance().surrenderConnection(connection);
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
	
	public ArrayList listTags(Connection connection) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LIST_TAGS_SQL, Statement.RETURN_GENERATED_KEYS);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_TAGS_SQL);
	
			ArrayList tagList = new ArrayList();
			while (resultSet.next()) {
				ITag tag = getTagFromResultSetCursor(resultSet);
				tagList.add(tag);
			}
			return tagList;
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
	 * Updates the Tag with the specified key. 
	 * @param connection The database connection to use for the operation
	 * @param tag The new Tag
	 * @param keyId The LockingModel key allowing updating
	 * @throws Exception If anything goes wrong during the process
	 */
	public void updateTag(Connection connection, ITag tag, long keyId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(UPDATE_TAG_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, tag.getName());
			stmt.setLong(2, tag.getID());
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_TAG_SQL, true);
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
	private XTag getTagFromResultSetCursor(ResultSet resultSet) throws Exception {
		
		//table fields
		long id;
		String name;
		id 						= resultSet.getLong(1);
		name 				= resultSet.getString(2);
		
		//build tag
		XTag tag = new XTag();
		tag.setID(id);
		tag.setName(name);
		
		return tag;
	}
	
	/** Main *********************************************************************************/
	/*
	public static void main(String[] args) {
		CommandTokenizer parser = new CommandTokenizer("--");
		parser.parse(args);
		ConfigurationProperties rconfig = parser.getMap();
		
		new ConnectionPool(rconfig);
		
		ProposalAccessor proposalAccessor = new ProposalAccessor();
		
		//stuff here
		XProposal proposal = new XProposal();
		proposal.setActivationDate(new Date().getTime());
		proposal.setExpiryDate(new Date().getTime());
		proposal.setName("bollocks");
		proposal.setPriority(6);
		proposal.setScienceAbstract("sci");
		try {
			proposalAccessor.addProposal(proposal);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
}


