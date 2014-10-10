package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IAutoguiderConfig;
import ngat.phase2.XAutoguiderConfig;

import org.apache.log4j.Logger;

public class AutoguiderConfigAccessor  {
	
	static Logger logger = Logger.getLogger(AutoguiderConfigAccessor.class);
	
	/* 
	 EA_AUTOGUIDER_CONFIG;
	 	id			int
	 	mode	int
	 	name    String
	*/
	
	//statements
	public static final String INSERT_AUTOGUIDER_CONFIG_SQL = 		
		"insert into EA_AUTOGUIDER_CONFIG (" +
		"mode, name" + 
		") values (" + 
		"?, ?)";
	
	public static final String GET_AUTOGUIDER_CONFIG_SQL = 							
		"select " +
		"mode, name " +
		"from " +
		"EA_AUTOGUIDER_CONFIG " +
		"where id=?";
	
	public static final String DEL_AUTOGUIDER_CONFIG_SQL = 								
		"delete from EA_AUTOGUIDER_CONFIG where id = ?";
															
	/** Public methods *******************************************************************/
	
	public IAutoguiderConfig getAutoguiderConfig(Connection connection, long id) throws Exception {
	
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_AUTOGUIDER_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_AUTOGUIDER_CONFIG_SQL);
			
			int mode;
			String name;;
			XAutoguiderConfig autoguiderConfig = null;
			
			if (resultSet.next()) {
				mode	= resultSet.getInt(1);
				name	= resultSet.getString(2);
	
				autoguiderConfig = new XAutoguiderConfig(mode, name);			
			}
			return autoguiderConfig;
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
	
	public long insertAutoguiderConfig(Connection connection, IAutoguiderConfig autoguiderConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			XAutoguiderConfig xautoguiderConfig = (XAutoguiderConfig)autoguiderConfig;
			
			stmt = connection.prepareStatement(INSERT_AUTOGUIDER_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setInt(1, xautoguiderConfig.getAutoguiderCommand());
			stmt.setString(2, xautoguiderConfig.getAutoguiderName());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_AUTOGUIDER_CONFIG_SQL, true);
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
