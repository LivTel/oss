package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.GroupTypes;
import ngat.phase2.ITimingConstraint;
import ngat.phase2.XEphemerisTimingConstraint;
import ngat.phase2.XFixedTimingConstraint;
import ngat.phase2.XFlexibleTimingConstraint;
import ngat.phase2.XMinimumIntervalTimingConstraint;
import ngat.phase2.XMonitorTimingConstraint;

import org.apache.log4j.Logger;

public class TimingConstraintAccessor {
	
	static Logger logger = Logger.getLogger(TimingConstraintAccessor.class);
	
	/*
	 TIMING_CONSTRAINT
	 	id				long
	 	type			int
	 	start			double
	 	end			double
	 	period		double
	 	window		double
	 	maxCount	int
	 	phase		double
	 */
	
	//statements
	//GET **************************
	public static final String GET_TIMING_CONSTR = 
			"select "
			+ "type, start, end, period, window, maxCount, phase from TIMING_CONSTRAINT where id=?";
	
	//LIST **************************
	public static final String LIST_TIMING_CONSTRAINTS_SQL = 		
		"select " +
		"id, type, start, end, period, window, maxCount, phase " +
		"from " +
		"TIMING_CONSTRAINT";
	
	public static final String LIST_TIMING_CONSTRAINTS_OF_TYPE_SQL = 
		"select " +
		"id, start, end, period, window, maxCount, phase " +
		"from " +
		"TIMING_CONSTRAINT where type=? order by start";
	
	public static final String LIST_FIXED_TIMING_CONSTRAINTS_WITH_END_AFTER_SQL = 
		"select " +
		"id, start, end, period, window, maxCount, phase " +
		"from " +
		"TIMING_CONSTRAINT where type=5 and (start + window / 2) >= ? order by start";
	
	public static final String LIST_TIMING_CONSTRAINTS_OF_TYPE_WITH_END_AFTER_SQL = 
		"select " +
		"id, start, end, period, window, maxCount, phase " +
		"from " +
		"TIMING_CONSTRAINT where type=? and end >= ? order by start";
	
	//INSERT **********************
	public static final String INSERT_TIMING_CONSTR_PREFIX_SQL = 
			"insert into TIMING_CONSTRAINT ";

	public final String INSERT_FIXED_TIMING_CONSTR = INSERT_TIMING_CONSTR_PREFIX_SQL
			+ "(type, start, window ) values (?, ?, ?)";

	public final String INSERT_FLEXIBLE_TIMING_CONSTR = INSERT_TIMING_CONSTR_PREFIX_SQL
			+ "(type, start, end ) values (?, ?, ?)";

	public final String INSERT_MONITOR_TIMING_CONSTR = INSERT_TIMING_CONSTR_PREFIX_SQL
			+ "(type, start, end, period, window ) values (?, ?, ?, ?, ?)";

	public final String INSERT_EPHEMERIS_TIMING_CONSTR = INSERT_TIMING_CONSTR_PREFIX_SQL
			+ "(type, start, end, period, window, phase ) values (?, ?, ?, ?, ?, ?)";

	public final String INSERT_MIN_INTERVAL_TIMING_CONSTR = INSERT_TIMING_CONSTR_PREFIX_SQL
			+ "(type, start, end, period, maxCount ) values (?, ?, ?, ?, ?)";

	//DELETE **********************
	
	public static final String DEL_TIMING_CONSTR_SQL = 
			"delete from TIMING_CONSTRAINT where id = ?";
	
	/** Public methods *****************************************************************
	 * @throws Exception */
	
	public ITimingConstraint getTimingConstraint(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_TIMING_CONSTR, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			ITimingConstraint timingConstraint = null;
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TIMING_CONSTR);
			while (resultSet.next()) {
				//type, start, end, period, window, maxCount, phase
			 	int type = resultSet.getInt(1);
			 	long start = resultSet.getLong(2);
			 	long end = resultSet.getLong(3);
			 	long period = resultSet.getLong(4);
			 	long window = resultSet.getLong(5);
			 	int maxCount = resultSet.getInt(6);
			 	double phase = resultSet.getDouble(7);
				
				timingConstraint = createTimingConstraint(id, type, start, end, period, window, maxCount, phase);
			}
			return timingConstraint;
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
	
