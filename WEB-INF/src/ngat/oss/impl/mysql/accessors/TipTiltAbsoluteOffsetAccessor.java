package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.ITipTiltAbsoluteOffset;
import ngat.phase2.XTipTiltAbsoluteOffset;

import org.apache.log4j.Logger;

public class TipTiltAbsoluteOffsetAccessor  {
	
	static Logger logger = Logger.getLogger(TipTiltAbsoluteOffsetAccessor.class);
	
	/*
	 EA_TIPTILTABSOLUTEOFFSET
	 id 						int
	 offset1					DOUBLE
	 offset2					DOUBLE
	 instrumentName	varchar(32)
	 offsetType			int
	 tipTiltId					int
	 */
	
	//statements
	
	public static final String INSERT_TIP_TILT_OFFSET_SQL = 						
		"insert into EA_TIPTILTABSOLUTEOFFSET (" +
		"offset1, offset2, instrumentName, offsetType, tipTiltId" +
		") values (" + 
		"?,?,?,?,?)";
	
	public static final String GET_TIP_TILT_OFFSET_SQL = 							
		"select " +
		"offset1, " + 
		"offset2, " + 
		"instrumentName, " + 
		"offsetType, " + 
		"tipTiltId "+
		"from " +
		"EA_TIPTILTABSOLUTEOFFSET " +
		"where id=?";
	
	public static final String DEL_TIP_TILT_OFFSET_SQL = 								
		"delete from EA_TIPTILTABSOLUTEOFFSET where id = ?";

	/** Public methods *******************************************************************/
	
	public ITipTiltAbsoluteOffset getTipTiltAbsoluteOffset(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_TIP_TILT_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TIP_TILT_OFFSET_SQL);
			
			XTipTiltAbsoluteOffset tipTiltAbsoluteOffset = null;
			double offset1, offset2;
			String instrumentName; 
			int offsetType, tipTiltId;
			
			if (resultSet.next()) {
				offset1	= resultSet.getDouble(1);
				offset2	= resultSet.getDouble(2);
				instrumentName = resultSet.getString(3);
				offsetType = resultSet.getInt(4);
				tipTiltId = resultSet.getInt(5);
				tipTiltAbsoluteOffset = new XTipTiltAbsoluteOffset(offset1, offset2, instrumentName, offsetType, tipTiltId);
			}
			return tipTiltAbsoluteOffset;
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
	
	public long insertTipTiltAbsoluteOffset(Connection connection, ITipTiltAbsoluteOffset tipTiltAbsoluteOffset) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_TIP_TILT_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, tipTiltAbsoluteOffset.getOffset1());
			stmt.setDouble(2, tipTiltAbsoluteOffset.getOffset2());
			stmt.setString(3, tipTiltAbsoluteOffset.getInstrumentName());
			stmt.setInt(4, tipTiltAbsoluteOffset.getOffsetType());
			stmt.setInt(5, tipTiltAbsoluteOffset.getTipTiltId());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_TIP_TILT_OFFSET_SQL, true);
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
