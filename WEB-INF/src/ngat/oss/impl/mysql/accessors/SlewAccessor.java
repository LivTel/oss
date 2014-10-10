package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IRotatorConfig;
import ngat.phase2.ISlew;
import ngat.phase2.ITarget;
import ngat.phase2.XSlew;

import org.apache.log4j.Logger;

public class SlewAccessor  {
	
	static Logger logger = Logger.getLogger(SlewAccessor.class);
	
	/* 
	  EA_SLEW;
	  	id										int
	  	targetRef  							int
	  	rotatorRef  						int
	  	usesNonSiderealTracking 	boolean        
	*/
	
	//statements
	public static final String INSERT_SLEW_SQL = 		
		"insert into EA_SLEW (" +
		"targetRef, rotatorRef, usesNonSiderealTracking" +
		") values (" +
		"?, ?, ?)";
	
	public static final String GET_SLEW_SQL = 							
		"select " +
		"targetRef, rotatorRef,  usesNonSiderealTracking " +
		"from " +
		"EA_SLEW " +
		"where id=?";
	
	public static final String DEL_SLEW_SQL = 								
		"delete from EA_SLEW where id = ?";
															
	
	/** Public methods *******************************************************************/
	
	public ISlew getSlew(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_SLEW_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_SLEW_SQL);
			
			int targetRef, rotatorRef;
			boolean usesNonSiderealTracking;
			
			XSlew slew = null;
			
			if (resultSet.next()) {
				targetRef	= resultSet.getInt(1);
				rotatorRef	= resultSet.getInt(2);
				usesNonSiderealTracking = resultSet.getBoolean(3);
				
				TargetAccessor targetAccessor = new TargetAccessor();
				ITarget target = targetAccessor.getTarget(connection, targetRef);
				
				RotatorConfigAccessor rotatorConfigAccessor = new RotatorConfigAccessor();
				IRotatorConfig rotatorConfig = (IRotatorConfig) rotatorConfigAccessor.getRotatorConfig(connection, rotatorRef);
				
				slew = new XSlew();
				slew.setTarget(target);
				slew.setRotatorConfig(rotatorConfig);
				slew.setUsesNonSiderealTracking(usesNonSiderealTracking);
			}
			return slew;
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
	
	public long insertSlew(Connection connection, ISlew slew) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			XSlew xslew = (XSlew)slew;
			//insert rotator config
			RotatorConfigAccessor rotatorConfigAccessor = new RotatorConfigAccessor();
			long rotId = rotatorConfigAccessor.insertRotatorConfig(connection, xslew.getRotatorConfig());
			long targetId = xslew.getTarget().getID();
			
			stmt = connection.prepareStatement(INSERT_SLEW_SQL, Statement.RETURN_GENERATED_KEYS);
	
			//targetRef, rotatorRef, usesNonSiderealTracking
			stmt.setLong(1, targetId);
			stmt.setLong(2, rotId);
			stmt.setBoolean(3, xslew.usesNonSiderealTracking());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_SLEW_SQL, true);
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
