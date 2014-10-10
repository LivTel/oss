package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IRotatorConfig;
import ngat.phase2.XRotatorConfig;

import org.apache.log4j.Logger;

public class RotatorConfigAccessor  {
	static Logger logger = Logger.getLogger(RotatorConfigAccessor.class);
	
	/* 
	 EA_ROTATOR_CONFIG
	 	id				int
	 	mode		int
	 	angle			int
	 	instrument	String
	*/
	
	//statements
	public static final String INSERT_ROTATOR_CONFIG_SQL = 		
		"insert into EA_ROTATOR_CONFIG (" +
		"mode, angle, instrument" +
		") values (" +
		"?, ?, ?)";
	
	public static final String GET_ROTATOR_CONFIG_SQL = 							
		"select " +
		"mode, angle, instrument " +
		"from " +
		"EA_ROTATOR_CONFIG " +
		"where id=?";
	
	public static final String DEL_ROTATOR_CONFIG_SQL = 								
		"delete from EA_ROTATOR_CONFIG where id = ?";
															
	/** Public methods *******************************************************************/
	
	public IRotatorConfig getRotatorConfig(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_ROTATOR_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_ROTATOR_CONFIG_SQL);
			
			int mode;
			double angle;
			String instrument;
			XRotatorConfig rotatorConfig = null;
			
			if (resultSet.next()) {
				mode				= resultSet.getInt(1);
				angle				= resultSet.getDouble(2);
				instrument		= resultSet.getString(3);
				rotatorConfig 	= new XRotatorConfig(mode, angle, instrument);
			}
			return rotatorConfig;
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
	
	public long insertRotatorConfig(Connection connection, IRotatorConfig rotatorConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			XRotatorConfig xrotatorConfig = (XRotatorConfig)rotatorConfig;
			
			stmt = connection.prepareStatement(INSERT_ROTATOR_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setInt(1, xrotatorConfig.getRotatorMode());
			stmt.setDouble(2, xrotatorConfig.getRotatorAngle());
			stmt.setString(3,  xrotatorConfig.getInstrumentName());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_ROTATOR_CONFIG_SQL, true);
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
