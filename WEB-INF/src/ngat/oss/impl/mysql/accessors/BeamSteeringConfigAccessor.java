package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IBeamSteeringConfig;
import ngat.phase2.XBeamSteeringConfig;
import ngat.phase2.XOpticalSlideConfig;

import org.apache.log4j.Logger;

public class BeamSteeringConfigAccessor  {
	
	static Logger logger = Logger.getLogger(BeamSteeringConfigAccessor.class);
	
	/*
		 EA_BEAMSTEERINGCONFIG
		 	id 						int
		 	slideConfig1Ref		int
		 	slideConfig2Ref		int
	 */
	
	//statements
	
	public static final String INSERT_BEAM_STEERING_CONFIG_SQL = 						
		"insert into EA_BEAMSTEERINGCONFIG (" +
		"slideConfig1Ref, slideConfig2Ref " +
		") values (" + 
		"?, ?)";
	
	public static final String GET_BEAM_STEERING_CONFIG_SQL = 							
		"select " +
		"slideConfig1Ref, slideConfig2Ref " +
		"from " +
		"EA_BEAMSTEERINGCONFIG " +
		"where id=?";
	
	public static final String DEL_BEAM_STEERING_CONFIG_SQL = 								
		"delete from EA_BEAMSTEERINGCONFIG where id = ?";

	/** Public methods *******************************************************************/
	
	public IBeamSteeringConfig getBeamSteeringConfig(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_BEAM_STEERING_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_BEAM_STEERING_CONFIG_SQL);
			
			int upperOpticalSlideConfigId, lowerOpticalSlideConfigId; 
			XOpticalSlideConfig upperOpticalSlideConfig = null;
			XOpticalSlideConfig lowerOpticalSlideConfig = null;
			
			if (resultSet.next()) {
				upperOpticalSlideConfigId	= resultSet.getInt(1);
				lowerOpticalSlideConfigId	= resultSet.getInt(2);
				
				OpticalSlideConfigAccessor opticalSlideConfigAccessor = new OpticalSlideConfigAccessor();
				upperOpticalSlideConfig = (XOpticalSlideConfig) opticalSlideConfigAccessor.getOpticalSlideConfig(connection, upperOpticalSlideConfigId);
				lowerOpticalSlideConfig = (XOpticalSlideConfig) opticalSlideConfigAccessor.getOpticalSlideConfig(connection, lowerOpticalSlideConfigId);
				
				XBeamSteeringConfig beamSteeringConfig = new XBeamSteeringConfig(upperOpticalSlideConfig, lowerOpticalSlideConfig);
				return beamSteeringConfig;
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
	
	public long insertBeamSteeringConfig(Connection connection, IBeamSteeringConfig beamSteeringConfig) throws Exception {
		
		PreparedStatement stmt = null;
		try {
			XBeamSteeringConfig xBeamSteeringConfig = (XBeamSteeringConfig) beamSteeringConfig;
			OpticalSlideConfigAccessor opticalSlideConfigAccessor = new OpticalSlideConfigAccessor();
			long upperSlideConfigId = opticalSlideConfigAccessor.insertOpticalSlideConfig(connection, xBeamSteeringConfig.getUpperSlideConfig());
			long lowerSlideConfigId = opticalSlideConfigAccessor.insertOpticalSlideConfig(connection, xBeamSteeringConfig.getLowerSlideConfig());
			
			stmt = connection.prepareStatement(INSERT_BEAM_STEERING_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, upperSlideConfigId);
			stmt.setLong(2, lowerSlideConfigId);
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_BEAM_STEERING_CONFIG_SQL, true);
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
