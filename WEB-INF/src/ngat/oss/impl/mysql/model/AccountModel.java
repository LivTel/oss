package ngat.oss.impl.mysql.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.accessors.AccountAccessor;
import ngat.oss.impl.mysql.accessors.SemesterAccessor;
import ngat.oss.impl.mysql.accessors.TestAccessor;
import ngat.oss.impl.mysql.accessors.TransactionAccessor;
import ngat.oss.listeners.AccountingModelUpdateListener;
import ngat.oss.model.IAccountModel;
import ngat.oss.monitor.AccountMonitor;
import ngat.oss.transport.RemotelyPingable;
import ngat.phase2.IAccount;
import ngat.phase2.ISemester;
import ngat.phase2.ISemesterPeriod;
import ngat.phase2.ITransaction;
import ngat.phase2.XAccount;

import org.apache.log4j.Logger;

public class AccountModel extends UnicastRemoteObject implements IAccountModel, AccountMonitor, RemotelyPingable {

	static Logger logger = Logger.getLogger(AccountModel.class);
	
	private int modelType;
	
	//phase2 accessors
	AccountAccessor		accountAccessor;
	TransactionAccessor	transactionAccessor;
	SemesterAccessor 	semesterAccessor;
	TestAccessor 			testAccessor;
	
	//update listeners
	ArrayList updateListeners;
	
