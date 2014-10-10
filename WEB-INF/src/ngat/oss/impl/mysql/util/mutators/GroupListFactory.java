package ngat.oss.impl.mysql.util.mutators;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ngat.oss.impl.mysql.DatabaseTransactor;

public class GroupListFactory {
	
	//return a list of Long objects representing the groups required by the groupSetDescription
	public List getGroupIDList(String groupSetDescription) throws Exception {
		
		ArrayList groupIDs = new ArrayList();
		
		if (!groupSetDescription.equals(SkyBrightnessMutator.ALL_GROUPS_DESCRIPTION)) {
			long groupId = Long.valueOf(groupSetDescription);
			groupIDs.add(new Long(groupId));
			return groupIDs;
		}
		
		//all the group IDs are required
		
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			String statementString = "select id from OBSERVATION_GROUP";
	
			stmt = SkyBrightnessMutator.connection.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, statementString);
	
			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getInt(1);
				groupIDs.add(new Long(id));
			}
			return groupIDs;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
				System.err.println("failed to close ResultSet");
				e.printStackTrace();
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				System.err.println("failed to close PreparedStatement");
			}
		}
	}
}