	//list all timing constraints
	public List listTimingConstraints(Connection connection) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(LIST_TIMING_CONSTRAINTS_SQL, Statement.RETURN_GENERATED_KEYS);
	
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_TIMING_CONSTRAINTS_SQL);
			List list = new ArrayList();
			while (resultSet.next()) {
				//id, type, start, end, period, window, maxCount, phase
				long id = resultSet.getLong(1);
			 	int type = resultSet.getInt(2);
			 	long start = resultSet.getLong(3);
			 	long end = resultSet.getLong(4);
			 	long period = resultSet.getLong(5);
			 	long window = resultSet.getLong(6);
			 	int maxCount = resultSet.getInt(7);
			 	double phase = resultSet.getDouble(8);
				
				ITimingConstraint timingConstraint = createTimingConstraint(id, type, start, end, period, window, maxCount, phase);
				list.add(timingConstraint);
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
	
	//list timing constraints of type (where type is in GroupTypes.EPHEMERIS_GROUP ... etc)
	public List listTimingConstraints(Connection connection, int type) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(LIST_TIMING_CONSTRAINTS_OF_TYPE_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, type);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_TIMING_CONSTRAINTS_OF_TYPE_SQL);
			List list = new ArrayList();
			while (resultSet.next()) {
				//id, start, end, period, window, maxCount, phase
				long id = resultSet.getLong(1);
			 	long start = resultSet.getLong(2);
			 	long end = resultSet.getLong(3);
			 	long period = resultSet.getLong(4);
			 	long window = resultSet.getLong(5);
			 	int maxCount = resultSet.getInt(6);
			 	double phase = resultSet.getDouble(7);
				
				ITimingConstraint timingConstraint = createTimingConstraint(id, type, start, end, period, window, maxCount, phase);
				list.add(timingConstraint);
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
	
	//list timing constraints of type (where type is in GroupTypes.EPHEMERIS_GROUP ... etc)
	//and end of TimingConstraint >= cutOff
	public List listTimingConstraints(Connection connection, int type, long cutOff) throws Exception {
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			String statementString;
			if (type == GroupTypes.FIXED_GROUP) {
				statementString = LIST_FIXED_TIMING_CONSTRAINTS_WITH_END_AFTER_SQL;
				stmt = connection.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
				stmt.setLong(1, cutOff);
			} else {
				statementString = LIST_TIMING_CONSTRAINTS_OF_TYPE_WITH_END_AFTER_SQL;
				stmt = connection.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
				stmt.setLong(1, type);
				stmt.setLong(2, cutOff);
			}
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, statementString);
			List list = new ArrayList();
			while (resultSet.next()) {
				//id, start, end, period, window, maxCount, phase
				long id = resultSet.getLong(1);
			 	long start = resultSet.getLong(2);
			 	long end = resultSet.getLong(3);
			 	long period = resultSet.getLong(4);
			 	long window = resultSet.getLong(5);
			 	int maxCount = resultSet.getInt(6);
			 	double phase = resultSet.getDouble(7);
				
				ITimingConstraint timingConstraint = createTimingConstraint(id, type, start, end, period, window, maxCount, phase);
				list.add(timingConstraint);
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
	
	public void deleteTimingConstraint(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(DEL_TIMING_CONSTR_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_TIMING_CONSTR_SQL, false);
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
	
	public int insertTimingConstraint(Connection connection, ITimingConstraint timingConstraint) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			if (timingConstraint == null) {
				return -1;
			}
	
			if (timingConstraint.getClass() == XFixedTimingConstraint.class) {
	
				stmt = connection.prepareStatement(INSERT_FIXED_TIMING_CONSTR, Statement.RETURN_GENERATED_KEYS);
				XFixedTimingConstraint fixedConstraints = (XFixedTimingConstraint) timingConstraint;
				
				//type, start, window
				stmt.setInt(1, GroupTypes.FIXED_GROUP);
				stmt.setDouble(2, fixedConstraints.getFixedTime());
				stmt.setDouble(3, fixedConstraints.getSlack());
				return DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_FIXED_TIMING_CONSTR, true);
	
			} else if (timingConstraint.getClass() == XFlexibleTimingConstraint.class) {
	
				stmt = connection.prepareStatement(INSERT_FLEXIBLE_TIMING_CONSTR, Statement.RETURN_GENERATED_KEYS);
				XFlexibleTimingConstraint flexibleConstraints = (XFlexibleTimingConstraint) timingConstraint;
				
				//type, start, end
				stmt.setInt(1, GroupTypes.FLEXIBLE_GROUP);
				stmt.setLong(2, flexibleConstraints.getActivationDate());
				stmt.setLong(3, flexibleConstraints.getExpiryDate());
				return DatabaseTransactor.getInstance().executeUpdateStatement(
						connection, stmt, INSERT_FLEXIBLE_TIMING_CONSTR, true);
	
			} else if (timingConstraint.getClass() == XMonitorTimingConstraint.class) {
	
				stmt = connection.prepareStatement(INSERT_MONITOR_TIMING_CONSTR, Statement.RETURN_GENERATED_KEYS);
				XMonitorTimingConstraint monitorConstraints = (XMonitorTimingConstraint) timingConstraint;
				
				//type, start, end, period, window
				stmt.setInt(1, GroupTypes.MONITOR_GROUP);
				stmt.setDouble(2, monitorConstraints.getStartDate());
				stmt.setDouble(3, monitorConstraints.getEndDate());
				stmt.setLong(4, monitorConstraints.getPeriod());
				stmt.setLong(5, monitorConstraints.getWindow());
				return DatabaseTransactor.getInstance().executeUpdateStatement(
						connection, stmt, INSERT_MONITOR_TIMING_CONSTR, true);
	
			} else if (timingConstraint.getClass() == XEphemerisTimingConstraint.class) {
	
				stmt = connection.prepareStatement(INSERT_EPHEMERIS_TIMING_CONSTR, Statement.RETURN_GENERATED_KEYS);
				XEphemerisTimingConstraint ephemerisConstraints = (XEphemerisTimingConstraint) timingConstraint;
				
				//type, start, end, period, window, phase
				stmt.setInt(1, GroupTypes.EPHEMERIS_GROUP);
				stmt.setDouble(2, ephemerisConstraints.getStart());
				stmt.setDouble(3, ephemerisConstraints.getEnd());
				stmt.setLong(4, ephemerisConstraints.getCyclePeriod());
				stmt.setLong(5, ephemerisConstraints.getWindow());
				stmt.setDouble(6, ephemerisConstraints.getPhase());
				
				return DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_EPHEMERIS_TIMING_CONSTR, true);
	
			} else if (timingConstraint.getClass() == XMinimumIntervalTimingConstraint.class) {
	
				stmt = connection.prepareStatement(INSERT_MIN_INTERVAL_TIMING_CONSTR, Statement.RETURN_GENERATED_KEYS);
				XMinimumIntervalTimingConstraint repeatableConstraints = (XMinimumIntervalTimingConstraint) timingConstraint;
				
				//type, start, end, period, maxCount
				stmt.setInt(1, GroupTypes.INTERVAL_GROUP);
				stmt.setDouble(2, repeatableConstraints.getStart());
				stmt.setDouble(3, repeatableConstraints.getEnd());
				stmt.setLong(4, repeatableConstraints.getMinimumInterval());
				stmt.setLong(5, repeatableConstraints.getMaximumRepeats());
				return DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_MIN_INTERVAL_TIMING_CONSTR, true);
	
			} else {
				throw new Phase2Exception("Unknown timing constraint type");
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
	
	
	//** Private methods *****************************************************************
	
	private ITimingConstraint createTimingConstraint(long id, int type, long start, long end, long period, long window, int maxCount, double phase) throws Phase2Exception {
		ITimingConstraint timingConstraints = null;
		switch (type) {
		case GroupTypes.EPHEMERIS_GROUP:
			XEphemerisTimingConstraint xEphemerisTimingConstraint = new XEphemerisTimingConstraint();
			xEphemerisTimingConstraint.setCyclePeriod(period);
			xEphemerisTimingConstraint.setEnd(end);
			xEphemerisTimingConstraint.setPhase(phase);
			xEphemerisTimingConstraint.setStart(start);
			xEphemerisTimingConstraint.setWindow(window);
			timingConstraints = xEphemerisTimingConstraint;
			break;
		case GroupTypes.FIXED_GROUP:
			XFixedTimingConstraint xFixedConstraints = new XFixedTimingConstraint();
			xFixedConstraints.setFixedTime(start);
			xFixedConstraints.setSlack(window);
			timingConstraints = xFixedConstraints;
			break;
		case GroupTypes.FLEXIBLE_GROUP:
			XFlexibleTimingConstraint xFlexibleConstraints = new XFlexibleTimingConstraint();
			xFlexibleConstraints.setActivationDate(start);
			xFlexibleConstraints.setExpiryDate(end);
			timingConstraints = xFlexibleConstraints;
			break;
		case GroupTypes.MONITOR_GROUP:
			XMonitorTimingConstraint xMonitorConstraints = new XMonitorTimingConstraint();
			xMonitorConstraints.setEndDate(end);
			xMonitorConstraints.setPeriod(period);
			xMonitorConstraints.setStartDate(start);
			xMonitorConstraints.setWindow(window);
			timingConstraints = xMonitorConstraints;
			break;
		case GroupTypes.INTERVAL_GROUP:
			XMinimumIntervalTimingConstraint xMinimumIntervalTimingConstraint = new XMinimumIntervalTimingConstraint();
			xMinimumIntervalTimingConstraint.setEnd(end);
			xMinimumIntervalTimingConstraint.setMaximumRepeats(maxCount);
			xMinimumIntervalTimingConstraint.setMinimumInterval(period);
			xMinimumIntervalTimingConstraint.setStart(start);
			timingConstraints = xMinimumIntervalTimingConstraint;
			break;
		default:
			throw new Phase2Exception("unknown timing constraints type");
		}
		return timingConstraints;
	}
}