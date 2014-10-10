package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IOpticalSlideConfig;
import ngat.phase2.XOpticalSlideConfig;

import org.apache.log4j.Logger;

public class OpticalSlideConfigAccessor  {
	
	static Logger logger = Logger.getLogger(OpticalSlideConfigAccessor.class);
	
	/*
	 EA_OPTICALSLIDECONFIG
		  id						int
		  slideNumber 		int
		  positionNumber	int	
     */

	//statements
	public static final String GET_OPTICALSLIDECONFIG_SQL = 							
		"select " +
		"slideNumber, selectedElementName " +
		"from " +
		"EA_OPTICALSLIDECONFIG " +
		"where id=?";
	
	public static final String INSERT_OPTICALSLIDECONFIG_SQL = 						
		"insert into EA_OPTICALSLIDECONFIG (" +
		"slideNumber, selectedElementName " +
		") values (" + 
		"?, ?)";
	
	/** Public methods *******************************************************************/
	
	public IOpticalSlideConfig getOpticalSlideConfig(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_OPTICALSLIDECONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_OPTICALSLIDECONFIG_SQL);
			
			int slideNumber; 
			String selectedElementName;
			
			if (resultSet.next()) {
				slideNumber =  resultSet.getInt(1);
				selectedElementName = resultSet.getString(2);
				
				XOpticalSlideConfig opticalSlideConfig= new XOpticalSlideConfig();
				opticalSlideConfig.setSlide(slideNumber);
				opticalSlideConfig.setElementName(selectedElementName);
				return opticalSlideConfig;
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
	
	public long insertOpticalSlideConfig(Connection connection, IOpticalSlideConfig opticalSlideConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_OPTICALSLIDECONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, opticalSlideConfig.getSlide());
			stmt.setString(2, opticalSlideConfig.getElementName());
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_OPTICALSLIDECONFIG_SQL, true);
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
