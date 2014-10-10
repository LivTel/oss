package ngat.oss.impl.mysql.util.mutators;

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

public class SkyBrightnessMutator {

	static Logger logger = Logger.getLogger(SkyBrightnessMutator.class);
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
	
	
	public SkyBrightnessMutator() throws Exception {
	}
	
	private void mutateObservingConstraintsOfGroups(List groupIDs) {
		
		Iterator groupIdIterator = groupIDs.iterator();
		
		while (groupIdIterator.hasNext()) {
			long goupId = ((Long) groupIdIterator.next()).longValue();
			mutateObservingConstraintsOfGroup(goupId);
		}
	}
	
	private void mutateObservingConstraintsOfGroup(long groupId) {
		
		try {
			GroupSkyBrightnessConstraintsDescription brightnessConstraintsDescription = getGroupSkyBrightnessConstraintsDescription(groupId);
			addSkyBrightnessConstraint(groupId, brightnessConstraintsDescription);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * from the constraints summarized in brightnessConstraintsDescription add a single Sky Brightness Constraint
	 */
	private void addSkyBrightnessConstraint(long groupID, GroupSkyBrightnessConstraintsDescription brightnessConstraintsDescription) throws Exception  {
		
		//System.err.println("addSkyBrightnessConstraint(" + groupID + "," +brightnessConstraintsDescription + ")");
		
		XSkyBrightnessConstraint skyBrightnessConstraint = new XSkyBrightnessConstraint();
		double minLunarDistance = brightnessConstraintsDescription.getMinimumLunarDistance();
		
		if (brightnessConstraintsDescription.getTwilightValue() == TWILIGHT_CIVIL) {
			System.err.println("... CIVIL TWILIGHT, setting sky brightness category = magnitude 10");
			skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_10);
			  
		} else if (brightnessConstraintsDescription.getTwilightValue() == TWILIGHT_NAUTICAL) {
			System.err.println("... NAUTICAL TWILIGHT, setting sky brightness category = magnitude 6");
			skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_6);
			
		} else if (brightnessConstraintsDescription.getTwilightValue() == TWILIGHT_ASTRONOMICAL) {
			System.err.print("... ASTRONOMICAL TWILIGHT");
			if ( minLunarDistance > UnitConverter.convertDegsToRads(30)) {
				System.err.println(", minLunarDistance > 30 degrees, setting sky brightness category = magnitude 4");
				skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_4);
				
			} else if (brightnessConstraintsDescription.getMoonBrightness() == MOON_BRIGHT) {
				System.err.println(", MOON BRIGHT, setting sky brightness category = magnitude 4");
				skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_4);
				
			} else {
				System.err.println(", setting sky brightness category = magnitude 2");
				skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_2);
					
			 }
		
		} else if (brightnessConstraintsDescription.getTwilightValue() == NIGHT_TIME) { 
			System.err.print("... NIGHT TIME");
			
			if (brightnessConstraintsDescription.getMoonBrightness() == MOON_BRIGHT) { 
				System.err.print(", MOON BRIGHT");
				if (minLunarDistance < UnitConverter.convertDegsToRads(30)) {
					skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_1P5);
					System.err.println(", minLunarDistance < 30 degrees, setting sky brightness category = magnitude 1.5");
				} else { 
					skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_2);
					System.err.println(", minLunarDistance !< 30 degrees, setting sky brightness category = magnitude 2");
				}
				
			} else { 
				System.err.print(", MOON NOT BRIGHT");
				if (minLunarDistance <= UnitConverter.convertDegsToRads(30)) {
					skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.DARK); 
					System.err.println(", minLunarDistance < 30 degrees, setting sky brightness category = DARK");
					
				} else { 
					skyBrightnessConstraint.setSkyBrightnessCategory(IObservingConstraint.MAG_0P75);
					System.err.println(", minLunarDistance !< 30 degrees, setting sky brightness category = 0.75");
				}
			} 
		}
		
		insertSkyBrightnessConstraint(groupID,  skyBrightnessConstraint);
	}
	
	private void insertSkyBrightnessConstraint(long gid, XSkyBrightnessConstraint skyBrightnessConstraint) throws Exception {
		System.err.println("... insertSkyBrightnessConstraint(" + gid + "," +skyBrightnessConstraint + ")" );
		
		PreparedStatement stmt = null;
		try {
			long id;
			int type;
			int category;
			double min = 0, max = 0;

			type = ObservingConstraintTypes.SKY_BRIGHTNESS_CONSTRAINT;
			category = skyBrightnessConstraint.getSkyBrightnessCategory();
			
			stmt = SkyBrightnessMutator.connection.prepareStatement(GroupAccessor.INSERT_OBSERVING_CONSTR_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);
			stmt.setLong(2, type);
			stmt.setInt(3, category);
			stmt.setDouble(4, min);
			stmt.setDouble(5, max);
			id = DatabaseTransactor.getInstance().executeUpdateStatement(SkyBrightnessMutator.connection, stmt, GroupAccessor.INSERT_OBSERVING_CONSTR_SQL, true);
			System.err.println("... ... inserted SKyBrightnessConstraint with ID " + id);
			
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
	
	private GroupSkyBrightnessConstraintsDescription getGroupSkyBrightnessConstraintsDescription(long groupID) throws Exception {
		
		System.err.println("getGroupSkyBrightnessConstraintsDescription(" + groupID + ")");
		
		String getSkyBrightnessConstraintsSQL = 
		   "select id, type, category, min, max from OBSERVING_CONSTRAINT where gid = ?";
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = SkyBrightnessMutator.connection.prepareStatement(getSkyBrightnessConstraintsSQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, groupID);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, getSkyBrightnessConstraintsSQL);
			
			ArrayList constraintsList = new ArrayList();
			
			while (resultSet.next()) {
				int id 			= resultSet.getInt(1);
				int type 		= resultSet.getInt(2);
				int category 	= resultSet.getInt(3);
				double min 	= resultSet.getDouble(4);
				double max	= resultSet.getDouble(5);
				ObservingConstraintSummary summary = new ObservingConstraintSummary(groupID, id, type, category, min, max);
				System.err.println("... adding " + summary);
				constraintsList.add(summary);
			}
			return getGroupSkyBrightnessConstraintsDescription(constraintsList);
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
	
	/*
	* given a set of sky brightness constraints of a group (represented as a list of ObservingConstraintSummary objects)
	* return GroupSkyBrightnessConstraintSetDescription summarising the constraints
	*/
	private GroupSkyBrightnessConstraintsDescription getGroupSkyBrightnessConstraintsDescription(ArrayList constraintsList) {
		
		//System.err.println("getGroupSkyBrightnessConstraintsDescription(list of ObservingConstraintSummary objects, size=" + constraintsList.size() + ")" );
		
		Iterator constraintsListIterator = constraintsList.iterator();
		boolean twilightValueSpecified = false;
		boolean setMinLunarDIstance = false;
		boolean moonBrightnesSpecified = false;
		
		GroupSkyBrightnessConstraintsDescription skyBrightnessConstraintSetDescription = new GroupSkyBrightnessConstraintsDescription();
		
		while (constraintsListIterator.hasNext()) {
			ObservingConstraintSummary constraintSummary = (ObservingConstraintSummary) constraintsListIterator.next();
			
			if (constraintSummary.getType() == SOLAR_ELEVATION_CONSTRAINT_TYPE) {
				//System.err.println("constraintSummary.getType() == SOLAR_ELEVATION_CONSTRAINT_TYPE");
				twilightValueSpecified = true;
				skyBrightnessConstraintSetDescription.setTwilightValue(constraintSummary.getCategory()); //set the twilight value as the category
				//System.err.println("... setTwilightValue(" + constraintSummary.getCategory() + ")");
				
			} else if (constraintSummary.getType() == LUNAR_DISTANCE_CONSTRAINT_TYPE) {
				//System.err.println("constraintSummary.getType() == LUNAR_DISTANCE_CONSTRAINT_TYPE");
				setMinLunarDIstance = true;
				skyBrightnessConstraintSetDescription.setMinimumLunarDistance(constraintSummary.getMin()); //set the min lunar distance as the min value
				//System.err.println("... setMinimumLunarDistance(" + constraintSummary.getMin() + ")");
				
			} else if (constraintSummary.getType() == LUNAR_ELEVATION_CONSTRAINT_TYPE) {
				//System.err.println("constraintSummary.getType() == LUNAR_ELEVATION_CONSTRAINT_TYPE");
				moonBrightnesSpecified = true;
				skyBrightnessConstraintSetDescription.setMoonBrightness(constraintSummary.getCategory()); //set moon brightness as the category
				//System.err.println("... setMoonBrightness(" + constraintSummary.getCategory() + ")");
				
			} else if (constraintSummary.getType() == LUNAR_PHASE_CONSTRAINT_TYPE) {
				//ignored, I think
			} else if (constraintSummary.getType() == 1) {
				//seeing constraint, ignore
			} else if (constraintSummary.getType() == 2) {
				//extinction constraint, ignore
			} else if (constraintSummary.getType() == 3) {
				//airmass constraint, ignore
			} else if (constraintSummary.getType() == 4) {
				//hour-angle constraint, ignore
			} else {
				System.err.println("... UNKNOWN constraintSummary type, constraintSummary.getType()=" + constraintSummary.getType());
			}
		}
		
		if (!twilightValueSpecified) {
			//haven't specified a twilight value - default to CIVIL
			skyBrightnessConstraintSetDescription.setTwilightValue(TWILIGHT_CIVIL); 
		}
		if (!setMinLunarDIstance) {
			//haven't specified a lunar distance - default to 0 degree min lunar distance
			skyBrightnessConstraintSetDescription.setMinimumLunarDistance(0);
		}
		if (!moonBrightnesSpecified) {
			//haven't specified a moon brightness - default to bright moon
			skyBrightnessConstraintSetDescription.setMoonBrightness(MOON_BRIGHT);
		}
		
		System.err.println("Built " + skyBrightnessConstraintSetDescription);
		return skyBrightnessConstraintSetDescription;
	}


	public static void main(String[] args) {
		
		// get JDBC Connection from the supplied arguments and instantiate SkyBrightnessMutator
		
		//e.g. args:
		// --host ltdev1 --db phase2odb --user oss --pw ng@toss --group 1356
		// --host ltdev1 --db phase2odb --user oss --pw ng@toss --group ALL_GROUPS
		
		CommandTokenizer parser = new CommandTokenizer("--");
		parser.parse(args);
		ConfigurationProperties rconfig = parser.getMap();
		
		String host = rconfig.getProperty("host");
		String db = rconfig.getProperty("db");
		String dbu = rconfig.getProperty("user");
		String pass = rconfig.getProperty("pw");
		String groupSetDescription = rconfig.getProperty("group");
		
		String url = "jdbc:mysql://"+host+"/"+db+"?user="+dbu+"&password="+pass;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			SkyBrightnessMutator.connection = DriverManager.getConnection(url);
			System.err.println("SQL Driver got connection: " + SkyBrightnessMutator.connection);
			System.err.println("getting group id list");
			List groupIDs = new GroupListFactory().getGroupIDList(groupSetDescription);
			
			System.err.print("group list= ");
			Iterator gli = groupIDs.iterator();
			while (gli.hasNext()) {
				Long gid = (Long) gli.next();
				System.err.print(gid.longValue() + ",");
			}
			System.err.println();
			
			SkyBrightnessMutator skyBrightnessMutator = new SkyBrightnessMutator();
			System.err.println("Instantiated SkyBrightnessMutator");
			skyBrightnessMutator.mutateObservingConstraintsOfGroups(groupIDs);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
			"Error creating SQL Connection to IDMAP Table:");
		}
	}
	
}







