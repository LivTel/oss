package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IApertureConfig;
import ngat.phase2.IFocusControl;
import ngat.phase2.XApertureConfig;
import ngat.phase2.XFocusControl;

import org.apache.log4j.Logger;

public class FocusControlAccessor  {
	
	static Logger logger = Logger.getLogger(FocusControlAccessor.class);
	
	/*
	 EA_FOCUSCONTROL
	 id 						int
	 instrumentName	varchar(32)
	 */
	
	//statements
	
	public static final String INSERT_FOCUS_CONTROL_SQL = 						
		"insert into EA_FOCUSCONTROL (" +
		"instrumentName " +
		") values (" + 
		"?)";
	
	public static final String GET_FOCUS_CONTROL_SQL = 							
		"select " +
		"instrumentName " +
		"from " +
		"EA_FOCUSCONTROL " +
		"where id=?";
	
	public static final String DEL_FOCUS_CONTROL_SQL = 								
		"delete from EA_FOCUSCONTROL where id = ?";

	/** Public methods *******************************************************************/
	
	public IFocusControl getFocusControl(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_FOCUS_CONTROL_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_FOCUS_CONTROL_SQL);
			
			String instrumentName;
			XFocusControl focusControl = null;
			
			if (resultSet.next()) {
				instrumentName	= resultSet.getString(1);
				focusControl = new XFocusControl(instrumentName);
			}
			return focusControl;
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
	
	public long insertFocusControl(Connection connection, IFocusControl focusControl) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_FOCUS_CONTROL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, focusControl.getInstrumentName());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_FOCUS_CONTROL_SQL, true);
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