	/**
	 * Constructor 
	 * @param modelType
	 * 			category of account model, one of 
	 * 				AccountTypes.PROPOSAL_ACCOUNT_TYPE; AccountTypes.TAG_ACCOUNT_TYPE
	 * @throws RemoteException
	 */
	public AccountModel(int rmiPort, int modelType) throws RemoteException {
		super(rmiPort);
		
		this.modelType = modelType;
		
		accountAccessor 		= new AccountAccessor(modelType);
		transactionAccessor 	= new TransactionAccessor();
		semesterAccessor		=	new SemesterAccessor();
		testAccessor				= new TestAccessor();
		
		updateListeners = new ArrayList();
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
	
	//**************************** ADDS *********************************************************//
	
	public long addAccount(long ownerId, long semesterId, IAccount account) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long aid =  accountAccessor.addAccount(connection, ownerId, semesterId, account);
			connection.commit();
			XAccount xAccount = (XAccount) account;
			xAccount.setID(aid);
			notifyListenersAccountAdded(xAccount, ownerId, semesterId);
			return aid;
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
	
	public void addAllAccountsBetweenSemesters(long ownerId, long startSemesterId, long endSemesterId) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			accountAccessor.addAllAccountsBetweenSemesters(connection, ownerId, startSemesterId, endSemesterId);
			connection.commit();
			////NO NOTIFICATION TO LISTENERS
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
	
	public long addSemester(ISemester semester) throws RemoteException{
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long sid = semesterAccessor.addSemester(connection, semester);
			connection.commit();
			return sid;
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

	public long modifyAllocation(long accountID, double amount, String comment, String clientRef) throws RemoteException{
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long sid = accountAccessor.modifyAllocation(connection,  accountID, amount, comment, clientRef);
			IAccount account = accountAccessor.getAccount(connection, accountID);
			notifyListenersAccountUpdated(account);
			connection.commit();
			return sid;
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

	public long modifyConsumed(long accountID, double amount, String comment, String clientRef) throws RemoteException{
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			long sid = accountAccessor.modifyConsumed(connection,  accountID, amount, comment, clientRef);
			IAccount account = accountAccessor.getAccount(connection, accountID);
			notifyListenersAccountUpdated(account);
			connection.commit();
			return sid;
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
	
	//**************************** DELETES *********************************************************//

	public void deleteAccount(long accountID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			accountAccessor.deleteAccount(connection, accountID);
			connection.commit();
			notifyListenersAccountDeleted(accountID);
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

	public void deleteAccountsOfOwner(long ownerID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			accountAccessor.deleteAccountsOfOwner(connection, ownerID);
			connection.commit();
			//NO NOTIFICATION TO LISTENERS
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

	public void deleteSemester(long id) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			semesterAccessor.deleteSemester(connection, id);
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
	
	public void deleteAccountTransactions(long aid) throws RemoteException {
		
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			transactionAccessor.deleteAccountTransactions(connection, aid);
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

	public void deleteTransaction(long id) throws RemoteException {
		
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			transactionAccessor.deleteTransaction(connection, id);
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
	
	//**************************** GETS *********************************************************//

	public IAccount getAccount(long accountID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accountAccessor.getAccount(connection, accountID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public long getAccountOwnerID(long accountID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accountAccessor.getAccountOwnerID(connection, accountID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public ISemesterPeriod getSemesterPeriodOfDate(long dateTime)  throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return semesterAccessor.getSemesterPeriodOfDate(connection, dateTime);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public ISemester getSemester(long semesterId)  throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return semesterAccessor.getSemester(connection, semesterId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}

	public ITransaction getTransaction(long transID) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return transactionAccessor.getTransaction(connection, transID);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	//**************************** FINDS *********************************************************//
	
	public IAccount findAccount(long ownerId, long semesterId) throws RemoteException {

		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accountAccessor.findAccount(connection, ownerId, semesterId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	//**************************** LISTS *********************************************************//

	public List listSemestersFromDate(long dateTime) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return semesterAccessor.listSemestersFromDate(connection, dateTime);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public List listAccountsOfSemester(long semesterId) throws RemoteException {
		
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accountAccessor.listAccountsOfSemester(connection, semesterId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public List listSemestersForWhichOwnerHasAccounts(long ownerId) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accountAccessor.listSemestersForWhichOwnerHasAccounts(connection, ownerId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	public List listTransactions(long accountId) throws RemoteException {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return transactionAccessor.listTransactions(connection, accountId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	//**************************** UTILITY METHODS ************************************************//
	public List listAccountEntriesOfSemester(long semesterId) throws Exception {
		Connection connection = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			return accountAccessor.listAccountEntriesOfSemester(connection, semesterId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getClass().getName(), e);
		} finally {
			ConnectionPool.getInstance().surrenderConnection(connection);
		}
	}
	
	//**************************** LISTENERS *********************************************************//

	public void addAccountUpdateListener(AccountingModelUpdateListener listener) {
		logger.info(".addAccountUpdateListener(" + listener + ")");
	    if (!updateListeners.contains(listener)) {
	    	updateListeners.add(listener);
	    	logger.info("listener added:" + listener);
	    }	
	}

	public void removeAccountUpdateListener(AccountingModelUpdateListener listener) {
		logger.info(".removeAccountUpdateListener(" + listener + ")");
	    if (updateListeners.contains(listener)) {
	    	updateListeners.remove(listener);
	    	logger.info("listener removed:" +listener );
	    }
	}

	//**************************** PRIVATE METHODS *********************************************************//
	
	private void notifyListenersAccountAdded(IAccount account, long ownerId, long semesterId) {
		logger.info(".notifyListenersAccountAdded(" + account + ")");
		logger.info(".... updateListeners=");
		
		Iterator j = updateListeners.iterator();
		while (j.hasNext()) {
			logger.info(".... ... " + (AccountingModelUpdateListener)j.next());
		}
		logger.info(".... END");
		
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			AccountingModelUpdateListener accountingModelUpdateListener = (AccountingModelUpdateListener)i.next();
			try {
				logger.info("calling accountAdded(" + account + ") on AccountingModelUpdateListener: " +accountingModelUpdateListener);
				accountingModelUpdateListener.accountAdded(account, ownerId, semesterId);
				logger.info("... call completed");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:" +accountingModelUpdateListener );
			}
		}
	}
	
	private void notifyListenersAccountUpdated(IAccount account) {
		logger.info(".notifyListenersAccountUpdated(" + account + ")");
		logger.info(".... updateListeners=");
		
		Iterator j = updateListeners.iterator();
		while (j.hasNext()) {
			logger.info(".... ... " + (AccountingModelUpdateListener)j.next());
		}
		logger.info(".... END");
		
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			AccountingModelUpdateListener accountingModelUpdateListener = (AccountingModelUpdateListener)i.next();
			try {
				logger.info("calling accountUpdated(" + account + ") on AccountingModelUpdateListener: " +accountingModelUpdateListener);
				accountingModelUpdateListener.accountUpdated(account);
				logger.info("... call completed");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:" +accountingModelUpdateListener );
			}
		}
	}
	
	private void notifyListenersAccountDeleted(long aid) {
		logger.info(".notifyListenersAccountDeleted(" + aid + ")");
		logger.info(".... updateListeners=");
		
		Iterator j = updateListeners.iterator();
		while (j.hasNext()) {
			logger.info(".... ... " + (AccountingModelUpdateListener)j.next());
		}
		logger.info(".... END");
		
		Iterator i = updateListeners.iterator();
		while (i.hasNext()) {
			AccountingModelUpdateListener accountingModelUpdateListener = (AccountingModelUpdateListener)i.next();
			try {
				logger.info("calling accountDeleted(" + aid + ") on AccountingModelUpdateListener: " +accountingModelUpdateListener);
				accountingModelUpdateListener.accountDeleted(aid);
				logger.info("... call completed");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				i.remove();
				logger.info("removed unresponsive listener:" +accountingModelUpdateListener );
			}
		}
	}

}
