package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.ExecutiveActionElementTypes;
import ngat.oss.impl.mysql.reference.ObservingConstraintTypes;
import ngat.phase2.IGroup;
import ngat.phase2.IInstrumentConfig;
import ngat.phase2.IObservingConstraint;
import ngat.phase2.ITarget;
import ngat.phase2.ITimingConstraint;
import ngat.phase2.XGroup;
import ngat.phase2.XAirmassConstraint;
import ngat.phase2.XHourAngleConstraint;
import ngat.phase2.XPhotometricityConstraint;
import ngat.phase2.XSeeingConstraint;
import ngat.phase2.XSkyBrightnessConstraint;

//no longer used
//import ngat.phase2.XLunarDistanceConstraint;
//import ngat.phase2.XLunarElevationConstraint;
//import ngat.phase2.XLunarPhaseConstraint;
//import ngat.phase2.XSolarElevationConstraint;

import org.apache.log4j.Logger;

//timing constraint access has been split out into it's own accessor (which is regularly invoked in this class)
//this is not the case for observing constraints as yet.

public class GroupAccessor {

	static Logger logger = Logger.getLogger(GroupAccessor.class);

	/*
	 * OBSERVATION_GROUP id int pid int tcid int osid int active boolean name
	 * String priority int urgent boolean
	 */
	/*
	 * OBSERVING_CONSTRAINT id int type tinyint category int gid int min double
	 * max double
	 */

	// statements

	public static final String INSERT_GRP_SQL = "insert into OBSERVATION_GROUP ("
			+"pid, tcid, active, name, priority, urgent"
			+") values ("
			+"?, ?, ?, ?, ?, ?)";

	public static final String GET_GRP_COUNT_SQL = "select count(*) from OBSERVATION_GROUP where pid = ? and  active = ?";

	public static final String GET_GRP_SQL = "select "
			+"id, pid, tcid, osid, active, name, priority, urgent " +"from "
			+"OBSERVATION_GROUP " +"where id=?";

	public static final String GET_GROUP_ID_SQL = "select " +"id " +"from "
			+"OBSERVATION_GROUP " +"where pid=? " +"and name=?";

	public static final String LIST_GRP_SQL = "select "
			+"id, pid, tcid, osid, active, name, priority, urgent " +"from "
			+"OBSERVATION_GROUP " +"where pid=? " +"order by name";

	public static final String LIST_ALL_GRP_IDs_SQL_PREFIX = "select " +"id "
			+"from " +"OBSERVATION_GROUP " +"where pid=? ";

	
	// ------------------ GROUP LISTINGS SQL ----------------------------
	public static final String LIST_UNEXPIRED_GROUPS_SQL = 
			LIST_ALL_GRP_IDs_SQL_PREFIX
			+"and tcid in (select id from TIMING_CONSTRAINT where (type = 5 and (start +window/2) > UNIX_TIMESTAMP(now()) * 1000) or (end >UNIX_TIMESTAMP(now()) * 1000)) "
			+"order by name";

	// This is used to load the active unexpired groups required by the schdeuler. 
	public static final String LIST_ACTIVE_UNEXPIRED_GROUPS_SQL = 
			LIST_ALL_GRP_IDs_SQL_PREFIX
			+"and active=true "
			+"and tcid in (select id from TIMING_CONSTRAINT where (type = 5 and (start +window/2) > UNIX_TIMESTAMP(now()) * 1000) or (end >UNIX_TIMESTAMP(now()) * 1000)) "
			+"order by name";
	
	// / ------------------ GROUP LISTINGS SQL ---------------------------
	
	public static final String LIST_ALL_GRP_IDs_SQL = LIST_ALL_GRP_IDs_SQL_PREFIX
			+"order by name";

	public static final String LIST_ALL_ACTIVE_GRP_IDs_SQL = LIST_ALL_GRP_IDs_SQL_PREFIX
			+" and active=true " +"order by name";

