package ngat.oss.impl.mysql.accessors;

import org.apache.log4j.Logger;

public class LinkageAccessor {
	
	static Logger logger = Logger.getLogger(LinkageAccessor.class);
	/*
	 	LINKAGE;
			id					int
			preceding  	int
			following    	int
			minInterval	int
			maxInterval  	int
			confidence   	double
			relationship 	tinyint 
	 */
	
	//statements
	public static final String INSERT_LINKAGE_SQL = 						
		"insert into LINKAGE (" +
		"preceding, following, minInterval, maxInterval, confidence, relationship" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?)";
	
	public static final String GET_LINKAGE_SQL = 							
		"select " +
		"id, preceding, following, minInterval, maxInterval, confidence, relationship " +
		"from " +
		"LINKAGE " +
		"where id=?";
	
	/*
	 * no implementations as yet
	 */
}
