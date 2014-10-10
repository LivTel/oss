package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IAccessPermission;
import ngat.phase2.XAccessPermission;

import org.apache.log4j.Logger;

/**
 * TESTED 22/02/08
 * @author nrc
 *
 */
public class AccessPermissionAccessor {

	static Logger logger = Logger.getLogger(AccessPermissionAccessor.class);
	
	/*
	ACCESS_PERMISSION
		id 							int
		uid 						int
		pid 						int
		role 						tinyint
	 */
	
	
	public static final String SELECT_ACCESS = 
		"select " + 
		"id, uid, pid, role " + 
		"from " + 
		"ACCESS_PERMISSION ";
		
	public static final String SELECT_ACCESS_UP = 
		SELECT_ACCESS +
		"where uid=? and pid=?";
	
	public static final String SELECT_ACCESS_U = 
		SELECT_ACCESS +
		"where uid=?";
	
	public static final String SELECT_ACCESS_P = 
		SELECT_ACCESS +
		"where pid=?";
	
	public static final String SELECT_ACCESS_PR = 
		SELECT_ACCESS +
		"where pid=? and role=?";

	public static final String INSERT_ACCESS = 
		"insert into ACCESS_PERMISSION (" +
		"uid, pid, role" +
		") values (" +
		"?, ?, ?)";
	
	public static final String UPDATE_ACCESS_SQL =					
		"update ACCESS_PERMISSION "+
		"set " + 
		"uid=?, " +
		"pid=?, " +
		"role=? " +
		"where id=?";
	
	public static final String DELETE_ACCESS_PERMISSION = 
		"delete from ACCESS_PERMISSION "+
		"where id =?";
	
	public static final String DELETE_USERS_ACCESS_PERMISSIONS = 
		"delete from ACCESS_PERMISSION "+
		"where uid =?";
	
	public static final String DELETE_PROPOSALS_ACCESS_PERMISSIONS = 
		"delete from ACCESS_PERMISSION "+
		"where pid =?";
	
	
	public AccessPermissionAccessor() {}
	
	public long addPermission(Connection connection, IAccessPermission perm) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_ACCESS, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setInt(1, (int)perm.getUserID());
			stmt.setInt(2, (int)perm.getProposalID());
			stmt.setInt(3, perm.getUserRole());
			
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_ACCESS, true);
			
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
	
	/**
	 *  Returns an AccessPermission for the speciifed user on the specified proposal. 
	 *  This should be null if the user has no access permission for the specified proposal.
	 * @param uid The user for which access permissions are required.
	 * @param pid The proposal for which access permissions are required.
	 * @return
	 * @throws SQLException
	 */
	public IAccessPermission getAccessPermission(Connection connection, long uid, long pid) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(SELECT_ACCESS_UP, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, uid);
			stmt.setLong(2, pid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_ACCESS_UP);
			IAccessPermission accessPermission = null;
			if (resultSet.next()) {
				accessPermission = getAccessPermissionFromResultSetCursor(resultSet);
			}
			return accessPermission;
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
	 * Implementors should return the ID of the user who owns (is PI) for the specified proposal.
	 * 
	 * @param proposalID
	 *            The proposal for which the owner is required.
	 * @return The ID of the proposal's owner.
	 * @throws Phase2Exception
	 */
	public long getProposalPI(Connection connection, long proposalID) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(SELECT_ACCESS_PR, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, proposalID);
			stmt.setLong(2, IAccessPermission.PRINCIPLE_INVESTIGATOR_ROLE);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_ACCESS_PR);
			long id = -1;
			if (resultSet.next()) {
				IAccessPermission accessPermission = getAccessPermissionFromResultSetCursor(resultSet);
				id = accessPermission.getUserID();
			}
			return id;
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
	
	public void revokePermission(Connection connection, long apid) throws Exception {
	
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DELETE_ACCESS_PERMISSION, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, apid);
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DELETE_ACCESS_PERMISSION, false);
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
	
	public void revokeAllPermissionsOfUser(Connection connection, long uid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DELETE_USERS_ACCESS_PERMISSIONS, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, uid);
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DELETE_USERS_ACCESS_PERMISSIONS, false);
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
	
	public void revokeAllPermissionsOfProposal(Connection connection, long pid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DELETE_PROPOSALS_ACCESS_PERMISSIONS, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DELETE_PROPOSALS_ACCESS_PERMISSIONS, false);
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
	 * @param perm
	 * @throws Phase2Exception
	 * @throws SQLException
	 */
	
	public void updatePermission(Connection connection, IAccessPermission perm) throws Exception {				
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(UPDATE_ACCESS_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, perm.getUserID());
			stmt.setLong(2, perm.getProposalID());
			stmt.setInt(3, perm.getUserRole());
			stmt.setLong(4, perm.getID());			//key being used
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_ACCESS_SQL, false);
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
	 * Returns a list of AccessPermissions for the specified
	 * user.
	 * 
	 * @param userID
	 *            The user for which access permissions are required.
	 * @return A list of access permissions.
	 * @throws Phase2Exception
	 * @throws SQLException 
	 */

	public List listAccessPermissionsOfUser(Connection connection, long userID) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(SELECT_ACCESS_U, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, userID);
		
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_ACCESS_U);
			
			ArrayList permissionsList = new ArrayList();
			while (resultSet.next()) {
				IAccessPermission accessPermission = getAccessPermissionFromResultSetCursor(resultSet);
				permissionsList.add(accessPermission);
			}
			return permissionsList;
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
	 * Returns a list of AccessPermissions for the specified proposal.
	 * 
	 * @param proposalID
	 *            The proposal for which access permissions are required.
	 * @return A list of access permissions.
	 * @throws Phase2Exception
	 * @throws SQLException 
	 */
	public List listUserPermissionsOnProposal(Connection connection, long proposalID) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(SELECT_ACCESS_P, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, proposalID);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_ACCESS_P);
			
			ArrayList permissionsList = new ArrayList();
			while (resultSet.next()) {
				IAccessPermission accessPermission = getAccessPermissionFromResultSetCursor(resultSet);
				permissionsList.add(accessPermission);
			}
			return permissionsList;
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
	
	/* private methods *********************  */
	private IAccessPermission getAccessPermissionFromResultSetCursor(ResultSet resultSet) throws Exception {
		
		//table fields
		long id, pid, uid;
		int role;
		
		id							= resultSet.getLong(1);
		uid						= resultSet.getLong(2);
		pid						= resultSet.getLong(3);
		role						= resultSet.getInt(4);
		
		XAccessPermission accessPermission = new XAccessPermission();
		accessPermission.setID(id);
		accessPermission.setUserID(uid);
		accessPermission.setProposalID(pid);
		accessPermission.setUserRole(role);
		
		return accessPermission;
	}
}
