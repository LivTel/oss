package ngat.oss.impl.mysql.util.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.accessors.GroupAccessor;
import ngat.oss.impl.mysql.reference.ObservingConstraintTypes;
import ngat.phase2.IObservingConstraint;
import ngat.phase2.XSkyBrightnessConstraint;
import ngat.phase2.util.UnitConverter;
import ngat.util.CommandTokenizer;
import ngat.util.ConfigurationProperties;

import org.apache.log4j.Logger;

public class ObsConstraintAdder {

	static Logger logger = Logger.getLogger(ObsConstraintAdder.class);
	public static Connection connection;
	public static final String ALL_GROUPS_DESCRIPTION = "ALL_GROUPS";
	
	private static final int SOLAR_ELEVATION_CONSTRAINT_TYPE = 5;
	private static final int LUNAR_DISTANCE_CONSTRAINT_TYPE = 7;
	private static final int LUNAR_ELEVATION_CONSTRAINT_TYPE = 8;
	private static final int LUNAR_PHASE_CONSTRAINT_TYPE = 9;
	
	private static final int TWILIGHT_CIVIL = 0;
	private static final int TWILIGHT_NAUTICAL = 1;
	private static final int TWILIGHT_ASTRONOMICAL = 2;
	private static final int NIGHT_TIME = 3;
	
	private static final int MOON_DARK = 0;
	private static final int MOON_BRIGHT = 1;
	
	
	public ObsConstraintAdder() throws Exception {
	}
	
	
	private void addObservingConstraintToGroup(String groupIDStr, String typeStr, String categoryStr, String minDegStr, String maxStr) throws Exception {
		
		System.err.println("addObservingConstraintToGroup(" +groupIDStr +"," + typeStr +"," + categoryStr +"," + minDegStr +"," + maxStr +")");
		
		long groupId;
		try {
			groupId = Integer.parseInt(groupIDStr);
		} catch (NumberFormatException e) {
			System.err.println("VALUE FOR GROUP IS NOT AN INTEGER: " +groupIDStr );
			return;
		}
		
		int type;
		if (typeStr == null) {
			type = -1;
		} else {
			try {
				type = Integer.parseInt(typeStr);
			} catch (NumberFormatException e) {
				System.err.println("VALUE FOR TYPE IS NOT AN INTEGER: " +typeStr );
				return;
			}
		}
		switch(type) {
			case  SOLAR_ELEVATION_CONSTRAINT_TYPE:
				break;
			case  LUNAR_DISTANCE_CONSTRAINT_TYPE:
				break;
			case  LUNAR_ELEVATION_CONSTRAINT_TYPE:
				break;
			case  LUNAR_PHASE_CONSTRAINT_TYPE:
				System.err.println("THIS PROGRAM WILL NOT INSERT LUNAR_PHASE CONSTRAINTS");
				return;
			default:
				System.err.println("UNKNOWN CONSTRAINT TYPE: " +type );
				return;
		}
		
		int category;
		if (categoryStr == null) {
			category = -1;
		} else {
			try {
				category = Integer.parseInt(categoryStr);
			} catch (NumberFormatException e) {
				System.err.println("VALUE FOR CATEGORY IS NOT AN INTEGER: " +categoryStr );
				return;
			}
		}
		
		/*
		double constraintMinRads;
		try {
			constraintMinRads = UnitConverter.convertDegsToRads(Double.parseDouble(constraintMinDeg));
		} catch (NumberFormatException e) {
			System.err.println("VALUE FOR MIN IS NOT AN DOUBLE: " +constraintMinDeg );
			return;
		}
		 */
		double minRads;
		if (minDegStr == null) {
			minRads = 0;
		} else {
			try {
				minRads = UnitConverter.convertDegsToRads(Double.parseDouble(minDegStr));
			} catch (NumberFormatException e) {
				System.err.println("VALUE FOR MIN IS NOT AN DOUBLE: " +minDegStr );
				return;
			}
		}
		
		double max;
		if (maxStr == null) {
			max = 0;
		} else {
			try {
				max = Double.parseDouble(maxStr);
			} catch (NumberFormatException e) {
				System.err.println("VALUE FOR MAX IS NOT AN DOUBLE: " +maxStr );
				return;
			}
		}
		
		insertObservingConstraint(groupId, type, category, minRads, max);
	}
	
	private void insertObservingConstraint(long gid, int type, int category, double minRads, double max) throws Exception {
		System.err.println("... insertObservingConstraint(" + gid + "," + + type + ","  + category + ","  + minRads + ","  + max  + ")" );
		
		PreparedStatement stmt = null;
		try {
			long id;
			
			stmt = ObsConstraintAdder.connection.prepareStatement(GroupAccessor.INSERT_OBSERVING_CONSTR_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);
			stmt.setInt(2, type);
			stmt.setInt(3, category);
			stmt.setDouble(4, minRads);
			stmt.setDouble(5, max);
			id = DatabaseTransactor.getInstance().executeUpdateStatement(ObsConstraintAdder.connection, stmt, GroupAccessor.INSERT_OBSERVING_CONSTR_SQL, true);
			System.err.println("... ... inserted observing constraint with ID " + id);
			
		} finally {
			try {
				if (stmt != null) {
					stmt.close(); //only closes the latest one I guess, ho humm - am sure gc will get others
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	

	public static void main(String[] args) {
		
		// get JDBC Connection from the supplied arguments and instantiate SkyBrightnessMutator
		
		//e.g. args:
		// --host ltdev1 --db phase2odb --user oss --pw ng@toss --group 36859 --type 7 --min 30
		// --host ltdev1 --db phase2odb --user oss --pw ng@toss --group ALL_GROUPS
		
		CommandTokenizer parser = new CommandTokenizer("--");
		parser.parse(args);
		ConfigurationProperties rconfig = parser.getMap();
		
		//database strings
		String host = rconfig.getProperty("host");
		String db = rconfig.getProperty("db");
		String dbu = rconfig.getProperty("user");
		String pass = rconfig.getProperty("pw");
		
		String url = "jdbc:mysql://"+host+"/"+db+"?user="+dbu+"&password="+pass;
		
		System.out.println("using database url: " + url);
		
		//obs constraint strings
		String groupID = rconfig.getProperty("group");
		String constraintType =  rconfig.getProperty("type");
		String constraintCategory =  rconfig.getProperty("category");
		String constraintMinDeg =  rconfig.getProperty("min"); //min is entered by the user in degrees
		String constraintMax =  rconfig.getProperty("max");
		
		System.out.println("using arguments: ");
		System.out.println("... groupID: " + groupID);
		
		System.out.println("... constraint type: " + constraintType);
		
		if (constraintCategory != null) {
			System.out.println("... category: " + constraintCategory);
		}
		if (constraintMinDeg != null) {
			System.out.println("... min(deg): " + constraintMinDeg);
		}
		if (constraintMax != null) {
			System.out.println("... max: " + constraintMax);
		}
				
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			ObsConstraintAdder.connection = DriverManager.getConnection(url);
			System.err.println("SQL Driver got connection: " + ObsConstraintAdder.connection);
			
			ObsConstraintAdder obsConstraintAdder = new ObsConstraintAdder();
			System.err.println("Instantiated ObsConstraintAdder");
			obsConstraintAdder.addObservingConstraintToGroup(groupID, constraintType, constraintCategory, constraintMinDeg, constraintMax);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("FINISHED");
	}
	
}







