package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IApertureConfig;
import ngat.phase2.XApertureConfig;

import org.apache.log4j.Logger;

public class ApertureConfigAccessor  {
	
	static Logger logger = Logger.getLogger(AcquisitionConfigAccessor.class);
	
	/*
	 EA_APERTURE_CONFIG;
	 	id				int
	 	configure	int
	*/
	
	//statements
	
	public static final String INSERT_APERTURE_CONFIG_SQL = 						
		"insert into EA_APERTURE_CONFIG() values ()";
	 
	public static final String GET_APERTURE_CONFIG_COUNT_SQL = 							
		"select " +
		"count(*) " +
		"from " +
		"EA_APERTURE_CONFIG " +
		"where id=?";
	
	public static final String DEL_APERTURE_CONFIG_SQL = 								
		"delete from EA_APERTURE_CONFIG where id = ?";

	/** Public methods *******************************************************************/
	
	public IApertureConfig getApertureConfig(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_APERTURE_CONFIG_COUNT_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_APERTURE_CONFIG_COUNT_SQL);
			XApertureConfig apertureConfig = null;
			
			int count = -1;
			if (resultSet.next()) {
				//if the config exists, the query will return a 1
				count = resultSet.getInt(1);
			}
			if (count ==1) {
				return new XApertureConfig();
			} else {
				throw new Exception("cannot find aperture config with id=" +id);
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
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	public long insertApertureConfig(Connection connection, IApertureConfig apertureConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			XApertureConfig xApertureConfig = (XApertureConfig)apertureConfig;
			
			stmt = connection.prepareStatement(INSERT_APERTURE_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_APERTURE_CONFIG_SQL, true);
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
