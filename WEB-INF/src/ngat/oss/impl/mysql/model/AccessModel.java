package ngat.oss.impl.mysql.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.accessors.AccessPermissionAccessor;
import ngat.oss.impl.mysql.accessors.LoginAccessor;
import ngat.oss.impl.mysql.accessors.TestAccessor;
import ngat.oss.impl.mysql.accessors.UserAccessor;
import ngat.oss.impl.mysql.accessors.MinimumClientVersionAccessor;
import ngat.oss.model.IAccessModel;
import ngat.oss.transport.RemotelyPingable;
import ngat.phase2.IAccessPermission;
import ngat.phase2.ILoginRecord;
import ngat.phase2.IUser;
import ngat.phase2.IVersion;

import org.apache.log4j.Logger;

public class AccessModel extends UnicastRemoteObject implements IAccessModel, RemotelyPingable {

	static Logger logger = Logger.getLogger(AccessModel.class);
	
	//phase2 accessors
	AccessPermissionAccessor	accessPermissionAccessor;
	UserAccessor 						userAccessor;
	TestAccessor 						testAccessor;
	LoginAccessor						loginAccessor;
	MinimumClientVersionAccessor 					versionAccessor;
	
	public AccessModel(int rmiPort) throws RemoteException {
		super(rmiPort);
		accessPermissionAccessor 	= new AccessPermissionAccessor();
		userAccessor 						= new UserAccessor();
		testAccessor							= new TestAccessor();
		loginAccessor						= new LoginAccessor();
		versionAccessor					= new MinimumClientVersionAccessor();
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
	
	public IVersion getMinimumClientVersionNumber() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return versionAccessor.getMinimumClientVersionNumber(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public void receiveLoginRecord(ILoginRecord loginRecord) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			loginAccessor.receiveLogin(connection, loginRecord);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public List listAccessPermissionsOnProposal(long proposalID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accessPermissionAccessor.listUserPermissionsOnProposal(connection, proposalID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IAccessPermission getAccessPermission(long userID, long proposalID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accessPermissionAccessor.getAccessPermission(connection, userID, proposalID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long getProposalPI(long proposalID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accessPermissionAccessor.getProposalPI(connection, proposalID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IUser findUser(String name) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return userAccessor.findUser(connection, name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}	
	}

	public boolean userExists(String name) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return userAccessor.userExists(connection, name);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}	
	}
	
	public List listUsers() throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return userAccessor.listUsers(connection);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}	
	}

	public List listAccessPermissionsOfUser(long userID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accessPermissionAccessor.listAccessPermissionsOfUser(connection, userID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public IUser authenticate(String username, String password, String ignoreThis) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return userAccessor.authenticate(connection, username, password);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public IUser getUser(long userID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return userAccessor.getUser(connection, userID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addPermission(IAccessPermission permission) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long pid =  accessPermissionAccessor.addPermission(connection, permission);
			connection.commit();
			return pid;
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
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void revokePermission(long aid) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			accessPermissionAccessor.revokePermission(connection, aid);
			connection.commit();
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
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updatePermission(IAccessPermission perm) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			accessPermissionAccessor.updatePermission(connection, perm);
			connection.commit();
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
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addUser(IUser user) throws RemoteException {

		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long uid =  userAccessor.addUser(connection, user);
			connection.commit();
			return uid;
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
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void deleteUser(long id) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			userAccessor.deleteUser(connection, id);
			connection.commit();
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
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void updateUser(IUser user) throws RemoteException {
		// TODO Auto-generated method stub
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			userAccessor.updateUser(connection, user);
			connection.commit();
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
			throw new Phase2Exception(e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
}
