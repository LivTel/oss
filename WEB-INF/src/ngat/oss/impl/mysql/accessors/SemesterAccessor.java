package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.phase2.ISemester;
import ngat.phase2.ISemesterPeriod;
import ngat.phase2.XSemester;
import ngat.phase2.XSemesterPeriod;

import org.apache.log4j.Logger;

public class SemesterAccessor {
	
	public static final int MAX_SEMESTER_ID = 40;
	
	/*	
	SEMESTER;
		id				int
		name		String
		startDate	datetime
		endDate 	datetime
  	*/
	
	static Logger logger = Logger.getLogger(SemesterAccessor.class);
	
	//statements
	
	public static final String INSERT_SEMESTER_SQL = 						
		"insert into SEMESTER (" +
		"name, startDate, endDate" + 
		") values (" + 
		"?, ?, ?)";
	
	public static final String GET_SEMESTER_SQL = 							
		"select " +
		"id, name, startDate, endDate " +
		"from " +
		"SEMESTER " +
		"where id=?";
	
	public static final String FIND_SEMESTER_SQL = 							
		"select " +
		"id, name, startDate, endDate " +
		"from " +
		"SEMESTER " +
		"where name=?";
	
	public static final String GET_SEMESTER_OF_DATE_SQL = 							
		"select " +
		"id, name, startDate, endDate " +
		"from " +
		"SEMESTER " +
		"where startDate <= ? " +
		"and endDate > ?";
	
	public static final String LIST_SEMESTERS_FROM_ID_SQL = 
		"select " +
		"id, name, startDate, endDate " +
		"from " +
		"SEMESTER " +
		"where id >= ?";
	
	public static final String DEL_SEMESTER_SQL = 							
		"delete from SEMESTER where id = ?";
	
	public static final String UPDATE_SEMESTER_SQL =					
		"update SEMESTER "+
		"set " + 
		"name=?," + 
		"startDate=?,"+
		"endDate=?";

	
	/** Public methods ******************************************************************
	 * @throws Exception */
	
	public long addSemester(Connection connection, ISemester semester) throws Exception {
		PreparedStatement stmt = null;
		
		try {
			//prepare statement
			stmt = connection.prepareStatement(INSERT_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, semester.getName());
			stmt.setLong(2, semester.getStartDate());
			stmt.setLong(3, semester.getEndDate());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_SEMESTER_SQL, true);
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
	
	public void deleteSemester(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(DEL_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, id);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_SEMESTER_SQL, true);
			if (numRows ==0) {
				throw new Exception("No rows updated");
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
	}
	
	public ISemester getSemester(Connection connection, long id)  throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(GET_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_SEMESTER_SQL);
			ISemester semester = null;
			if (resultSet.next()) {
				semester = getSemesterFromResultSetCursor(resultSet);			
			}
			return semester;
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
	
	public ISemester findSemester(Connection connection, String name)  throws Exception {
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement(FIND_SEMESTER_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, FIND_SEMESTER_SQL);
			ISemester semester = null;
			if (resultSet.next()) {
				semester = getSemesterFromResultSetCursor(resultSet);			
			}
			return semester;
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
	
	public List listSemestersFromDate(Connection connection, long dateTime) throws Exception {

		ISemester startSemester = getSemesterPeriodOfDate(connection, dateTime).getFirstSemester();
		if (startSemester == null) {
			return new ArrayList<ISemester>(); //empty list
		}
		
		return listSemestersFromID(connection, startSemester.getID());
	}
	
	private ArrayList listSemestersFromID(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LIST_SEMESTERS_FROM_ID_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, id);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_SEMESTERS_FROM_ID_SQL);
			
			ArrayList list = new ArrayList<ISemester>();
			while (resultSet.next()) {
				ISemester semester = getSemesterFromResultSetCursor(resultSet);
				list.add(semester);
			}
			return list;
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

	public ISemesterPeriod getSemesterPeriodOfDate(Connection connection, long dateTime) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_SEMESTER_OF_DATE_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, dateTime);
			stmt.setLong(2, dateTime);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_SEMESTER_OF_DATE_SQL);
			
			ArrayList list = new ArrayList<ISemester>();
			int numberOfSemesters = 0;
			while (resultSet.next()) {
				ISemester semester = getSemesterFromResultSetCursor(resultSet);
				list.add(semester);
				numberOfSemesters ++;
			}
			if (numberOfSemesters ==0) {
				//return semester period without any semesters
				return new XSemesterPeriod();
			} else if (numberOfSemesters ==1) {
				//return semester period with the only semester found.
				return new XSemesterPeriod((ISemester)list.get(0));
			} else  if (numberOfSemesters ==2) {
				//return semester period with both semesters that overlap this date, however you need to make sure they are the right way round
				ISemester firstSemester = (ISemester)list.get(0);
				ISemester secondSemester = (ISemester)list.get(1);
				if (firstSemester.getID() < secondSemester.getID()) {
					return new XSemesterPeriod(firstSemester, secondSemester, true);
				} else {
					return new XSemesterPeriod(secondSemester, firstSemester, true);
				}
				
			} else {
				logger.error("weirdly, the date " + dateTime +" was found to exist in more than 2 semesters" );
				return null;
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
	
	
	
	/* Private  methods ******************************************************************/

	/**
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private ISemester getSemesterFromResultSetCursor(ResultSet resultSet) throws Exception {

		long id, startDate, endDate;
		String name; 
		
		//id, name, startDate, endDate
		
		id 				= resultSet.getLong(1);
		name 		= resultSet.getString(2);
		startDate	= resultSet.getLong(3);
		endDate	= resultSet.getLong(4);
		
		XSemester semester = new XSemester();
		semester.setID(id);
		semester.setName(name);
		semester.setStartDate(startDate);
		semester.setEndDate(endDate);
	
		return semester;
	}
}
