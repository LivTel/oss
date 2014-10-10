package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IVersion;
import ngat.phase2.XVersion;

import org.apache.log4j.Logger;

public class MinimumClientVersionAccessor  {
	
	/*
	VERSION
	  `major` 		long
	  `minor` 		long
	 */
	
	static Logger logger = Logger.getLogger(MinimumClientVersionAccessor.class);
	
	//statements
	public static final String GET_VERSION_SQL = 							
		"select " +
		"major, minor, revision, minorRevision  " +
		"from " +
		"MINIMUM_CLIENT_VERSION ";

	/** Public methods ******************************************************************/
	/*
	 * returns the value in the database representing the lowest usable version of the P2UI for this server version
	 */
	public IVersion getMinimumClientVersionNumber(Connection connection) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_VERSION_SQL, Statement.RETURN_GENERATED_KEYS);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_VERSION_SQL);
			IVersion version = null;
			if (resultSet.next()) {
				int major = resultSet.getInt(1);
				int minor = resultSet.getInt(2);
				int revision = resultSet.getInt(3);
				int minorRevision = resultSet.getInt(4);
				version = new XVersion(major, minor, revision, minorRevision); 
			}
			return version;
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
