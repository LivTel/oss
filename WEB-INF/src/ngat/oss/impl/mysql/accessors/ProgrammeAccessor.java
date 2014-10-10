package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.IAccessPermission;
import ngat.phase2.IProgram;
import ngat.phase2.IProposal;
import ngat.phase2.XProgram;

import org.apache.log4j.Logger;

public class ProgrammeAccessor {
	
	static Logger logger = Logger.getLogger(ProgrammeAccessor.class);
	
	/*
	 PROGRAMME
		 id 				int
		 name 			String
		 description 	String
	*/
	
	//statements	
	public static final String INSERT_PROG_SQL = 						
		"insert into PROGRAMME (" +
		"id, name, description" + 
		") values (" + 
		"?, ?, ?)";

	public static final String GET_PROG_SQL = 							
		"select " +
		"id, name, description " +
		"from " +
		"PROGRAMME " +
		"where id=?";
	
	public static final String GET_PROG_ID_OF_PROPOSAL_SQL = 							
		"select " +
		"pid " +
		"from " +
		"PROPOSAL " +
		"where id=?";
	
	public static final String FIND_PROGRAMME_ID_SQL = 							
		"select " +
		"id " +
		"from " +
		"PROGRAMME " +
		"where name=?";
	
	public static final String LIST_PROG_IDS_SQL = 							
		"select " +
		"id " +
		"from " + 
		"PROGRAMME " +
		"order by name";
	
	public static final String DEL_PROG_SQL = 							
		"delete from PROGRAMME where id = ?";
	
	public static final String UPDATE_PROG_SQL =					
		"update PROGRAMME "+
		"set " + 
		"name=?,"+
		"description=? " +
		"where id=?";
	
	/** Public methods *******************************************************************/
	
	public long addProgramme(Connection connection, IProgram prog) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			//table fields
			long id;
			String name, description;
			
			//load values
			id = prog.getID();
			name = prog.getName();
			description = prog.getDescription();
			
			//prepare statement
			stmt = connection.prepareStatement(INSERT_PROG_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			stmt.setString(2, name);
			stmt.setString(3, description);
			
			//execute query
			long pid = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_PROG_SQL, true);
			
			return pid;
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
	
	public void deleteProgramme(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			//delete header only at the moment
			stmt = connection.prepareStatement(DEL_PROG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_PROG_SQL, true);
			if (numRows ==0) {
				throw new Phase2Exception("No rows updated");
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
		//delete targets and instrument configs of programme??
	}
	
	public IProgram getProgramme(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_PROG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_PROG_SQL);
			IProgram programme = null;
			if (resultSet.next()) {
				programme = getProgrammeFromResultSetCursor(resultSet);
			}
			return programme;
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
	
	public IProgram getProgrammeOfProposal(Connection connection, long proposalId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_PROG_ID_OF_PROPOSAL_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, proposalId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_PROG_ID_OF_PROPOSAL_SQL);
			if (resultSet.next()) {
				long progId = resultSet.getLong(1);
				return getProgramme(connection, progId);
			} else {
				throw new Phase2Exception("proposal " + proposalId + " does not belong to a programme");
			}
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
	
	public IProgram findProgramme(Connection connection, String name) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(FIND_PROGRAMME_ID_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, name);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, FIND_PROGRAMME_ID_SQL);
			IProgram programme = null;
			if (resultSet.next()) {
				long id = resultSet.getLong(1);
				return getProgramme(connection, id);
			}
			return null;
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
	
	public ArrayList listProgrammes(Connection connection) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(LIST_PROG_IDS_SQL, Statement.RETURN_GENERATED_KEYS);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_PROG_IDS_SQL);
			
			ArrayList programmeList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getLong(1);
				IProgram programme = getProgramme(connection, id);
				programmeList.add(programme);
			}
			return programmeList;
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
	 * @param uid
	 * @return
	 */
	public ArrayList listProgrammesOfUser(Connection connection, long uid) throws Exception {
		
		ArrayList programmeList = new ArrayList();
		
		AccessPermissionAccessor accessPermissionAccessor = new AccessPermissionAccessor();
		ProposalAccessor proposalAccessor = new ProposalAccessor();
		
		List accessPermissionsOfUser = accessPermissionAccessor.listAccessPermissionsOfUser(connection, uid);
		
		Iterator accessPermissionIterator = accessPermissionsOfUser.iterator();
		while (accessPermissionIterator.hasNext()) {
			IAccessPermission accessPermission = (IAccessPermission)accessPermissionIterator.next();
			IProposal proposal = proposalAccessor.getProposal(connection, accessPermission.getProposalID());
			long proposalId = proposal.getID();
			IProgram program = getProgrammeOfProposal(connection, proposalId);
			if (!programmeListContainsProgrammeName(programmeList, program.getName())) {
				programmeList.add(program);
			}
		}
		
		return programmeList;
		
	}
	
	private boolean programmeListContainsProgrammeName(ArrayList programmeList, String programmeName) {
		
		Iterator pli = programmeList.iterator();
		while (pli.hasNext()) {
			IProgram program = (IProgram) pli.next();
			if (program.getName().trim().equals(programmeName.trim())) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Updates the Programme with the specified key. 
	 * @param connection The database connection to use for the operation
	 * @param prog The new Programme
	 * @param keyId The LockingModel key allowing updating
	 * @throws Exception If anything goes wrong during the process
	 */
	public void updateProgramme(Connection connection, IProgram prog, long keyId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(UPDATE_PROG_SQL, Statement.RETURN_GENERATED_KEYS);
		
			stmt.setString(1, prog.getName());
			stmt.setString(2, prog.getDescription());
			stmt.setLong(3, prog.getID());
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_PROG_SQL, false);
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
	
	
	/** Private  methods *******************************************************************/
	 
	private IProgram getProgrammeFromResultSetCursor(ResultSet resultSet) throws Exception { 
		
		long id;
		String name, description;
		
		id				= resultSet.getLong(1);
		name		= resultSet.getString(2);
		description	= resultSet.getString(3);
		
		XProgram programme = new XProgram();
		programme.setID(id);
		programme.setName(name);
		programme.setDescription(description);

		return programme;
	}
	
	/*
	public String getStuff() throws Exception {
		try {
			System.err.println("trying");
			double number = Math.random(); 
			System.err.println("number=" + number);
			if (number > 0.5) {
				throw new Exception("e1");
			}
			return "stuff";
		//} catch (Exception e1) {
		//	System.err.println("caught e1");
		//	throw e1;
		} finally {
			System.err.println("finally");
			try {
				throw new Exception("in finally");
			} catch (Exception e) {}
		}
	}
	
	public static void main(String[] a) {
		ProgrammeAccessor pa = new ProgrammeAccessor();
		try {
			String myString = pa.getStuff();
			System.err.println("... myString=" + myString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
