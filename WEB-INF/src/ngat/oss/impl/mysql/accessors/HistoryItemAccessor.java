package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IExecutionFailureContext;
import ngat.phase2.IHistoryItem;
import ngat.phase2.IQosMetric;
import ngat.phase2.XExposureInfo;
import ngat.phase2.XHistoryItem;

import org.apache.log4j.Logger;

public class HistoryItemAccessor {
	
	static Logger logger = Logger.getLogger(HistoryItemAccessor.class);
	
	/*
	HISTORY_ITEM;
		id							int
		gid						int
		scheduledTime		double
		completionStatus	int
		completionTime		double
		errorCode				int
		errorMessage			String
	*/
	
	public static final String ADD_HISTORY_ITEM_SQL = 							
		"insert into HISTORY_ITEM " + 
		"(gid, scheduledTime, errorCode) " + 
		"values " + 
		"(?, ?, ?) ";
	
	public static final String LIST_HISTORY_ITEMS_OF_GROUP_SQL = 							
		"select " +
		"id, gid, scheduledTime, completionStatus, completionTime, errorCode, errorMessage " +
		"from " + 
		"HISTORY_ITEM " + 
		"where gid=? " +
		"order by scheduledTime";
	
	public static final String LIST_EXPOSURE_ITEMS_SQL = 
       "select " + 
       "id, time, fileName " + 
       "from " + 
       "EXPOSURE_ITEM " + 
       "where hid = ?";
	
	public static final String UPDATE_HISTORY_ITEM_COMPLETION_STATUS_SQL = "update HISTORY_ITEM set completionStatus = ? where id = ?"; 			
	public static final String UPDATE_HISTORY_ITEM_COMPLETION_TIME_SQL = "update HISTORY_ITEM set completionTime = ? where id = ?"; 			
	public static final String UPDATE_HISTORY_ITEM_COMPLETION_ERROR_CODE_SQL = "update HISTORY_ITEM set errorCode = ? where id = ?";
	public static final String UPDATE_HISTORY_ITEM_COMPLETION_ERROR_MESSAGE_SQL = "update HISTORY_ITEM set errorMessage = ? where id = ?"; 			
	
	public static final String ADD_EXPOSURE_ITEM_SQL = 							
		"insert into EXPOSURE_ITEM  " + 
		"(hid, time, fileName) " + 
		"values " + 
		"(?, ?, ?) ";
	
	public static final String ADD_QOS_ITEM_SQL = 							
		"insert into QOS_ITEM  " + 
		"(hid, qosName, qosValue) " + 
		"values " + 
		"(?, ?, ?) ";
	
	public long addHistoryItem(Connection connection, long gid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(ADD_HISTORY_ITEM_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);
			stmt.setDouble(2,  (double)(System.currentTimeMillis()));
			stmt.setInt(3, 0);
			
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, ADD_HISTORY_ITEM_SQL, true);
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
	
