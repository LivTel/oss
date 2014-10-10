package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.ExposureTypes;
import ngat.phase2.IExposure;
import ngat.phase2.XMultipleExposure;
import ngat.phase2.XPeriodExposure;
import ngat.phase2.XPeriodRunAtExposure;

import org.apache.log4j.Logger;

public class ExposureAccessor  {
	
	static Logger logger = Logger.getLogger(ExposureAccessor.class);
	
	/*
	EA_EXPOSURE;
	id						int
	exposureTime	double
	repeats      		int
	standard			boolean
	type					int
	*/
	
	//statements
	
	public static final String INSERT_EXPOSURE_SQL = 						
		"insert into EA_EXPOSURE (" +
		"exposureTime, repeats, standard, type, totalDuration, runAtTime " +
		") values (" + 
		"?, ?, ?, ?, ?, ?)";
	
	public static final String GET_EXPOSURE_SQL = 							
		"select " +
		"exposureTime, repeats, standard, type, totalDuration, runAtTime " +
		"from " +
		"EA_EXPOSURE " +
		"where id=?";
	
	public static final String DEL_EXPOSURE_SQL = 								
		"delete from EA_EXPOSURE where id = ?";
	
	public static final String UPDATE_EXPOSURE_SQL =						
		"update EA_EXPOSURE "+
		"set " +
		"exposureTime=?, " +
		"repeats=?, " +
		"standard=?, " +
		"type=?, " +
		"totalDuration=?, " +
		"runAtTime=?" +
		" where id=?";

	/** Public methods *******************************************************************/
	
	public IExposure getExposure(Connection connection, long id) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_EXPOSURE_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_EXPOSURE_SQL);
			
			double exposureTime, totalDuration;
			int repeats, type;
			boolean standard;
			long runAtTime;
			
			if (resultSet.next()) {
				exposureTime	= resultSet.getDouble(1);
				repeats				= resultSet.getInt(2);
				standard			= resultSet.getBoolean(3);
				type 					= resultSet.getInt(4);
				totalDuration		= resultSet.getDouble(5);
				runAtTime			= resultSet.getLong(6);
				switch (type) {
					case ExposureTypes.MULT_RUN:
						return new XMultipleExposure(exposureTime, repeats, standard);
					case ExposureTypes.PERIOD:
						return new XPeriodExposure(exposureTime, standard);
					case ExposureTypes.PERIOD_RUN_AT:
						return new XPeriodRunAtExposure(exposureTime, totalDuration, runAtTime);
				}			
			}
			return null;
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
	
	public long insertExposure(Connection connection, IExposure exposure) throws Exception {
		
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(INSERT_EXPOSURE_SQL, Statement.RETURN_GENERATED_KEYS);
	
			double exposureTime, totalDuration;
			int repeats, type;
			boolean standard;
			long runAtTime;
			
			if (exposure instanceof XMultipleExposure) {
				type = ExposureTypes.MULT_RUN;
				XMultipleExposure multipleExposure = (XMultipleExposure)exposure;
				exposureTime = multipleExposure.getExposureTime();
				repeats = multipleExposure.getRepeatCount();
				standard = multipleExposure.isStandard();
				totalDuration = 0;
				runAtTime = 0;
			} else if (exposure instanceof XPeriodExposure) {
				type = ExposureTypes.PERIOD;
				XPeriodExposure periodExposure = (XPeriodExposure)exposure;
				exposureTime = periodExposure.getExposureTime();
				repeats = 0;
				standard = periodExposure.isStandard();
				totalDuration = 0;
				runAtTime = 0;
			} else if (exposure instanceof XPeriodRunAtExposure) {
				type = ExposureTypes.PERIOD_RUN_AT;
				XPeriodRunAtExposure periodRunAtExposure = (XPeriodRunAtExposure)exposure;
				exposureTime = periodRunAtExposure.getExposureLength();
				repeats = 0;
				standard = periodRunAtExposure.isStandard();
				totalDuration = periodRunAtExposure.getTotalExposureDuration();
				runAtTime = periodRunAtExposure.getRunAtTime();
			} else {
				throw new Exception("Unknown exposure type class: " + exposure.getClass().getName());
			}
			
			//exposureTime, repeats, standard, type, totalDuration, runAtTime
			stmt.setDouble(1, exposureTime);
			stmt.setInt(2, repeats);
			stmt.setBoolean(3, standard);
			stmt.setInt(4, type);
			stmt.setDouble(5, totalDuration);
			stmt.setLong(6, runAtTime);
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_EXPOSURE_SQL, true);
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
}
