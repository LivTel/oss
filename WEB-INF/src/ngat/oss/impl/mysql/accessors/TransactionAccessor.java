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
import ngat.phase2.ITransaction;
import ngat.phase2.XTransaction;

import org.apache.log4j.Logger;

public class TransactionAccessor {
	
	/*	
	TRANSACTION;
		id       		int
		aid      		int
		clientRef	String
		time	      	datetime
		amount    	double
		comment   String
	*/
	
	static Logger logger = Logger.getLogger(TransactionAccessor.class);
	//statements
	
	public static final String INSERT_TRANS_SQL = 						
		"insert into TRANSACTION (" +
		"aid, clientRef, time, amount, comment, balanceType" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?)";

	public static final String GET_TRANS_SQL = 							
		"select " +
		"id, aid, clientRef, time, amount, comment, balanceType " +
		"from " +
		"TRANSACTION " +
		"where id=?";
	
	public static final String LIST_TRANS_SQL = 							
		"select " +
		"id, aid, clientRef, time, amount, comment, balanceType " +
		"from " +
		"TRANSACTION " +
		"where aid=?";
	
	public static final String DEL_TRANS_SQL = 							
		"delete from TRANSACTION where id = ?";
	
	public static final String DEL_ACC_TRANS_SQL = 							
		"delete from TRANSACTION where aid = ?";
	
	public static final String UPDATE_TRANS_SQL =					
		"update TRANSACTION "+
		"set " + 
		"aid=?," + 
		"clientRef=?, " +
		"time=?, " +
		"amount=?, " +
		"comment=? " +
		"balanceType=? " +
		"where id=?";

	
	/** Public methods *******************************************************************/
	public long addTransaction(Connection connection, long aid, ITransaction transaction) throws Exception{
		PreparedStatement stmt = null;
		try {
			//table fields
			long time;
			double amount;
			String clientRef, comment; 
			int balanceType;
			
			//load values
			clientRef = transaction.getClientReference();
			time =transaction.getTime();
			amount = transaction.getAmount();
			comment = transaction.getComment();
			balanceType = transaction.getBalanceType();
			
			//prepare statement
			stmt = connection.prepareStatement(INSERT_TRANS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, aid);
			stmt.setString(2, clientRef);
			stmt.setLong(3, time);
			stmt.setDouble(4, amount);
			stmt.setString(5, comment);
			stmt.setInt(6, balanceType);
	
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_TRANS_SQL, true);
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

	
	public void deleteTransaction(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_TRANS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_TRANS_SQL, true);
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
	
	public void deleteAccountTransactions(Connection connection, long aid) throws Exception  {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_ACC_TRANS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, aid);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_ACC_TRANS_SQL, true);
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
	
	public ITransaction getTransaction(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_TRANS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TRANS_SQL);
			ITransaction transaction = null;
			if (resultSet.next()) {
				transaction = getTransactionFromResultSetCursor(resultSet);
			}
			return transaction;
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
	
	public List listTransactions(Connection connection, long accountId) throws Exception {
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(LIST_TRANS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, accountId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_TRANS_SQL);
			ArrayList list = new ArrayList();
			while (resultSet.next()) {
				ITransaction transaction = getTransactionFromResultSetCursor(resultSet);
				list.add(transaction);
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
	
	
	/* Private  methods ******************************************************************/

	/**
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private ITransaction getTransactionFromResultSetCursor(ResultSet resultSet) throws Exception {

		long id, time, aid;
		double amount;
		String clientRef, comment; 
		int balanceType;
		
		id 					= resultSet.getLong(1);
		aid					= resultSet.getLong(2);
		clientRef 		= resultSet.getString(3);
		time					= resultSet.getLong(4);
		amount			= resultSet.getDouble(5);
		comment 		= resultSet.getString(6);
		balanceType	= resultSet.getInt(7);
		
		XTransaction transaction = new XTransaction();
		transaction.setAmount(amount);
		transaction.setClientReference(clientRef);
		transaction.setComment(comment);
		transaction.setID(id);
		transaction.setTime(time);
		transaction.setBalanceType(balanceType);
		return transaction;
	}
}
