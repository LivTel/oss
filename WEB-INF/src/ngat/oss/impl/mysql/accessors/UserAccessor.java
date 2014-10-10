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
import ngat.oss.reference.Const;
import ngat.phase2.IUser;
import ngat.phase2.XUser;

import org.apache.log4j.Logger;

public class UserAccessor  {
	
	/*
	USER
	  `id` 					int
	  `userName` 		String
	  `password` 		String
	  `lastName` 		String
	  `firstName` 		String
	  `department` 	String
	  `organisation`	String
	  `address` 		String
	  `city` 				String
	  `region` 			String
	  `country` 			String
	  `postcode` 		String
	  `email` 			String
	  `telephone` 		String
	  `fax` 				String
	  `isSuperUser`	boolean
	 */
	
	static Logger logger = Logger.getLogger(UserAccessor.class);
	
	//statements
	
	public static final String SELECT_USER = 
		"select " +
		"id, userName, password, lastName, firstName, department, organisation, address, city, region, country, postcode, email, telephone, fax, isSuperUser " +
		"from " +
		"USER ";
		
	public static final String SELECT_USER_N = 
		SELECT_USER + 
		" where userName=?";
	
	public static final String SELECT_USER_I = 
		SELECT_USER + 
		" where id=?";
	
	//statements
	public static final String INSERT_USER_SQL = 						
		"insert into USER (" +
		"userName, password, lastName, firstName, department, organisation, address, city, region, country, postcode, email, telephone, fax, isSuperUser" + 
		") values (" + 
		"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public static final String LIST_USER_SQL = 							
		"select " +
		"id, userName, password, lastName, firstName, department, organisation, address, city, region, country, postcode, email, telephone, fax, isSuperUser " + 
		"from " + 
		"USER " +
		"order by userName";
	
	public static final String DEL_USER_SQL = 								
		"delete from USER where id = ?";
	
	public static final String UPDATE_USER_SQL =						
		"update USER "+
		"set " + 
		"userName=?," +
		"password=?," +
		"lastName=?," +
		"firstName=?," +
		"department=?," +
		"organisation=?," +
		"address=?," +
		"city=?," +
		"region=?," +
		"country=?," +
		"postcode=?," +
		"email=?," +
		"telephone=?," +
		"fax=?, " +
		"isSuperUser=?" +
		" where id=?";
																			
	/** Public methods ******************************************************************/
	
