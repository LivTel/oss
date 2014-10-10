package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IDetectorConfig;
import ngat.phase2.IInstrumentConfig;
import ngat.phase2.IWindow;
import ngat.phase2.XDetectorConfig;
import ngat.phase2.XWindow;

import org.apache.log4j.Logger;

public class DetectorConfigAccessor {

	static Logger logger = Logger.getLogger(DetectorConfigAccessor.class);

	/*
	 * DETECTOR_CONFIG id int xbin int ybin int detectorClassName String
	 */

	/*
	 * DETECTOR_WINDOW id int dcid int x int y int w int h int
	 */

	// statements
	public static final String INSERT_DETECTOR_CONFIG_SQL = 
			"insert into DETECTOR_CONFIG ("
			+ "xbin, ybin) values (?, ?)";

	public static final String INSERT_DETECTOR_WINDOW_SQL = 
			"insert into DETECTOR_WINDOW ("
			+ "dcid, x, y, w, h) values (?, ?, ?, ?, ?)";

	public static final String GET_DETECTOR_CONFIG_SQL = 
			"select id, xbin, ybin from DETECTOR_CONFIG where id=?";

	public static final String GET_DETECTOR_WINDOWS_SQL = "select id, dcid, x, y, w, h from DETECTOR_WINDOW where dcid=?";

	public static final String DEL_DETECTOR_CONFIG_SQL = "delete from DETECTOR_CONFIG where id = ?";

	public static final String DEL_DETECTOR_WINDOW_SQL = "delete from DETECTOR_WINDOW where id = ?";

	public static final String UPDATE_DETECTOR_CONFIG_SQL = "update DETECTOR_CONFIG "
			+ "set "
			+ "id=?,"
			+ "xbin=?,"
			+ "ybin=?,"
			+ "detectorClassName=? "
			+ "where id=?";

	/**
	 * Public methods
	 * ******************************************************************
	 */

	public long addDetectorConfig(Connection connection, IDetectorConfig detectorConfig) throws Exception {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		
		try {
			if (detectorConfig == null) {
				throw new Phase2Exception("instance of IDetectorConfig is null");
			}
	
			// table fields
			int xbin, ybin;
	
			// load values
			xbin = detectorConfig.getXBin();
			ybin = detectorConfig.getYBin();
	
			// prepare statement
			stmt = connection.prepareStatement(INSERT_DETECTOR_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, xbin);
			stmt.setInt(2, ybin);
	
			// execute query - insert Detector Config
			long dcid = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_DETECTOR_CONFIG_SQL, true);
	
			List windowList = detectorConfig.listWindows();
			if (windowList != null) {
				Iterator i = windowList.iterator();
				while (i.hasNext()) {
					// insert each window
					IWindow window = (IWindow) i.next();
					int x, y, w, h;
					x = window.getX();
					y = window.getY();
					w = window.getWidth();
					h = window.getHeight();
	
					// prepare statement, using same connection
					stmt2 = connection.prepareStatement(INSERT_DETECTOR_WINDOW_SQL, Statement.RETURN_GENERATED_KEYS);
					stmt2.setLong(1, dcid);
					stmt2.setInt(2, x);
					stmt2.setInt(3, y);
					stmt2.setInt(4, w);
					stmt2.setInt(5, h);
	
					// execute query - insert Detector Window using same connection
					DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt2, INSERT_DETECTOR_WINDOW_SQL, true);
				}
			}
			return dcid;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
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
		}
	}

	public void deleteDetectorConfig(Connection connection, long dcid)throws Exception {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(DEL_DETECTOR_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, dcid);
	
			// execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_DETECTOR_CONFIG_SQL, true);
			if (numRows == 0) {
				ConnectionPool.getInstance().surrenderConnection(connection);
				throw new Phase2Exception("No rows updated");
			}
			// get detector windows and delete them, using same connection
			stmt2 = connection.prepareStatement(GET_DETECTOR_WINDOWS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt2.setLong(1, dcid);
	
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt2, GET_DETECTOR_WINDOWS_SQL);
			while (resultSet.next()) {
				long dwid = resultSet.getLong(1);
				stmt3 = connection.prepareStatement(DEL_DETECTOR_WINDOW_SQL, Statement.RETURN_GENERATED_KEYS);
				stmt3.setLong(1, dwid);
	
				// execute update, using same connection
				int numRows2 = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_DETECTOR_WINDOW_SQL, true);
				if (numRows2 == 0) {
					throw new Phase2Exception("No rows updated");
				}
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
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
		}
	}

	/**
	 * Not Used at the mo.
	 * 
	 * @param connection
	 * @param dwid
	 * @throws Exception
	 */
	public void deleteDetectorWindow(Connection connection, long dwid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_DETECTOR_WINDOW_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, dwid);
	
			// execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_DETECTOR_WINDOW_SQL, true);
	
			if (numRows == 0) {
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

	public IDetectorConfig getDetectorConfig(Connection connection, long cid) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		ResultSet resultSet2 = null;
		
		try {
			stmt = connection.prepareStatement(GET_DETECTOR_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, cid);
	
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_DETECTOR_CONFIG_SQL);
			
			if (resultSet.next()) {
				int dcid, xbin, ybin;
				String detectorClassName;
		
				dcid = resultSet.getInt(1);
				xbin = resultSet.getInt(2);
				ybin = resultSet.getInt(3);
		
				XDetectorConfig config = new XDetectorConfig();
				config.setID(dcid);
				config.setYBin(ybin);
				config.setXBin(xbin);
		
				// get all windows and add them to detector config
				stmt = connection.prepareStatement(GET_DETECTOR_WINDOWS_SQL, Statement.RETURN_GENERATED_KEYS);
				stmt.setLong(1, dcid);
		
				resultSet2 = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_DETECTOR_WINDOWS_SQL);
				while (resultSet2.next()) {
					long dwid = resultSet2.getLong(1);
					int x = resultSet2.getInt(3);
					int y = resultSet2.getInt(4);
					int w = resultSet2.getInt(5);
					int h = resultSet2.getInt(6);
					XWindow window = new XWindow(x, y, w, h);
					config.addWindow(window);
				}
				return config;	
			} else {
				return null;
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
				if (resultSet2 != null) {
					resultSet2.close();
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

	public void updateDetectorConfig(Connection connection, IInstrumentConfig config, long keyId) throws Exception {

		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(UPDATE_DETECTOR_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, config.getID());
			stmt.setInt(2, config.getDetectorConfig().getXBin());
			stmt.setLong(3, config.getDetectorConfig().getYBin());
			stmt.setString(4, config.getClass().getName());
	
			// execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_DETECTOR_CONFIG_SQL, true);
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

}
