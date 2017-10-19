package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IAcquisitionConfig;
import ngat.phase2.XAcquisitionConfig;

import org.apache.log4j.Logger;

public class AcquisitionConfigAccessor  {
	
	static Logger logger = Logger.getLogger(AcquisitionConfigAccessor.class);
	
	/*
	 EA_ACQUISITION_CONFIG;
	 	id										int
	 	mode  								int
	 	targetInstrument 			String 
	 	acquisitionInstrument 	String 
	 	allowAlternative 			boolean
	 	precision							int
	*/
	
	//statements
	
	public static final String INSERT_ACQUISITION_CONFIG_SQL = 		
		"insert into EA_ACQUISITION_CONFIG (" +
		"mode, targetInstrument, acquisitionInstrument, allowAlternative,`precision`" + //NB: precision is escaped because it's an SQL reserved word
		") values (" +
		"?, ?, ?, ?,?)";
	
	public static final String GET_ACQUISITION_CONFIG_SQL = 							
		"select " +
		"mode, targetInstrument, acquisitionInstrument, allowAlternative,`precision`" + //NB: precision is escaped because it's an SQL reserved word
		"from " +
		"EA_ACQUISITION_CONFIG " +
		"where id=?";
	
	public static final String DEL_ACQUISITION_CONFIG_SQL = 								
		"delete from EA_ACQUISITION_CONFIG where id = ?";

	/** Public methods *******************************************************************/
	
	public IAcquisitionConfig getAcquisitionConfig(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_ACQUISITION_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_ACQUISITION_CONFIG_SQL);
			
			int mode, precision;
			String targetInstrument, acquisitionInstrument;
			boolean allowAlternative;
			XAcquisitionConfig acquisitionConfig = null;
			
			if (resultSet.next()) {
				mode = resultSet.getInt(1);
				targetInstrument = resultSet.getString(2);
				acquisitionInstrument = resultSet.getString(3);
				allowAlternative = resultSet.getBoolean(4);
				precision = resultSet.getInt(5);
				acquisitionConfig = new XAcquisitionConfig(mode, targetInstrument, acquisitionInstrument, allowAlternative, precision);
			}
			return acquisitionConfig;
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
	
	public long insertAcquisitionConfig(Connection connection, IAcquisitionConfig acquisitionConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			XAcquisitionConfig xacquisitionConfig= (XAcquisitionConfig)acquisitionConfig;
			stmt = connection.prepareStatement(INSERT_ACQUISITION_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			int mode = xacquisitionConfig.getMode();
			switch (mode) {
			/*
			public static final String INSERT_ACQUISITION_CONFIG_SQL = 		
			"insert into EA_ACQUISITION_CONFIG (" +
			"mode, targetInstrument, acquisitionInstrument, allowAlternative, precision " +
			") values (" +
			"?, ?, ?, ?,?)";
			 */
				case IAcquisitionConfig.BRIGHTEST: //2
					stmt.setInt(1, mode);
					stmt.setString(2, xacquisitionConfig.getTargetInstrumentName());
					stmt.setString(3, xacquisitionConfig.getAcquisitionInstrumentName());
					stmt.setBoolean(4, xacquisitionConfig.getAllowAlternative());
					stmt.setInt(5, xacquisitionConfig.getPrecision());
					break;
				case IAcquisitionConfig.WCS_FIT: //1
					stmt.setInt(1, mode);
					stmt.setString(2, xacquisitionConfig.getTargetInstrumentName());
					stmt.setString(3, xacquisitionConfig.getAcquisitionInstrumentName());
					stmt.setBoolean(4, xacquisitionConfig.getAllowAlternative());
					stmt.setInt(5, xacquisitionConfig.getPrecision());
					break;
				case IAcquisitionConfig.INSTRUMENT_CHANGE: //3
					stmt.setInt(1, mode);
					stmt.setString(2, xacquisitionConfig.getTargetInstrumentName());
					stmt.setString(3, null); //no acquisition instrument
					stmt.setBoolean(4, false); //don't allow alternative
					stmt.setInt(5, IAcquisitionConfig.PRECISION_NOT_SET); //precision doesn't matter
					break;
			}
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_ACQUISITION_CONFIG_SQL, true);
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