	public void updateHistory(Connection connection, long hid, int cstat, long ctime, IExecutionFailureContext efc, Set qosStats) throws Exception {
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;
		PreparedStatement stmt5 = null;
		try {
		    stmt1 = connection.prepareStatement(UPDATE_HISTORY_ITEM_COMPLETION_STATUS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt1.setInt(1, cstat);
			stmt1.setLong(2, hid);
			int numRows1 = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt1, UPDATE_HISTORY_ITEM_COMPLETION_STATUS_SQL, false);
			
			stmt2 = connection.prepareStatement(UPDATE_HISTORY_ITEM_COMPLETION_TIME_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt2.setDouble(1, (double)ctime);
			stmt2.setLong(2, hid);
			int numRows2 = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt2, UPDATE_HISTORY_ITEM_COMPLETION_TIME_SQL, false);
			
			stmt3 = connection.prepareStatement(UPDATE_HISTORY_ITEM_COMPLETION_ERROR_CODE_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt3.setInt(1, (efc == null ? 0 : efc.getErrorCode()));
			stmt3.setLong(2, hid);
			int numRows3 = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt3, UPDATE_HISTORY_ITEM_COMPLETION_ERROR_CODE_SQL, false);
			
			String errmsg;
			if (efc == null) {
				errmsg = "OKAY";
			} else {
				errmsg = efc.getErrorMessage();
				if (errmsg.length() > 60) {
					errmsg = errmsg.substring(0, 60);
				}
			}
			stmt4 = connection.prepareStatement(UPDATE_HISTORY_ITEM_COMPLETION_ERROR_MESSAGE_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt4.setString(1, errmsg);
			stmt4.setLong(2, hid);
			int numRows4 = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt4, UPDATE_HISTORY_ITEM_COMPLETION_ERROR_MESSAGE_SQL, false);
			
		    // Insert any QOS statistics here
		    Iterator qi = qosStats.iterator();
		    while (qi.hasNext()) {
		    	IQosMetric qm = (IQosMetric) qi.next();
		    	
		    	stmt5 = connection.prepareStatement(ADD_QOS_ITEM_SQL, Statement.RETURN_GENERATED_KEYS);
				stmt5.setLong(1, hid);
				stmt5.setString(2,  qm.getMetricID());
				stmt5.setDouble(3, qm.getMetricValue());
				
				long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt5, ADD_QOS_ITEM_SQL, true);
		   }
	    } finally {
			try {
				if (stmt1 != null) {
					stmt1.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
			try {
				if (stmt2 != null) {
					stmt2.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
			try {
				if (stmt3 != null) {
					stmt3.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
			try {
				if (stmt4 != null) {
					stmt4.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
			try {
				if (stmt5 != null) {
					stmt5.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
   } 
	
	public long addExposureUpdate(Connection connection, long hid, long expId, long expTime, String fileName) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(ADD_EXPOSURE_ITEM_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, hid);
			stmt.setLong(2, expId);
			stmt.setDouble(3, (double)expTime);
			stmt.setString(4, fileName);
			
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, ADD_EXPOSURE_ITEM_SQL, true);
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

   public List listExposureItems(Connection connection, long histID) throws Exception {

	    PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
		   	stmt = connection.prepareStatement(LIST_EXPOSURE_ITEMS_SQL, Statement.RETURN_GENERATED_KEYS);
		   	stmt.setLong(1, histID);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_EXPOSURE_ITEMS_SQL);
			
			ArrayList itemList = new ArrayList();
			if (resultSet == null) {
				return itemList;
			}
			
			while (resultSet.next()) {
				XExposureInfo expinfo = new XExposureInfo();
				expinfo.setID(resultSet.getInt(1));
				expinfo.setExposureTime((long)(resultSet.getDouble(2)));
				expinfo.setFileName(resultSet.getString(3));
				itemList.add(expinfo);
			}
			return itemList;
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

	public List listHistoryItems(Connection connection, long groupId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(LIST_HISTORY_ITEMS_OF_GROUP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, groupId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_HISTORY_ITEMS_OF_GROUP_SQL);
			
			ArrayList itemList = new ArrayList();
			if (resultSet == null) {
				return itemList;
			}
			
			while (resultSet.next()) {
				IHistoryItem item = getHistoryItemFromResultSetCursor(resultSet);
	            itemList.add(item);
			}
			return itemList;
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
	
	private IHistoryItem getHistoryItemFromResultSetCursor(ResultSet resultSet) throws SQLException {
		long id, gid;
		double scheduledTime, completionTime;
		int completionStatus, errorCode;
		String errorMessage;

		id 							= resultSet.getLong(1);
		gid 						= resultSet.getLong(2);
		scheduledTime		= resultSet.getDouble(3);
		completionStatus	= resultSet.getInt(4);
		completionTime		= resultSet.getDouble(5);
		errorCode				= resultSet.getInt(6);
		errorMessage			= resultSet.getString(7);
		
		XHistoryItem historyItem = new XHistoryItem();
        historyItem.setID(id);
        historyItem.setScheduledTime((long)scheduledTime);
        historyItem.setCompletionStatus(completionStatus);
        historyItem.setCompletionTime((long)completionTime);
        historyItem.setErrorCode(errorCode);
        historyItem.setErrorMessage(errorMessage);
		return historyItem;
	}
}
