/**
 * 
 */
package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.ObjectTypes;
import ngat.phase2.ILock;
import ngat.phase2.XLock;

import org.apache.log4j.Logger;

/**
 * @author snf & nrc (alterations to make use of PreparedStatements)
 *
 */
public class LocksAccessor {
	
	static Logger logger = Logger.getLogger(LocksAccessor.class);
	
	public static final String CREATE_LOCK_SQL = 						
		"insert into LOCKS (" +
		"oid, objectType, atTime, clientRef, keyVal" + 
		") values (" + 
		"?, ?, ?, ?, ?)";
	
	public static final String IS_LOCKED_SQL = 
		"select count(*) from LOCKS " + 
		"where oid=? " +
		"and objectType=?";

	public static final String GET_LOCK_SQL = 							
		"select " +
		"id, atTime, clientRef, keyVal, locked " +
		"from " +
		"LOCKS " +
		"where oid=? " +
		"and objectType=?";
	
	public static final String GET_KEY_OF_LOCK_SQL = 
		"select keyVal from LOCKS " + 
		"where oid=? " +
		"and objectType=?";
	
	public static final String DELETE_LOCK_SQL = 						
		"delete from LOCKS where " +
		"oid=? " + 
		"and objectType=? " + 
		"and keyVal=?";
	
	public LocksAccessor() {	
	}
	
	/**
	 * Create a lock for an object
	 * @param connection Database connection to use
	 * @param objectType object type selected from ngat.oss.impl.mysql.reference.ObjectTypes
	 * @param oid The id of the objecto to be locked
	 * @param clientRef The reference of the client creating the lock 
	 * @return The key of the lock (required to unlock the lock)
	 * @throws Exception
	 */
	public long createLock(Connection connection, int objectType, long oid, String clientRef) throws Exception {
		
		PreparedStatement stmt = null;
		
		try {
			if (isLocked(connection, objectType, oid)) {
				throw new Exception ("cannot create lock. object is already locked. object id = " + oid + " objectType=" +ObjectTypes.getObjectTypeAsName(objectType));
			}
			
			long atTime = new java.util.Date().getTime();
			int key = makeKey();
			
			//table fields
			stmt = connection.prepareStatement(CREATE_LOCK_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, oid);
			stmt.setLong(2, objectType);
			stmt.setLong(3, atTime);
			stmt.setString(4, clientRef);
			stmt.setInt(5, key);
			stmt.setBoolean(6, true);
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, CREATE_LOCK_SQL, true);
			return key;
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
	 * Check whether a specified lock exists
	 * @param connection Database connection to use
	 * @param objectType object type selected from ngat.oss.impl.mysql.reference.ObjectTypes
	 * @param oid Id of locked object
	 * @return Whether that object is locked true | fales
	 * @throws Exception
	 */
	public boolean isLocked(Connection connection, int objectType, long oid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			//table fields
			stmt = connection.prepareStatement(IS_LOCKED_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, oid);
			stmt.setLong(2, objectType);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, IS_LOCKED_SQL);
			if (resultSet.next()) {
				return (resultSet.getInt(1) != 0);
			} else {
				throw new Exception("unable to determine status of lock for object type: " +ObjectTypes.getObjectTypeAsName(objectType) + ", with id: " + oid);
			}
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
	 * Get the clientRef of a lock (assumes object is locked - i.e. isLocked() returned true)
	 * @param connection Database connection to use
	 * @param objectType object type selected from ngat.oss.impl.mysql.reference.ObjectTypes
	 * @param oid ID of locked object
	 * @return ClientRef of lock
	 * @throws Exception If object isn't in fact locked
	 */
	public ILock getLock(Connection connection, int objectType, long oid, boolean returnLockKey) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			//table fields
			stmt = connection.prepareStatement(GET_LOCK_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, oid);
			stmt.setLong(2, objectType);
			
			resultSet =  DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_LOCK_SQL);
			
			if (resultSet.next()) {
				XLock lock = new XLock();
				lock.setId(resultSet.getLong(1));
				lock.setDate(resultSet.getLong(2));
				lock.setClientRef(resultSet.getString(3));
				
				if (returnLockKey) {
					lock.setKey(resultSet.getInt(4));
				}
				return lock;
			} else {
				throw new Exception("specified lock for object of type: " +ObjectTypes.getObjectTypeAsName(objectType) + ", with id: " + oid + " does not exist");
			}
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
	 * Attempts to unlock specified lock with specified key
	 * If successful, the lock is deleted
	 * @param connection Database connection to use
	 * @param objectType objectType object type selected from ngat.oss.impl.mysql.reference.ObjectTypes
	 * @param oid Id of the object to unlock
	 * @param key Key of the lock to unlock
	 * @return true if unlock successful, false otherwise
	 * @throws Exception If someother error occurred
	 */
	public boolean unlock(Connection connection, int objectType, long oid, int key) throws Exception {
		
		//check whether lock exists
		if (!isLocked(connection, objectType, oid)) {
			throw new Exception("trying to unlock object that isn't locked: objectType=" + ObjectTypes.getObjectTypeAsName(objectType) + " oid=" + oid);
		}
		
		//lock exists, does that lock have the specified key?
		boolean keyIsCorrect = doesLockHaveKey(connection, objectType, oid, key);
		if (!keyIsCorrect) {
			logger.info("attempt to unlock object of type: " + ObjectTypes.getObjectTypeAsName(objectType) + " with id: " +oid + " failed, supplied key ( " + key + ") was incorrect" );
			return false;
		}
		
		try {
			deleteLock(connection, objectType, oid, key);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Failed to delete lock");
		}
		
		return true;
	}
	
	private void deleteLock(Connection connection, int objectType, long oid, int key) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			//table fields
			stmt = connection.prepareStatement(DELETE_LOCK_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, oid);
			stmt.setLong(2, objectType);
			stmt.setInt(3, key);
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, CREATE_LOCK_SQL, true);
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
	 * @param connection Database connection to use
	 * @param objectType Object type of object getting lock key of
	 * @param oid Object Id of object getting lock key of
	 * @param key Supplied key to check against
	 * @return true if lock has supplied key, false otherwise
	 * @throws Exception If something goes wrong
	 */
	private boolean doesLockHaveKey(Connection connection, int objectType, long oid, int key) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			//table fields
			stmt = connection.prepareStatement(GET_KEY_OF_LOCK_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, oid);
			stmt.setLong(2, objectType);
			
			resultSet =  DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_KEY_OF_LOCK_SQL);
			int keyVal = -1;
			if (resultSet.next()) {
				keyVal = resultSet.getInt(1);
			} else {
				throw new Exception("specified lock does not exist: " +objectType + ", with id: " + oid);
			}
			return (keyVal == key);
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
	
	/** Make up a key.*/
	private int makeKey() {
		return (int)System.currentTimeMillis()/4857;
	}
	
	/** Quote a string.*/
	private String quote(String arg) {
		return "\""+arg+"\"";
	}
}

