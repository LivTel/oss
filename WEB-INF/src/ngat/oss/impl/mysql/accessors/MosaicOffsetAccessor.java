package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IMosaicOffset;
import ngat.phase2.XPositionOffset;

public class MosaicOffsetAccessor {

	static Logger logger = Logger.getLogger(MosaicOffsetAccessor.class);
	
	/*
	EA_MOSAIC_OFFSET
		id				int
		relative		boolean
		raOffset		double
		decOffset	double 
	 */
	
	public static final String GET_MOSAIC_OFFSET_SQL = 							
		"select " +
		"relative, raOffset, decOffset " +
		"from " +
		"EA_MOSAIC_OFFSET " +
		"where id=? ";
	
	public static final String INSERT_MOSAIC_OFFSET_SQL = 						
		"insert into EA_MOSAIC_OFFSET (" +
		"relative, raOffset, decOffset " +
		") values (" + 
		"?, ?, ?)";
	
	public static final String DEL_MOSAICS_OF_OBS_SQL = 							
		"delete from MOSAIC_OFFSET where id = ?";
	
	/**
	 * Not used in project as yet
	 * @param connection
	 * @param offset
	 * @param obsId
	 * @param seq
	 * @return
	 * @throws Exception
	 */
	public long addMosaicOffset(Connection connection, IMosaicOffset offset, long obsId, int seq) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(INSERT_MOSAIC_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, obsId);
			stmt.setDouble(2, offset.getRAOffset());
			stmt.setDouble(3, offset.getDecOffset());
			stmt.setInt(4, seq);
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_MOSAIC_OFFSET_SQL, true);
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
	
	public IMosaicOffset getMosaicOffset(Connection connection, long id) throws Exception  {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_MOSAIC_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_MOSAIC_OFFSET_SQL);
			
			boolean relative;
			double raOffset, decOffset;
			IMosaicOffset mosaicOffset = null;
			
			if (resultSet.next()) {
				relative		= resultSet.getBoolean(1);
				raOffset		= resultSet.getDouble(2);
				decOffset	= resultSet.getDouble(3);
				
				mosaicOffset = getMosaicOffset(relative, raOffset, decOffset);
			}
			return mosaicOffset;
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
	
	private IMosaicOffset getMosaicOffset(boolean relative, double raOffset, double decOffset) throws Exception {
		return new XPositionOffset(relative, raOffset, decOffset);
	}
	
	public long insertMosaicOffset(Connection connection, IMosaicOffset mosaicOffset) throws Exception {
		PreparedStatement stmt = null;
		try {
			XPositionOffset xpositionOffset = (XPositionOffset)mosaicOffset;
			
			stmt = connection.prepareStatement(INSERT_MOSAIC_OFFSET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setBoolean(1, xpositionOffset.isRelative());
			stmt.setDouble(2, xpositionOffset.getRAOffset());
			stmt.setDouble(3, xpositionOffset.getDecOffset());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_MOSAIC_OFFSET_SQL, true);
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
