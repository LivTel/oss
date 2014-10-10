package ngat.oss.impl.mysql.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.accessors.AccessPermissionAccessor;
import ngat.oss.impl.mysql.accessors.LocksAccessor;
import ngat.oss.impl.mysql.accessors.TestAccessor;
import ngat.oss.impl.mysql.accessors.UserAccessor;
import ngat.oss.impl.mysql.reference.ObjectTypes;
import ngat.oss.model.ILockingModel;
import ngat.oss.transport.RemotelyPingable;
import ngat.phase2.ILock;

import org.apache.log4j.Logger;

public class LockingModel extends UnicastRemoteObject implements ILockingModel, RemotelyPingable {

	static Logger logger = Logger.getLogger(LockingModel.class);
	
	//phase2 accessors
	AccessPermissionAccessor	accessPermissionAccessor;
	UserAccessor 						userAccessor;
	LocksAccessor						locksAccessor;
	TestAccessor 						testAccessor;
	
	public LockingModel(int rmiPort) throws RemoteException {
		super(rmiPort);
		accessPermissionAccessor	= new AccessPermissionAccessor();
		userAccessor						= new UserAccessor();
		locksAccessor						= new LocksAccessor();
		testAccessor							= new TestAccessor();
	}
	
	public void ping() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			testAccessor.ping(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	/* Locking ******************************************/
	public long lockGroup(long groupID, String clientID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long lid =  locksAccessor.createLock(connection, ObjectTypes.GROUP_TYPE, groupID, clientID);
			connection.commit();
			return lid;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	/* Unlocking ******************************************/
	
	public boolean unlockGroup(long groupID, int key) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			boolean unlocked =  locksAccessor.unlock(connection, ObjectTypes.GROUP_TYPE, groupID, key);
			connection.commit();
			return unlocked;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				logger.info("performing connection.rollback();");
				connection.rollback();
				logger.info("connection.rollback() completed successfully.");
			} catch (SQLException e1) {
				logger.error("rollback failed!");
				e1.printStackTrace();
			}
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	/* Enquiring ******************************************/
	
	public ILock getGroupLock(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			//return the lock to the client, but don't populate the key field
			return locksAccessor.getLock(connection, ObjectTypes.GROUP_TYPE, groupID, false);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

}