	public static final String DEL_GRP_SQL = "delete from OBSERVATION_GROUP where id = ?";

	public static final String GET_TIMING_CONSTR_ID = "select tcid "
			+"from OBSERVATION_GROUP where id=?";

	public static final String UPDATE_GROUP_SQL = "update OBSERVATION_GROUP "
			+"set tcid=?, active=?,  name=?, priority=?, urgent=? where id=?";

	public static final String UPDATE_GROUP_URGENCY_SQL = "update OBSERVATION_GROUP "
			+"set urgent=? where id=?";

	public static final String FIND_ID_OF_PROPOSAL = "select pid from OBSERVATION_GROUP where id=?";

	public static final String FIND_ID_OF_GRP = "select id from OBSERVATION_GROUP where pid=? and name=?";

	public static final String GET_OBS_CONSTR_SQL = "select "
			+"type, category, min, max " +"from " +"OBSERVING_CONSTRAINT "
			+"where gid=?";

	public static final String INSERT_OBSERVING_CONSTR_SQL = "insert into OBSERVING_CONSTRAINT ( "
			+"gid, type, category, min, max "
			+") values ("
			+"?, ?, ?, ?, ?)";

	public static final String DEL_OBS_CONSTR_SQL = "delete from OBSERVING_CONSTRAINT where gid = ?";

	public static final String LIST_GROUPS_USING_TARGET = "select " +"gid "
			+"from " +"SEQUENCE_COMPONENT " +"where eaType="
			+ExecutiveActionElementTypes.EXECUTIVE_TYPE_SLEW+" "
			+"and eaRef in (select id from EA_SLEW where targetRef = ?) ";

	public static final String LIST_GROUPS_USING_INSTRUMENT_CONFIG = "select "
			+"gid "
			+"from "
			+"SEQUENCE_COMPONENT "
			+"where eaRef=? "
			+"and eaType = "
			+ExecutiveActionElementTypes.EXECUTIVE_TYPE_INSTRUMENT_CONFIG_SELECTOR;

	public static final String LIST_GROUPS_WITH_TC_OF_TYPE = "select " +"id "
			+"from " +"OBSERVATION_GROUP "
			+"where tcid in (select id from TIMING_CONSTRAINT where type = ?)";

	public static final String LIST_ACTIVE_FIXED_GRP_IDs_SQL = "select id from OBSERVATION_GROUP where active and tcid in "
			+"(select id from TIMING_CONSTRAINT where type = 5 and (start > UNIX_TIMESTAMP(now()) * 1000 or end > UNIX_TIMESTAMP(now()) * 1000))";

	/** Public methods *******************************************************************/

