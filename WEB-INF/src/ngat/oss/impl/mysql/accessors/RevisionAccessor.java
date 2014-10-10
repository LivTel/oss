package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IRevision;
import ngat.phase2.XRevision;

import org.apache.log4j.Logger;

public class RevisionAccessor {
	
	static Logger logger = Logger.getLogger(RevisionAccessor.class);
	
	/*
	REVISION
		id				int
		pid			int
		date			Date
		comment 	String
		who 			int
	 */
	
	//statements
	public static final String INSERT_REV_SQL = 							
		"insert into REVISION (" +
		"pid, date, comment, who" +
		") values (" + 
		"?, ?, ?, ?, ?)";
	
	public static final String GET_REV_SQL = 							
		"select " +
		"id, pid, date, comment, who " +
		"from " +
		"REVISION " +
		"where id=?";
	
	public static final String LIST_REV_SQL = 							
		"select " +
		"id, pid, date, comment, who " + 
		"from " + 
		"REVISION " + 
		"where pid=?";
	
	public static final String DEL_REV_SQL = 							
		"delete from REVISION where id = ?";
	
	public static final String UPDATE_REV_SQL =					
		"update REVISION "+
		"set " + 
		"id=?," + 
		"pid=?,"+
		"date=?," +
		"comment=? "+
		"who=? "+
		"where id=?";
	
	/** Public methods ******************************************************************
	 * @throws Exception */
	
	public long addRevision(Connection connection, long pid, IRevision revision) throws Exception {	

		PreparedStatement stmt = null;
		try {
			//table fields
			long date;
			String comment;
			String who;
			
			//load values
			date = revision.getTime();
			comment = revision.getComment();
			who = revision.getEditor();
			
			//prepare statement
			stmt = connection.prepareStatement(INSERT_REV_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, pid);
			stmt.setLong(2, date); 
			stmt.setString(3, comment);
			stmt.setString(4, who);
	
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_REV_SQL, true);
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
	
	public List listRevisions(Connection connection, long pid) throws Exception {
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(LIST_REV_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_REV_SQL);
			
			List revisionList = new ArrayList();
			while (resultSet.next()) {
				IRevision conf = getRevisionFromResultSetCursor(resultSet);
				revisionList.add(conf);
			}
			return revisionList;
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
	
	/** Private  methods ******************************************************************
	 * @throws Phase2Exception */
	
	private IRevision getRevisionFromResultSetCursor(ResultSet resultSet) throws Exception {
		
		long id, pid, date;
		String comment;
		String who;
		
		id				= resultSet.getInt(1);
		pid 			= resultSet.getInt(2);
		date 			= resultSet.getLong(3);
		comment	= resultSet.getString(4);
		who			= resultSet.getString(5);
		
		XRevision revision = new XRevision();
		revision.setID(id);
		revision.setTime(date);
		revision.setComment(comment);
		revision.setEditor(who);
		return revision;
	}
}
