package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IFocusOffset;
import ngat.phase2.XFocusOffset;

import org.apache.log4j.Logger;

public class FocusOffsetAccessor  {
	
	static Logger logger = Logger.getLogger(FocusOffsetAccessor.class);
	
	/*
	EA_FOCUS_OFFSET;
	id			int
	relative	boolean
	offset	double
	*/
	
	//statements
	public static final String INSERT_FOCUS_OFFSET_SQL = 		
		"insert into EA_FOCUS_OFFSET (" +
		"relative, offset" + 
		") values (" + 
		"?, ?)";
	
	public static final String GET_FOCUS_OFFSET_SQL = 							
		"select " +
		"relative, offset " +
		"from " +
		"EA_FOCUS_OFFSET " +
		"where id=?";
	
	public static final String DEL_FOCUS_OFFSET_SQL = 								
		"delete from EA_FOCUS_OFFSET where id = ?";
	
	public static final String UPDATE_EXPOSURE_SQL =						
		"update EA_FOCUS_OFFSET "+
		"set " +
		"relative=?, " +
		"offset=?" + 
		" where id=?";
																										
	/** Public methods *******************************************************************/
	
	public IFocusOffset getFocusOffset(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_FOCUS_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_FOCUS_OFFSET_SQL);
			
			boolean relative;;
			double offset;
			XFocusOffset focusOffset = null;
			
			if (resultSet.next()) {
				relative	= resultSet.getBoolean(1);
				offset	= resultSet.getDouble(2);
	
				focusOffset = new XFocusOffset(relative, offset);
			}
			return focusOffset;
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
	
	public long insertFocusOffset(Connection connection, IFocusOffset focusOffset) throws Exception {
		PreparedStatement stmt = null;
		try {
			XFocusOffset xfocusOffset = (XFocusOffset)focusOffset;
			
			stmt = connection.prepareStatement(INSERT_FOCUS_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setBoolean(1, xfocusOffset.isRelative());
			stmt.setDouble(2, xfocusOffset.getOffset());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_FOCUS_OFFSET_SQL, true);
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
