package ngat.oss.impl.mysql.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.accessors.HistoryItemAccessor;
import ngat.oss.impl.mysql.accessors.TestAccessor;
import ngat.oss.model.IHistoryModel;
import ngat.oss.transport.RemotelyPingable;
import ngat.phase2.IExecutionFailureContext;

import org.apache.log4j.Logger;

public class HistoryModel extends UnicastRemoteObject implements IHistoryModel, RemotelyPingable {

	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleTimeZone UTC = new SimpleTimeZone(0, "UTC");

	static Logger logger = Logger.getLogger(HistoryModel.class);
	
	//phase2 accessors
	HistoryItemAccessor	historyItemAccessor;;
	TestAccessor 			testAccessor;
	
	public HistoryModel(int rmiPort) throws RemoteException {
		super(rmiPort);
		
		historyItemAccessor 	= new HistoryItemAccessor();
		testAccessor				= new TestAccessor();
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
	
	public List listHistoryItems(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return historyItemAccessor.listHistoryItems(connection, groupID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long addHistoryItem(long groupID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long hid = historyItemAccessor.addHistoryItem(connection, groupID);
			connection.commit();
			return hid;
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

	public void updateHistory(long histID, int cstat, long ctime, IExecutionFailureContext efc, Set qosStats) throws RemoteException {
		Connection connection = null;
		try {
		    connection = ConnectionPool.getInstance().getConnection();
		    historyItemAccessor.updateHistory(connection, histID, cstat, ctime, efc, qosStats);
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
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public void addExposureUpdate(long histID, long expID, long expTime, String fileName) throws RemoteException {
		Connection connection = null;
		try {
		    connection = ConnectionPool.getInstance().getConnection();
		    historyItemAccessor.addExposureUpdate(connection, histID, expID, expTime, fileName);
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
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listExposureItems(long histID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return historyItemAccessor.listExposureItems(connection, histID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public List listQosItems(long histID) throws RemoteException {
		throw new RemoteException("unimplemented method: " + this.getClass().getName() + ".listQosItems()");
	}
}