	/**
	 * Public method for adding Group (and embedded TimingConstraint to the
	 * Database
	 * 
	 * @param pid
	 *            Proposal Id of group
	 * @param group
	 *            Group to add
	 * @return Id of Group added
	 * @throws Exception
	 *             If things go wrong
	 */
	public long addGroup(Connection connection, long pid, IGroup group)
			throws Exception {

		PreparedStatement stmt = null;
		try {
			// insert TimingConstraint
			long tcid = insertTimingConstraint(connection, group);

			boolean active = group.isActive();
			String name = group.getName();
			int priority = group.getPriority();
			boolean urgent = group.isUrgent();

			// prepare statement
			stmt = connection.prepareStatement(INSERT_GRP_SQL,
					Statement.RETURN_GENERATED_KEYS);

			stmt.setLong(1, pid);
			stmt.setLong(2, tcid);
			stmt.setBoolean(3, active);
			stmt.setString(4, name);
			stmt.setInt(5, priority);
			stmt.setBoolean(6, urgent);

			// execute query
			int gid = DatabaseTransactor.getInstance().executeUpdateStatement(
					connection, stmt, INSERT_GRP_SQL, true);

			// insert ObservingConstraints, using same connection
			insertObservingConstraints(connection, gid, group);

			// return gid
			return gid;
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

	/**
	 * Public method for deleting Group
	 * 
	 * @param id
	 *            Id of group to delete
	 * @throws Exception
	 * @throws Exception
	 *             If things go wrong
	 */
	public void deleteGroup(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_GRP_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);

			// delete timing constraints of group
			deleteTimingConstraintsOfGroup(connection, id);

			// execute query
			int numRows = DatabaseTransactor
					.getInstance()
					.executeUpdateStatement(connection, stmt, DEL_GRP_SQL, true);
			if (numRows == 0) {
				throw new Phase2Exception("No rows updated");
			}

			// delete obs constraints of group
			deleteObservingConstraintsOfGroup(connection, id);

			// delete obs sequence of group
			SequenceComponentAccessor sequenceComponentAccessor = new SequenceComponentAccessor();
			sequenceComponentAccessor.deleteObservationSequenceOfGroup(
					connection, id);
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

	public boolean groupExists(Connection connection, String groupName,
			long proposalId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_GROUP_ID_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, proposalId);
			stmt.setString(2, groupName);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, GET_GROUP_ID_SQL);
			if (resultSet == null) {
				return false;
			}
			return resultSet.next();
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

	/**
	 * 
	 * @param connection
	 * @param proposalID
	 * @param active
	 * @return
	 * @throws Exception
	 */
	public int getNumberOfGroups(Connection connection, long proposalID,
			boolean active) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_GRP_COUNT_SQL,
					Statement.RETURN_GENERATED_KEYS);