	public long addUser(Connection connection, IUser user) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getLastName());
			stmt.setString(4, user.getFirstName());
			stmt.setString(5, user.getDepartment());
			stmt.setString(6, user.getOrganization());
			stmt.setString(7, user.getAddress());
			stmt.setString(8, user.getCity());
			stmt.setString(9, user.getRegion());
			stmt.setString(10, user.getCountry());
			stmt.setString(11, user.getPostalCode());
			stmt.setString(12, user.getEmail());
			stmt.setString(13, user.getTelephone());
			stmt.setString(14, user.getFax());
			stmt.setBoolean(15, user.isSuperUser());
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_USER_SQL, true);
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
	
	public void deleteUser(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(DEL_USER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			//delete from USER table
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_USER_SQL, false);
			
			//delete users ACCESS_PERMISSIONs
			new AccessPermissionAccessor().revokeAllPermissionsOfUser(connection, id);
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
	
	public void updateUser(Connection connection, IUser user) throws Exception {	
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(UPDATE_USER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getLastName());
			stmt.setString(4, user.getFirstName());
			stmt.setString(5, user.getDepartment());
			stmt.setString(6, user.getOrganization());
			stmt.setString(7, user.getAddress());
			stmt.setString(8, user.getCity());
			stmt.setString(9, user.getRegion());
			stmt.setString(10, user.getCountry());
			stmt.setString(11, user.getPostalCode());
			stmt.setString(12, user.getEmail());
			stmt.setString(13, user.getTelephone());
			stmt.setString(14, user.getFax());
			stmt.setBoolean(15, user.isSuperUser());
			stmt.setLong(16, user.getID());
			
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_USER_SQL, false);
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
	 * Public method for listing all Users
	 * @return List of Users
	 * @throws SQLException If things go wrong
	 */
	public List listUsers(Connection connection) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(LIST_USER_SQL, Statement.RETURN_GENERATED_KEYS);
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_USER_SQL);
			
			List userList = new ArrayList();
			while (resultSet.next()) {
				IUser user = getUserFromResultSetCursor(resultSet);
				userList.add(user);
			}
			return userList;
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
	 * Return the User with the specified name.
	 * 
	 * @param name
	 *            The user whose name we know.
	 * @return The user entry.
	 * @throws SQLException 
	 */
	public IUser findUser(Connection connection, String name) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(SELECT_USER_N, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			
			IUser user = null;
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_USER_N);
			if (resultSet != null) {
				if (resultSet.next()) {
					user = getUserFromResultSetCursor(resultSet);
				}
			}
			return user;
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
	
	public boolean userExists(Connection connection, String name) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(SELECT_USER_N, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_USER_N);
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
	
	/**
	 * Return the User with the specified username and password, if one does not exist with this combination, return null.
	 * @param username supplied username
	 * @param suppliedPassword supplied password
	 * @return user object authenticated against username and password
	 * @throws Phase2Exception 
	 * @throws SQLException
	 */
	public IUser authenticate(Connection connection, String username, String suppliedPassword) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			logger.info("authenticate(" + username + "," + suppliedPassword + ")");
			
			stmt = connection.prepareStatement(SELECT_USER_N, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, username);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_USER_N);
			IUser user = null;
			if (resultSet != null) {
				//AUTHENTICATE HERE
				if (resultSet.next()) {
					//user with that username found
					//now compare passwords
					user = getUserFromResultSetCursor(resultSet);
					String passwordInUserTable = user.getPassword();
					if (passwordInUserTable == null) {
						throw new Exception ("password for user: " +username + " is null");
					}
					if (!passwordInUserTable.equals(suppliedPassword)) {
						//passwords do not match
						//return an 'error' user
						user = getErrorUser();
					}
				} else {
					//no user with that username found
					//return an 'error' user
					user = getErrorUser();
				}
			}
			return user;
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
	
	private IUser getErrorUser() {
		XUser xuser = new XUser();
		xuser.setID(Const.ERROR_ID);
		return xuser;
	}
	
	/**
	 * Returns the User identified by the ID.
	 * 
	 * @param userID
	 *            The ID of the User.
	 * @return The User specified by userID.
	 * @throws SQLException
	 */
	public IUser getUser(Connection connection, long uid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(SELECT_USER_I, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, uid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, SELECT_USER_I);
			IUser user = null;
			if (resultSet.next()) {
				user = getUserFromResultSetCursor(resultSet);
			}
			return user;
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
	 * Returns an IUser object from the cursor position in the ResultSet
	 * @param resultSet
	 * @return IUser The marshalled IUser object
	 * @throws SQLException If things go wrong
	 */
	private IUser getUserFromResultSetCursor(ResultSet resultSet) throws Exception {
		//table fields
		
		long id;
		String userName, password, lastName, firstName, department, organisation, address, city, region, country, postcode, email, telephone, fax;
		boolean isSuperUser;
		
		id 					= resultSet.getLong(1);
		userName		= resultSet.getString(2);
		password		= resultSet.getString(3);
		lastName		= resultSet.getString(4);		
		firstName		= resultSet.getString(5);
		department	= resultSet.getString(6);
		organisation	= resultSet.getString(7);
		address			= resultSet.getString(8);
		city				= resultSet.getString(9);
		region			= resultSet.getString(10);
		country			= resultSet.getString(11);
		postcode		= resultSet.getString(12);
		email				= resultSet.getString(13);
		telephone		= resultSet.getString(14);
		fax				= resultSet.getString(15);
		isSuperUser	= resultSet.getBoolean(16);
		
		//build user
		XUser user = new XUser();
		user.setID(id);
		user.setName(userName);
		user.setPassword(password);
		user.setLastName(lastName);
		user.setFirstName(firstName);
		user.setDepartment(department);
		user.setOrganization(organisation);
		user.setAddress(address);
		user.setCity(city);
		user.setRegion(region);
		user.setCountry(country);
		user.setPostalCode(postcode);
		user.setEmail(email);
		user.setTelephone(telephone);
		user.setFax(fax);
		user.setIsSuperUser(isSuperUser);
		return user;
	}
	
	/** Main *********************************************************************************/
	public static void main(String[] args) {
		//stuff here
	}
}
