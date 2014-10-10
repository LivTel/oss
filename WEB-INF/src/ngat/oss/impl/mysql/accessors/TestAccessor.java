package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class TestAccessor  {
	
	static Logger logger = Logger.getLogger(TestAccessor.class);
	
	/**
	 * Essentially just does a database operation to see if it can get through to the underlying database implementation
	 * @param connection
	 * @throws Exception
	 */
	public void ping(Connection connection) throws Exception {
		
		logger.info("ping()");
		PreparedStatement stmt = null;
		ResultSet metaResultSet = null;
		
		try {
			DatabaseMetaData meta = connection.getMetaData();
			metaResultSet = meta.getTables(null, null, null, new String[]{"TABLE"});
			ArrayList tableNames = new ArrayList();
			while (metaResultSet.next()) {
				String tableName = metaResultSet.getString(3);
				tableNames.add(tableName);
			}
			logger.info("... ping() complete");
		} finally {
			try {
				if (metaResultSet != null) {
					metaResultSet.close();
				}
			} catch (Exception e) {
				logger.error("failed to close ResultSet");
			}
		}
	}
	
}