			stmt.setLong(1, proposalID);
			stmt.setBoolean(2, active);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, GET_GRP_COUNT_SQL);
			int count = -1;
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
			return count;
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

	/**
	 * Public method for getting a Group NOT including underlying observation
	 * sequence data
	 * 
	 * @param id
	 *            The Id of the group to get
	 * @return The group
	 * @throws SQLException
	 *             If things go wrong
	 * @throws Phase2Exception
	 */
	public IGroup getGroup(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = connection.prepareStatement(GET_GRP_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, GET_GRP_SQL);
			XGroup group = null;
			if (resultSet.next()) {
				group = getGroupFromResultSetCursor(resultSet);
				group = addObsConstraintsToGroup(connection, id, group);

				long tcid = getTimingConstraintIDFromResultSetCursor(resultSet);
				group = addTimingConstraintToGroup(connection, tcid, group);
			}
			return group;
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

	/**
	 * 
	 * @param connection
	 * @param pid Proposal id
	 * @return
	 * @throws Exception
	 */
	/*
	//This is not called from anywhere (we think) and it has the same name as a public method in Phase2Model (which means it's confusing that it is not called!)
	public ArrayList listActiveUnexpiredGroups(Connection connection, long pid) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			String statementString = LIST_ACTIVE_UNEXPIRED_GROUPS_SQL;

			stmt = connection.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, statementString);

			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getInt(1);
				IGroup group = getGroup(connection, id);
				groupsList.add(group);
			}
			return groupsList;
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
	*/
	
	/**
	 * For use by the scheduler, it will call: Phase2Model.listGroups(pid,
	 * includeInactiveGroups, includeExpiredGroups)
	 * 
	 * @param connection
	 * @param pid proposal id
	 * @param includeInactiveGroups
	 * @param includeExpiredGroups
	 * @return
	 * @throws Exception
	 */
	public ArrayList listGroups(Connection connection, long pid, boolean includeInactiveGroups, boolean includeExpiredGroups) throws Exception {
		if (includeExpiredGroups) {
			return listGroups(connection, pid, includeInactiveGroups);
		}

		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			String statementString = null;
			if (includeInactiveGroups) {
				// list all groups that haven't expired
				statementString = LIST_UNEXPIRED_GROUPS_SQL;
			} else {
				// list all active groups that haven't expired
				// SNF this is the old call: statementString = LIST_ALL_NON_EXPIRED_ACTIVE_GRP_IDs_SQL;
				statementString = LIST_ACTIVE_UNEXPIRED_GROUPS_SQL;
			}

			stmt = connection.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, statementString);

			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getInt(1);
				IGroup group = getGroup(connection, id);
				groupsList.add(group);
			}
			return groupsList;
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
	
	/**
	 * Public method for listing Groups on a Proposal
	 * 
	 * @param pid
	 *            Id of Proposal
	 * @param includeInactiveGroups
	 *            If true inactive groups are also shown alongside active ones,
	 *            else only active groups are displayed
	 * @return ArrayList of IGroup objects
	 * @throws SQLException
	 *             If things go wrong
	 * @throws Phase2Exception
	 *             If other things go wrong
	 */
	public ArrayList listGroups(Connection connection, long pid, boolean includeInactiveGroups) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			String statementString = null;
			if (includeInactiveGroups) {
				statementString = LIST_ALL_GRP_IDs_SQL;
			} else {
				statementString = LIST_ALL_ACTIVE_GRP_IDs_SQL;
			}

			stmt = connection.prepareStatement(statementString,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, pid);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, statementString);

			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getInt(1);
				IGroup group = getGroup(connection, id);
				groupsList.add(group);
			}
			return groupsList;
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

	/**
	 * Returns a list of groups that have a fixed timing constraint that at least ends in the future
	 * 
	 * @param connection
	 * @return list of IGroups
	 * @throws Exception
	 */
	public ArrayList listActiveFixedGroups(Connection connection) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			String statementString = LIST_ACTIVE_FIXED_GRP_IDs_SQL;

			stmt = connection.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, statementString);

			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getInt(1);
				IGroup group = getGroup(connection, id);
				groupsList.add(group);
			}
			return groupsList;
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

	public ArrayList listGroupsUsingTarget(Connection connection, ITarget target)
			throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = connection.prepareStatement(LIST_GROUPS_USING_TARGET,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, target.getID());

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, LIST_GROUPS_USING_TARGET);
			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long gid = resultSet.getLong(1);
				groupsList.add(getGroup(connection, gid));
			}
			return groupsList;
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

	public ArrayList listGroupsUsingInstrumentConfig(Connection connection,
			IInstrumentConfig instrumentConfig) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = connection.prepareStatement(
					LIST_GROUPS_USING_INSTRUMENT_CONFIG,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, instrumentConfig.getID());

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, LIST_GROUPS_USING_INSTRUMENT_CONFIG);
			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long gid = resultSet.getLong(1);
				groupsList.add(getGroup(connection, gid));
			}
			return groupsList;
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

	/**
	 * Returns a List of IGroup objects which use timing constraints of the
	 * specified type
	 * 
	 * @param connection
	 *            The database connection to use for the operation
	 * @param timingConstraintType
	 *            one of GroupTypes.*
	 * @return list of IGroup objects
	 * @throws Exception
	 *             if anything goes wrong in the process
	 */
	public ArrayList listGroupsWithTimingConstraintOfType(
			Connection connection, int timingConstraintType) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = connection.prepareStatement(LIST_GROUPS_WITH_TC_OF_TYPE,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, timingConstraintType);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, LIST_GROUPS_WITH_TC_OF_TYPE);
			ArrayList groupsList = new ArrayList();
			while (resultSet.next()) {
				long gid = resultSet.getLong(1);
				groupsList.add(getGroup(connection, gid));
			}
			return groupsList;
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

	/**
	 * Updates the group with the specified key. The operation also updates the
	 * underlying TimingConstraints and ObservingConstraints
	 * 
	 * @param connection
	 *            The database connection to use for the operation
	 * @param group
	 *            The new Group
	 * @param keyId
	 *            The LockingModel key allowing updating
	 * @throws SQLException
	 * @throws Exception
	 *             if anything goes wrong in the process
	 */
	public void updateGroup(Connection connection, IGroup group, long keyId)
			throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			// delete timing constraints of group
			deleteTimingConstraintsOfGroup(connection, group);

			// create timing constraints of group
			long tcid = insertTimingConstraint(connection, group);

			// delete observing constraints of group
			deleteObservingConstraintsOfGroup(connection, group);

			// create observing constraints of group
			insertObservingConstraints(connection, group.getID(), group);

			// SAME FOR OBSERVING PREFERENCES

			// now update OBSERVATION_GROUP table

			stmt = connection.prepareStatement(UPDATE_GROUP_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tcid);
			stmt.setBoolean(2, group.isActive());
			stmt.setString(3, group.getName());
			stmt.setLong(4, group.getPriority());
			stmt.setBoolean(5, group.isUrgent());

			stmt.setLong(6, group.getID());

			DatabaseTransactor.getInstance().executeUpdateStatement(connection,
					stmt, UPDATE_GROUP_SQL, false);
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

	/*
	 * Update the isUrgent flag on the group
	 */
	public void updateGroupUrgency(Connection connection, long groupId,
			boolean isUrgent, long keyID) throws Exception {
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement(UPDATE_GROUP_URGENCY_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setBoolean(1, isUrgent);
			stmt.setLong(2, groupId);

			DatabaseTransactor.getInstance().executeUpdateStatement(connection,
					stmt, UPDATE_GROUP_URGENCY_SQL, false);
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

	public long findIdOfGroupInProposal(Connection connection,
			String groupName, long proposalId) throws Exception {

		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(FIND_ID_OF_GRP,
					Statement.RETURN_GENERATED_KEYS);

			stmt.setLong(1, proposalId);
			stmt.setString(2, groupName);

			ResultSet resultSet = DatabaseTransactor.getInstance()
					.executeQueryStatement(stmt, FIND_ID_OF_GRP);
			long gid = -1;
			if (resultSet.next()) {
				gid = resultSet.getLong(1);
			}
			return gid;
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

	/**
	 * 
	 * @param groupId
	 * @return
	 * @throws SQLException
	 */
	public long findProposalIdOfGroup(Connection connection, long gid)
			throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(FIND_ID_OF_PROPOSAL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, FIND_ID_OF_PROPOSAL);
			long pid = -1;
			if (resultSet.next()) {
				pid = resultSet.getInt(1);
			}
			return pid;
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
	 * Private methods
	 * ******************************************************************
	 */

	/**
	 * For a specified XGroup, extract from the database related observing
	 * constraint objects and append them to the group object
	 * 
	 * @param gid
	 *            The group id of the group in the database
	 * @param group
	 *            The XGroup object to append Observing constraints to
	 * @return The\XGroup with the observing constraints appended
	 * @throws SQLException
	 *             If something goes wrong
	 */
	private XGroup addObsConstraintsToGroup(Connection connection, long gid,
			XGroup group) throws Exception {

		PreparedStatement stmt = null;
		ResultSet resultSet_obsConstraint = null;

		try {
			// ************** observing constraints ***********************
			stmt = connection.prepareStatement(GET_OBS_CONSTR_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);

			// observing constraint variables
			int observingConstraintType, category;
			double min, max;

			resultSet_obsConstraint = DatabaseTransactor.getInstance()
					.executeQueryStatement(stmt, GET_OBS_CONSTR_SQL);
			while (resultSet_obsConstraint.next()) {
				// for each observing constraint
				observingConstraintType = resultSet_obsConstraint.getInt(1);
				category = resultSet_obsConstraint.getInt(2);
				min = resultSet_obsConstraint.getDouble(3);
				max = resultSet_obsConstraint.getDouble(4);
				// System.err.println("Processing group: "+id+"/"+name +
				// " OC: type="+octype+"category=" + category +
				// ", min="+min+", max="+max);
				switch (observingConstraintType) {
				case ObservingConstraintTypes.SEEING_CONSTRAINT:
					XSeeingConstraint seeConstraint = new XSeeingConstraint(max);
					group.addObservingConstraint(seeConstraint);
					break;
				case ObservingConstraintTypes.EXTINCTION_CONSTRAINT:
					XPhotometricityConstraint photConstraint = new XPhotometricityConstraint(
							category, max);
					group.addObservingConstraint(photConstraint);
					break;
				case ObservingConstraintTypes.HOUR_ANGLE_CONSTRAINT:
					XHourAngleConstraint hourAngleConstraint = new XHourAngleConstraint(
							min, max);
					group.addObservingConstraint(hourAngleConstraint);
					break;
				case ObservingConstraintTypes.AIRMASS_CONSTRAINT:
					XAirmassConstraint airmassConstraint = new XAirmassConstraint(
							max);
					group.addObservingConstraint(airmassConstraint);
					break;
				case ObservingConstraintTypes.SKY_BRIGHTNESS_CONSTRAINT:
					XSkyBrightnessConstraint skyBrightnessConstraint = new XSkyBrightnessConstraint(
							category);
					group.addObservingConstraint(skyBrightnessConstraint);
					break;
				}
				/*
				 * case ObservingConstraintTypes.SOLAR_ELEVATION_CONSTRAINT:
				 * XSolarElevationConstraint solConstraint = new
				 * XSolarElevationConstraint( category);
				 * group.addObservingConstraint(solConstraint); break; case
				 * ObservingConstraintTypes.LUNAR_DISTANCE_CONSTRAINT:
				 * XLunarDistanceConstraint lunarDistConstraint = new
				 * XLunarDistanceConstraint( min);
				 * group.addObservingConstraint(lunarDistConstraint); break;
				 * case ObservingConstraintTypes.LUNAR_ELEVATION_CONSTRAINT:
				 * XLunarElevationConstraint lunarElevConstraint = new
				 * XLunarElevationConstraint( category);
				 * group.addObservingConstraint(lunarElevConstraint); break;
				 * case ObservingConstraintTypes.LUNAR_PHASE_CONSTRAINT:
				 * XLunarPhaseConstraint lunarPhaseConstraint = new
				 * XLunarPhaseConstraint( max);
				 * group.addObservingConstraint(lunarPhaseConstraint); break; }
				 */
			}
			return group;
		} finally {
			try {
				if (resultSet_obsConstraint != null) {
					resultSet_obsConstraint.close();
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

	/**
	 * For a specified XGroup, extract from the database related timing
	 * constraint objects and append them to the group object
	 * 
	 * @param tcid
	 *            ID of the timing constraints to append to the object
	 * @param group
	 *            The XGroup to append the timing constraints to
	 * @return The XGroup with the Timing constraint object appended
	 * @throws SQLException
	 *             If things go wrong
	 * @throws Phase2Exception
	 */
	private XGroup addTimingConstraintToGroup(Connection connection, long tcid,
			XGroup group) throws Exception {

		TimingConstraintAccessor timingConstraintAccessor = new TimingConstraintAccessor();
		ITimingConstraint timingConstraint = timingConstraintAccessor
				.getTimingConstraint(connection, tcid);
		group.setTimingConstraint(timingConstraint);
		return group;
	}

	/**
	 * Insert into the database the Timing constraints of the specified IGroup
	 * 
	 * @param group
	 *            The group who's timing constraints are to be inserted into the
	 *            database;
	 * @throws Exception
	 * @throws Exception
	 *             If things go wrong
	 */
	private int insertTimingConstraint(Connection connection, IGroup group)
			throws Exception {

		ITimingConstraint timingConstraint = group.getTimingConstraint();

		TimingConstraintAccessor timingConstraintAccessor = new TimingConstraintAccessor();
		return timingConstraintAccessor.insertTimingConstraint(connection,
				timingConstraint);
	}

	private void insertObservingConstraints(Connection connection, long gid,
			IGroup group) throws Exception {
		PreparedStatement stmt = null;
		try {
			List obsConList = group.listObservingConstraints();
			if (obsConList == null) {
				return;
			}

			Iterator i = obsConList.iterator();
			while (i.hasNext()) {
				IObservingConstraint obsConstraint = (IObservingConstraint) i
						.next();
				long id;
				int type = -1;
				int category = -1;
				double min = 0, max = 0;

				Class constraintClass = obsConstraint.getClass();
				if (constraintClass.equals(XSeeingConstraint.class)) {
					type = ObservingConstraintTypes.SEEING_CONSTRAINT;
					XSeeingConstraint xSeeingConstraint = (XSeeingConstraint) obsConstraint;
					max = xSeeingConstraint.getSeeingValue();

				} else if (constraintClass
						.equals(XPhotometricityConstraint.class)) {
					type = ObservingConstraintTypes.EXTINCTION_CONSTRAINT;
					XPhotometricityConstraint xPhotomConstraint = (XPhotometricityConstraint) obsConstraint;
					category = xPhotomConstraint.getPhotometricityCategory();
					max = xPhotomConstraint.getMaximumExtinction();

				} else if (constraintClass
						.equals(XSkyBrightnessConstraint.class)) {
					type = ObservingConstraintTypes.SKY_BRIGHTNESS_CONSTRAINT;
					XSkyBrightnessConstraint xSkyBrightnessConstraint = (XSkyBrightnessConstraint) obsConstraint;
					category = xSkyBrightnessConstraint
							.getSkyBrightnessCategory();

					/*
					 * } else if
					 * (constraintClass.equals(XSolarElevationConstraint.class))
					 * { type =
					 * ObservingConstraintTypes.SOLAR_ELEVATION_CONSTRAINT;
					 * XSolarElevationConstraint xSolarElevConstraint =
					 * (XSolarElevationConstraint) obsConstraint; category =
					 * xSolarElevConstraint .getMaximumSolarElevationCategory();
					 * 
					 * } else if
					 * (constraintClass.equals(XLunarDistanceConstraint.class))
					 * { type =
					 * ObservingConstraintTypes.LUNAR_DISTANCE_CONSTRAINT;
					 * XLunarDistanceConstraint xLunarDistConstraint =
					 * (XLunarDistanceConstraint) obsConstraint; min =
					 * xLunarDistConstraint.getMinimumLunarDistance();
					 * 
					 * } else if
					 * (constraintClass.equals(XLunarElevationConstraint.class))
					 * { type =
					 * ObservingConstraintTypes.LUNAR_ELEVATION_CONSTRAINT;
					 * XLunarElevationConstraint xLunarElevConstraint =
					 * (XLunarElevationConstraint) obsConstraint; category =
					 * xLunarElevConstraint.getLunarElevationCategory();
					 */

				} else if (constraintClass.equals(XHourAngleConstraint.class)) {
					type = ObservingConstraintTypes.HOUR_ANGLE_CONSTRAINT;
					XHourAngleConstraint xHourAngleConstraint = (XHourAngleConstraint) obsConstraint;
					min = xHourAngleConstraint.getMinimumHourAngle();
					max = xHourAngleConstraint.getMaximumHourAngle();

				} else if (constraintClass.equals(XAirmassConstraint.class)) {
					type = ObservingConstraintTypes.AIRMASS_CONSTRAINT;
					XAirmassConstraint xAirmassConstraint = (XAirmassConstraint) obsConstraint;
					max = xAirmassConstraint.getMaximumAirmass();
				}
				/*
				 * } else if
				 * (constraintClass.equals(XLunarPhaseConstraint.class)) { type
				 * = ObservingConstraintTypes.LUNAR_PHASE_CONSTRAINT;
				 * XLunarPhaseConstraint xLunarPhaseConstraint =
				 * (XLunarPhaseConstraint) obsConstraint; max =
				 * xLunarPhaseConstraint.getMaximumLunarPhase(); }
				 */
				stmt = connection.prepareStatement(INSERT_OBSERVING_CONSTR_SQL,
						Statement.RETURN_GENERATED_KEYS);
				stmt.setLong(1, gid);
				stmt.setLong(2, type);
				stmt.setInt(3, category);
				stmt.setDouble(4, min);
				stmt.setDouble(5, max);
				id = DatabaseTransactor.getInstance().executeUpdateStatement(
						connection, stmt, INSERT_OBSERVING_CONSTR_SQL, true);
			} // next observing constraint
		} finally {
			try {
				if (stmt != null) {
					stmt.close(); // only closes the latest one I guess, ho humm
									// - am sure gc will get others
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}

	/**
	 * Given a ResultSet row accessing columns 'id, pid, tcid, name', returns a
	 * XGroup object representing the group row
	 * 
	 * @param resultSet
	 *            The ResultSet with the result cursor moved into the row
	 * @return An XGroup representing the row
	 * @throws SQLException
	 *             If things go wrong
	 */
	private XGroup getGroupFromResultSetCursor(ResultSet resultSet)
			throws Exception {

		/*
		 * public static final String GET_GRP_SQL = "select " +
		 * "id, pid, tcid, osid, active, name, priority, urgent " + "from " +
		 * "OBSERVATION_GROUP " + "where id=?";
		 */

		// group variables
		long id, pid, tcid, osid;
		boolean active, urgent;
		String name;
		int priority;

		id = resultSet.getInt(1);
		pid = resultSet.getInt(2);
		tcid = resultSet.getInt(3); // timing constraint id
		osid = resultSet.getInt(4); // observation sequence id
		active = resultSet.getBoolean(5); // is active
		name = resultSet.getString(6);
		priority = resultSet.getInt(7);
		urgent = resultSet.getBoolean(8); // is urgent

		// build group
		XGroup group = new XGroup();
		group.setID(id);
		group.setActive(active);
		group.setName(name);
		group.setPriority(priority);
		group.setUrgent(urgent);
		return group;
	}

	/**
	 * Given a ResultSet row accessing columns 'id, pid, tcid, name', returns
	 * the tcid value
	 * 
	 * @param resultSet
	 *            The ResultSet with a cursor moved onto the required data row
	 * @return The TimingConstraint ID in the current ResultSet row
	 * @throws SQLException
	 *             If things wrong
	 */
	private long getTimingConstraintIDFromResultSetCursor(ResultSet resultSet)
			throws Exception {
		long tcid = resultSet.getInt(3);
		return tcid;
	}

	private long getObsSequenceIDFromResultSetCursor(ResultSet resultSet)
			throws Exception {
		long osid = resultSet.getInt(4);
		return osid;
	}

	private void deleteTimingConstraintsOfGroup(Connection connection,
			IGroup group) throws Exception {
		deleteTimingConstraintsOfGroup(connection, group.getID());
	}

	private void deleteTimingConstraintsOfGroup(Connection connection, long gid)
			throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

		try {
			stmt = connection.prepareStatement(GET_GRP_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);

			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(
					stmt, GET_GRP_SQL);
			resultSet.next();

			long tcid = getTimingConstraintIDFromResultSetCursor(resultSet);

			stmt.clearBatch();
			stmt.clearParameters();

			TimingConstraintAccessor timingConstraintAccessor = new TimingConstraintAccessor();
			timingConstraintAccessor.deleteTimingConstraint(connection, tcid);
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

	private void deleteObservingConstraintsOfGroup(Connection connection,
			IGroup group) throws Exception {
		deleteObservingConstraintsOfGroup(connection, group.getID());
	}

	private void deleteObservingConstraintsOfGroup(Connection connection,
			long gid) throws Exception {
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement(DEL_OBS_CONSTR_SQL,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, gid);
			DatabaseTransactor.getInstance().executeUpdateStatement(connection,
					stmt, DEL_OBS_CONSTR_SQL, false);
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
