package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.ILoginRecord;
import ngat.phase2.XLoginRecord;

import org.apache.log4j.Logger;

public class LoginAccessor  {
	
	/*
	LOGIN
	  `id` 					long
	  `loginTime` 		long
	  `uid` 				long
	  `javaVersion` 	String
	  `osArch` 			String
	  `osName` 		String
	  `osVersion` 		String
	  `screenSize` 	String
	 */
	
	static Logger logger = Logger.getLogger(UserAccessor.class);
	
	//statements
	
	//statements
	public static final String INSERT_LOGIN_SQL = 						
		"insert into LOGIN (" +
		"loginTime, uid, javaVersion, osArch, osName, osVersion, screenSize" + 
		") values (" + 
		"?,?,?,?,?,?,?)";
																			
	/** Public methods ******************************************************************/
	
	public void receiveLogin(Connection connection, ILoginRecord loginRecord) throws Exception {
		logger.info("receiveLogin(" + loginRecord + ")");
		
		String javaVersion, osArch, osName, osVersion, screenSize;
		
		PreparedStatement stmt = null;
		XLoginRecord xLoginRecord = (XLoginRecord) loginRecord;
		
		try {
			stmt = connection.prepareStatement(INSERT_LOGIN_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, new Date().getTime());
			stmt.setLong(2, xLoginRecord.getUserId());
			stmt.setString(3, xLoginRecord.getJavaVersion());
			stmt.setString(4, xLoginRecord.getOsArch());
			stmt.setString(5, xLoginRecord.getOsName());
			stmt.setString(6, xLoginRecord.getOsVersion());
			stmt.setString(7, xLoginRecord.getScreenSize());
			
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_LOGIN_SQL, true);
			return;
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
		
	/** Main *********************************************************************************/
	public static void main(String[] args) {
		//stuff here
	}
}
