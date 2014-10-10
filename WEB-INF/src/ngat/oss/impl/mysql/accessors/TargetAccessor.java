package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.TargetTypes;
import ngat.phase2.IOrbitalElements;
import ngat.phase2.ITarget;
import ngat.phase2.XEphemerisTarget;
import ngat.phase2.XEphemerisTrackNode;
import ngat.phase2.XExtraSolarTarget;
import ngat.phase2.XOrbitalElementsTarget;
import ngat.phase2.XSlaCometElements;
import ngat.phase2.XSlaMajorPlanetElements;
import ngat.phase2.XSlaMinorPlanetElements;
import ngat.phase2.XSlaNamedPlanetTarget;
import ngat.phase2.XTarget;

import org.apache.log4j.Logger;

public class TargetAccessor {
	
	static Logger logger = Logger.getLogger(TargetAccessor.class);
	
	/*
	TARGET
		id				int
		pid			int
		name		String
		type			int
		targetRef	int	
	*/
	/*
	TARGET_CATALOG;
		id				int
		catIndex	int
	*/
	/*
	 TARGET_EPHEMERIS;
	 	id				int
	 	tid				int
	 	time			datetime
	 	ra				double
	 	decl			double
	 	raDot			double
	 	decDot		double   
	 */
	/*
	 TARGET_EXTRA_SOLAR	 
	 	id				long
	 	ra        		double
	 	decl			double
	 	pmra			double
	 	pmdec		double
	 	radialVel	double
	 	parallax		double
	 	epoch		double
	 	frame		int
	 */
	/*
	 TARGET_ORBITAL_ELEMENTS;
	 	id				int
	 	epoch		double
	 	orbinc		double
	 	anode		double
	 	perih			double
	 	aorq 			double
	 	ecc    		double
	 	aorl   		double
	 	dm			double
	 */
	
	//statements
	
	// INSERT **************************
	public static final String INSERT_TARGET_SQL = 						
		"insert into TARGET (" +
		"pid, name, type, targetRef" + 
		") values (" + 
		"?, ?, ?, ?)";
	
	public static final String INSERT_CATALOG_TARGET_SQL = 						
		"insert into TARGET_CATALOG (" +
		"catIndex" + 
		") values (" + 
		"?)";
	
	public static final String INSERT_EPHEMERIS_TARGET_SQL = 						
		"insert into TARGET_EPHEMERIS (" +
		"tid, time, ra, decl, raDot, decDot" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?)";
	
	public static final String INSERT_EXTRA_SOLAR_TARGET_SQL = 						
		"insert into TARGET_EXTRA_SOLAR (" +
		"ra, decl, pmra, pmdec, radialVel, parallax, epoch, frame" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static final String INSERT_ORBITAL_TARGET_SQL = 						
		"insert into TARGET_ORBITAL_ELEMENTS (" +
		"epoch, orbinc, anode, perih, aorq, ecc, aorl, dm" + 
		") values (" + 
		"?, ?, ?, ?, ?, ?, ?, ?)";
	
	// GET **************************
	public static final String GET_TARGET_SQL = 							
		"select " +
		"id, pid, name, type, targetRef " +
		"from " +
		"TARGET " +
		"where id=?";

	public static final String GET_EA_TARGET_COMPONENT = 							
		"select " +
		"parentId, name, type, targetRef " +
		"from " +
		"EA_TARGET_COMPONENT " +
		"where id=?";
	
	public static final String GET_CATALOG_TARGET_SQL = 							
		"select " +
		"catIndex " +
		"from " +
		"TARGET_CATALOG " +
		"where id=?";
	
	public static final String GET_EPHEMERIS_TARGETS_SQL = 							
		"select " +
		"id, time, ra, decl, raDot, decDot " +
		"from " +
		"TARGET_EPHEMERIS " +
		"where tid=?";
	
	public static final String GET_EXTRA_SOLAR_TARGET_SQL = 							
		"select " +
		"ra, decl, pmra, pmdec, radialVel, parallax, epoch, frame " +
		"from " +
		"TARGET_EXTRA_SOLAR " +
		"where id=?";
	
	public static final String GET_ORBITAL_TARGET_SQL = 							
		"select " +
		"epoch, orbinc, anode, perih, aorq, ecc, aorl, dm " +
		"from " +
		"TARGET_ORBITAL_ELEMENTS " +
		"where id=?";
	
	public static final String FIND_TARGET_ID_SQL = 							
		"select " +
		"id " +
		"from " +
		"TARGET " +
		"where name=? "+
		"and pid=?";
	
	//UPDATE **************************
	public static final String UPDATE_TARGET_SQL =					
		"update TARGET "+
		"set " +  
		"name=?, " + 
		"targetRef=? " +
		"where id=?";
	
	public static final String UPDATE_EPHEMERIS_TARGETS_PARENT = 
		"update TARGET_EPHEMERIS "+
		"set " + 
		"tid=?," +
		"where id=?";
	
	//LIST **************************
	public static final String LIST_TARGET_IDS_OF_PROGRAMME_SQL = 							
		"select " +
		"id " +
		"from " +
		"TARGET " +
		"where pid=? " +
		"order by name";
	
	//FIND **************************
	public static final String FIND_PROPOSAL_ID_OF_TARGET_SQL = 							
		"select " +
		"pid " +
		"from " +
		"TARGET " +
		"where id=?";
	
	//DELETE **************************
	public static final String DEL_TARGET_SQL = 							
		"delete from TARGET where id = ?";
	
	public static final String DEL_ORBITAL_ELEMENTS_TARGET_SQL = 							
		"delete from TARGET_ORBITAL_ELEMENTS where id = ?";
	
	public static final String DEL_CATALOG_TARGET_SQL = 							
		"delete from TARGET_CATALOG where id = ?";
	
	public static final String DEL_EXTRA_SOLAR_TARGET_SQL = 							
		"delete from TARGET_EXTRA_SOLAR where id = ?";
	
	public static final String DEL_EPHEMERIS_TARGETS_SQL = 							
		"delete from TARGET_EPHEMERIS where tid = ?";
	
	/** Public methods *****************************************************************
	 * @throws Exception */
	
	public long addTarget(Connection connection, long progId, ITarget target) throws Exception {
		PreparedStatement stmt = null;
		try {
			//table fields
			long id;
			String name;
			int type;
			long targetRef = 0;
			
			//load values
			name = target.getName();
			if (target instanceof XExtraSolarTarget ) {
				type = TargetTypes.EXTRA_SOLAR_TARGET;
				targetRef = addExtraSolarTarget(connection, (XExtraSolarTarget)target);
			} else if (target instanceof XOrbitalElementsTarget) { 
				XOrbitalElementsTarget xOrbitalElementsTarget = (XOrbitalElementsTarget)target;
				IOrbitalElements orbitalElements = xOrbitalElementsTarget.getElements();
				if (orbitalElements instanceof XSlaCometElements) {
					type = TargetTypes.SLA_COMET_ELEMENTS;
					targetRef = addCometElementsTarget(connection, (XSlaCometElements)target);
				} else if (orbitalElements instanceof XSlaMajorPlanetElements) {
					type = TargetTypes.SLA_MAJOR_PLANET_ELEMENTS;
					targetRef = addMajorPlanetElementsTarget(connection, (XSlaMajorPlanetElements)target);
				} else if (orbitalElements instanceof XSlaMinorPlanetElements) {
					type = TargetTypes.SLA_MINOR_PLANET_ELEMENTS;
					targetRef = addMinorPlanetElementsTarget(connection, (XSlaMinorPlanetElements)target);
				} else {
					throw new Phase2Exception("unknown orbital elements target class");
				}
			} else if (target instanceof XSlaNamedPlanetTarget) {
				type = TargetTypes.SLA_CATALOG_TARGET;
				targetRef = addCatalogTarget(connection, (XSlaNamedPlanetTarget)target);
			} else if (target instanceof XEphemerisTarget) {
				type = TargetTypes.EPHEMERIS_TARGET;
			} else {
				throw new Phase2Exception("unknown target class");
			}
			
			//prepare statement	/*
			stmt = connection.prepareStatement(INSERT_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, progId);
			stmt.setString(2, name);
			stmt.setInt(3, type);
			stmt.setLong(4, targetRef);
			
			//execute query
			long tid =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_TARGET_SQL, true);
			
			//ephemeris targets require a one to many link to be established, so we need to go and populate the tid field if it's an ephemeris target
			if (target instanceof XEphemerisTarget) {
				addEphemerisTarget(connection, (XEphemerisTarget)target, tid);
			}
			return tid;
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
	 * This method adds an ephemeris target. however the tid field (the parent target id field) is not populated
	 * @param connection
	 * @param target
	 * @return
	 * @throws Exception
	 */
	private void addEphemerisTarget(Connection connection, XEphemerisTarget target, long tid) throws Exception {
		
		//fields for ephemeris target
		long time;
		double ra, decl, raDot, decDot;
		
		SortedSet ephemerisTrack = target.getEphemerisTrack();
		if (ephemerisTrack != null) {
			Iterator trackIterator = ephemerisTrack.iterator();
			while (trackIterator.hasNext()) {
				XEphemerisTrackNode ephemerisTrackNode = (XEphemerisTrackNode)trackIterator.next();
				time = ephemerisTrackNode.time;
				ra = ephemerisTrackNode.ra;
				decl = ephemerisTrackNode.dec;
				raDot = ephemerisTrackNode.raDot;
				decDot = ephemerisTrackNode.decDot;
				//prepare new statement using same connection
				PreparedStatement stmt = null;
				try {
					stmt = connection.prepareStatement(INSERT_EPHEMERIS_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
					stmt.setLong(1, tid);
					stmt.setLong(2, time);
					stmt.setDouble(3, ra);
					stmt.setDouble(4, decl);
					stmt.setDouble(5, raDot);
					stmt.setDouble(6, decDot);
					
					//execute query
					DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_EPHEMERIS_TARGET_SQL, true);
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
	}
	
	private long addExtraSolarTarget(Connection connection, XExtraSolarTarget target) throws Exception {
		PreparedStatement stmt = null;
		try {
			//prepare statement
			double ra, decl, pmra, pmdec, radialVel, parallax, epoch;
			int frame;
			ra = target.getRa();
			decl = target.getDec();
			pmra = target.getPmRA();
			pmdec = target.getPmDec();
			radialVel = target.getRadialVelocity();
			parallax = target.getParallax();
			epoch = target.getEpoch();
			frame = target.getFrame();
			
			stmt = connection.prepareStatement(INSERT_EXTRA_SOLAR_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, ra);
			stmt.setDouble(2, decl);
			stmt.setDouble(3, pmra);
			stmt.setDouble(4, pmdec);
			stmt.setDouble(5, radialVel);
			stmt.setDouble(6, parallax);
			stmt.setDouble(7, epoch);
			stmt.setInt(8, frame);
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_EXTRA_SOLAR_TARGET_SQL, true);
			ConnectionPool.getInstance().surrenderConnection(connection);
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
	
	
	private long addCometElementsTarget(Connection connection, XSlaCometElements target) throws Exception {
		PreparedStatement stmt = null;
		try {
			double epoch, orbinc, anode, perih, aorq, ecc, aorl, dm;
			
			epoch = target.getElementEpoch();
			orbinc = target.getOrbitalInc();
			anode = target.getLongAscNode();
			perih = target.getArgPeri();
			aorq = target.getPeriDist();
			ecc = target.getEccentricity();
			aorl = 0; //not a comet element
			dm = 0; //not a comet element
			
			stmt = connection.prepareStatement(INSERT_ORBITAL_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, epoch);
			stmt.setDouble(2, orbinc);
			stmt.setDouble(3, anode);
			stmt.setDouble(4, perih);
			stmt.setDouble(5, aorq);
			stmt.setDouble(6, ecc);
			stmt.setDouble(7, aorl);
			stmt.setDouble(8, dm);
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_ORBITAL_TARGET_SQL, true);
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
	
	private long addMajorPlanetElementsTarget(Connection connection, XSlaMajorPlanetElements target) throws Exception {
		PreparedStatement stmt = null;
		try {
			double epoch, orbinc, anode, perih, aorq, ecc, aorl, dm;
			epoch = target.getElementEpoch();
			orbinc = target.getOrbitalInc();
			anode = target.getLongAscNode();
			perih = target.getLongPeri();
			aorq = target.getMeanDistance();
			ecc = target.getEccentricity();
			aorl = target.getMeanLongitude();
			dm = target.getDailyMotion();
			
			stmt = connection.prepareStatement(INSERT_ORBITAL_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, epoch);
			stmt.setDouble(2, orbinc);
			stmt.setDouble(3, anode);
			stmt.setDouble(4, perih);
			stmt.setDouble(5, aorq);
			stmt.setDouble(6, ecc);
			stmt.setDouble(7, aorl);
			stmt.setDouble(8, dm);
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_ORBITAL_TARGET_SQL, true);
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
	
	private long addMinorPlanetElementsTarget(Connection connection, XSlaMinorPlanetElements target) throws Exception {
		PreparedStatement stmt = null;
		try {
			double epoch, orbinc, anode, perih, aorq, ecc, aorl, dm;
			epoch = target.getElementEpoch();
			orbinc = target.getOrbitalInc();
			anode = target.getLongAscNode();
			perih = target.getArgPeri();
			aorq = target.getMeanDistance();
			ecc = target.getEccentricity();
			aorl = target.getMeanAnomaly();
			dm = 0; //not a minor planet element
			
			stmt = connection.prepareStatement(INSERT_ORBITAL_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setDouble(1, epoch);
			stmt.setDouble(2, orbinc);
			stmt.setDouble(3, anode);
			stmt.setDouble(4, perih);
			stmt.setDouble(5, aorq);
			stmt.setDouble(6, ecc);
			stmt.setDouble(7, aorl);
			stmt.setDouble(8, dm);
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection,stmt, INSERT_ORBITAL_TARGET_SQL, true);
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
	
	private long addCatalogTarget(Connection connection, XSlaNamedPlanetTarget target) throws Exception {
		PreparedStatement stmt = null;
		try {
			int catIndex;
			
			catIndex = target.getIndex();
			
			stmt = connection.prepareStatement(INSERT_CATALOG_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, catIndex);
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_CATALOG_TARGET_SQL, true);
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
	
	public void deleteTarget(Connection connection, long tid) throws Exception {
		
		PreparedStatement stmt = null;
		try {
			int type, targetRef;
			type = getTargetType(connection, tid);
			targetRef = getTargetRef(connection, tid);
			
			switch(type) {
				case TargetTypes.EXTRA_SOLAR_TARGET:
					deleteExtraSolarTarget(connection, targetRef);
					break;
				case TargetTypes.PLANETOCENTRIC_TARGET:
					deletePlanetocentricTarget(connection, targetRef);
					break;
				case TargetTypes.SELENOGRAPHIC_TARGET:
					deleteSelenographicTarget(connection, targetRef);
					break;
				case TargetTypes.SLA_COMET_ELEMENTS:
					deleteOrbitalElementsTarget(connection, targetRef);
					break;
				case TargetTypes.SLA_MAJOR_PLANET_ELEMENTS:
					deleteOrbitalElementsTarget(connection, targetRef);
					break;
				case TargetTypes.SLA_MINOR_PLANET_ELEMENTS:
					deleteOrbitalElementsTarget(connection, targetRef);
					break;
				case TargetTypes.SLA_CATALOG_TARGET:
					deleteCatalogTarget(connection, targetRef);
					break;
				case TargetTypes.EPHEMERIS_TARGET:
					deleteEphemerisTargets(connection, tid); //NB: uses tid not targetRef
					break;
				default:
					throw new Phase2Exception("unknown target type: " +type );
			}
			
			stmt = connection.prepareStatement(DEL_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_TARGET_SQL, true);
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
	}
	
	public ITarget getTarget(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TARGET_SQL);
			ITarget target = null;
			if (resultSet.next()) {
				target = getTargetFromResultSetCursor(connection, resultSet);
			}
			return target;
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
	
	public int getTargetType(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, tid);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TARGET_SQL);
			int targetType = -1;
			if (resultSet.next()) {
				targetType = getTargetTypeFromResultSetCursor(resultSet);
			}
			return targetType;
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
	
	public int getTargetRef(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TARGET_SQL);
			int targetRef = -1;
			if (resultSet.next()) {
				targetRef = resultSet.getInt(5);
			}
			return targetRef;
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
	
	public ITarget findTarget(Connection connection, long programId, String targetName) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(FIND_TARGET_ID_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, targetName);
			stmt.setLong(2, programId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, FIND_TARGET_ID_SQL);
			if (resultSet.next()) {
				long id = resultSet.getLong(1);
				return getTarget(connection, id);
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
	
	public List listTargets(Connection connection, long progId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(LIST_TARGET_IDS_OF_PROGRAMME_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, progId);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_TARGET_IDS_OF_PROGRAMME_SQL);
			List targetList = new ArrayList();
			while (resultSet.next()) {
				long id = resultSet.getLong(1);
				ITarget target = getTarget(connection, id);
				targetList.add(target);
			}
			return targetList;
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
	 * Updates the target with the specified id, doesn't change the type of the target. 
	 * @param connection The database connection to use for the operation
	 * @param target The new Target
	 * @param keyId The LockingModel key allowing updating
	 * @throws Exception If anything goes wrong during the process
	 */
	public void updateTarget(Connection connection, ITarget target, long keyId) throws Exception {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet resultSet = null;
		try {
			//firstly, get targetRef of underlying target
			long id = target.getID();
			
			//get targetRef of current target data in database
			stmt = connection.prepareStatement(GET_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_TARGET_SQL);
			
			int type;
			long targetRef = -1;
			if (resultSet.next()) {
				type = resultSet.getInt(4);
				targetRef = resultSet.getInt(5);
			}
			
			long newTargetRef = -1;
			if (target instanceof XEphemerisTarget) {
				deleteEphemerisTargets(connection, id);
				addEphemerisTarget(connection, (XEphemerisTarget)target, id);
			} else if (target instanceof XExtraSolarTarget) {
				deleteExtraSolarTarget(connection, targetRef);
				newTargetRef = addExtraSolarTarget(connection, (XExtraSolarTarget)target);
			/*} else if (target instanceof XOrbitalElementsTarget) {
				deleteOrbitalElementsTarget(connection, targetRef);
				XOrbitalElementsTarget xOrbitalElementsTarget = (XOrbitalElementsTarget)target;
				IOrbitalElements orbitalElements = xOrbitalElementsTarget.getElements();
				if (orbitalElements instanceof XSlaCometElements) {
					targetRef = addCometElementsTarget(connection, (XSlaCometElements)target);
				} else if (orbitalElements instanceof XSlaMajorPlanetElements) {
					targetRef = addMajorPlanetElementsTarget(connection, (XSlaMajorPlanetElements)target);
				} else if (orbitalElements instanceof XSlaMinorPlanetElements) {
					targetRef = addMinorPlanetElementsTarget(connection, (XSlaMinorPlanetElements)target);
				} else {
					throw new Phase2Exception("unknown orbital elements target class");
				}*/
			} else if (target instanceof XSlaNamedPlanetTarget) {
				deleteCatalogTarget(connection, targetRef);
				newTargetRef = addCatalogTarget(connection, (XSlaNamedPlanetTarget)target);
			} else {
				throw new Exception("unknown underlying target type");
			}
			
			//update main Target 'name' and 'targetRef' fields
			//id, pid, type all stay the same
			
			stmt2 = connection.prepareStatement(UPDATE_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt2.setString(1, target.getName());
			stmt2.setLong(2, newTargetRef);
			stmt2.setLong(3, id);
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt2, UPDATE_TARGET_SQL, true);
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
			try {
				if (stmt2 != null) {
					stmt2.close();
				}
			} catch (Exception e) {
				logger.error("failed to close PreparedStatement");
			}
		}
	}
	
	/** Private  methods ******************************************************************/

	private ITarget getTargetFromResultSetCursor(Connection connection, ResultSet resultSet) throws Exception {
		
		long id, pid;
		String name;
		int type, targetRef;
		
		id				= resultSet.getLong(1);
		pid			= resultSet.getLong(2);
		name 		= resultSet.getString(3);
		type 			= resultSet.getInt(4);
		targetRef	= resultSet.getInt(5);
		
		XTarget target = (XTarget)getTargetFromFields(connection, id, pid, name, type, targetRef);
		return target;
	}
	
	private ITarget getTargetFromFields(Connection connection, long id, long pid, String name, int type, long targetRef) throws Exception {
		XTarget target;

		switch (type) {
			case TargetTypes.EXTRA_SOLAR_TARGET:
				XExtraSolarTarget xExtraSolarTarget = getExtraSolarTarget(connection, targetRef);
				target = xExtraSolarTarget;
				break;
			//orbital elements target:
			case TargetTypes.SLA_COMET_ELEMENTS:  case TargetTypes.SLA_MAJOR_PLANET_ELEMENTS: case TargetTypes.SLA_MINOR_PLANET_ELEMENTS:
				XOrbitalElementsTarget xOrbitalElementsTarget = getOrbitalElementsTarget(connection, type, targetRef);
				target = xOrbitalElementsTarget;
				break;
			case TargetTypes.SLA_CATALOG_TARGET:
				XSlaNamedPlanetTarget xSlaNamedPlanetTarget = getNamedPlanetTarget(connection, targetRef);
				target = xSlaNamedPlanetTarget;
				break;
			case TargetTypes.EPHEMERIS_TARGET:
				XEphemerisTarget xEphemerisTarget = getEphemerisTarget(connection, id); //NB: uses id, not targetRef
				target = xEphemerisTarget;
				break;
			case TargetTypes.PLANETOCENTRIC_TARGET:
				throw new Phase2Exception("non-implemented target type");
			default:
				throw new Phase2Exception("unknown target type");
		}
		
		target.setID(id);
		target.setName(name);
		
		return target;
	}
	
	private int getTargetTypeFromResultSetCursor(ResultSet resultSet) throws Exception {
		int type;
		type = resultSet.getInt(4);
		return type;
	}
	
	private XExtraSolarTarget getExtraSolarTarget(Connection connection, long targetRef) throws Exception {
		
		PreparedStatement stmt = connection.prepareStatement(GET_EXTRA_SOLAR_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
		
		stmt.setLong(1, targetRef);
		
		ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_EXTRA_SOLAR_TARGET_SQL);
		XExtraSolarTarget target = null;
		if (resultSet.next()) {
			target = getExtraSolarTargetFromResultSetCursor(resultSet);
		}
		return target;	
	}
	
	private XExtraSolarTarget getExtraSolarTargetFromResultSetCursor(ResultSet resultSet) throws Exception {
		double ra, decl, pmra, pmdec, radialVel, parallax, epoch;
		int frame;
		
		ra = 			resultSet.getDouble(1);
		decl =  		resultSet.getDouble(2);
		pmra =  	resultSet.getDouble(3);
		pmdec =  	resultSet.getDouble(4);
		radialVel =	resultSet.getDouble(5);
		parallax =  resultSet.getDouble(6);
		epoch =  	resultSet.getDouble(7);
		frame = 	resultSet.getInt(8);	
		
		XExtraSolarTarget xExtraSolarTarget = new XExtraSolarTarget();
		xExtraSolarTarget.setRa(ra);
		xExtraSolarTarget.setDec(decl);
		xExtraSolarTarget.setPmRA(pmra);
		xExtraSolarTarget.setPmDec(pmdec);
		xExtraSolarTarget.setRadialVelocity(radialVel);
		xExtraSolarTarget.setParallax(parallax);
		xExtraSolarTarget.setEpoch(epoch);
		xExtraSolarTarget.setFrame(frame);
		return xExtraSolarTarget;
	}
	
	private XOrbitalElementsTarget getOrbitalElementsTarget(Connection connection, int targetType, long targetRef)  throws Exception {
		
		PreparedStatement stmt = connection.prepareStatement(GET_ORBITAL_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);

		stmt.setLong(1, targetRef);
		
		ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_ORBITAL_TARGET_SQL);
		XOrbitalElementsTarget target = null;
		if (resultSet.next()) {
			IOrbitalElements orbitalElements =  getOrbitalElementsFromResultSetCursor(targetType, resultSet);
			target = new XOrbitalElementsTarget();
			target.setElements(orbitalElements);
		}
		return target;
	}
	
	private IOrbitalElements getOrbitalElementsFromResultSetCursor(int type, ResultSet resultSet) throws Exception {
		
		double epoch, orbinc, anode, perih, aorq, ecc, aorl, dm;
		epoch	= resultSet.getDouble(1);
		orbinc	= resultSet.getDouble(2);
		anode	= resultSet.getDouble(3);
		perih		= resultSet.getDouble(4);
		aorq		= resultSet.getDouble(5);
		ecc		= resultSet.getDouble(6);
		aorl		= resultSet.getDouble(7);
		dm		= resultSet.getDouble(8);
		
		IOrbitalElements orbitalElements;
		
		switch (type) {
			case TargetTypes.SLA_COMET_ELEMENTS:
				XSlaCometElements slaCometElements = new XSlaCometElements();
				slaCometElements.setElementEpoch(epoch);
				slaCometElements.setOrbitalInc(orbinc);
				slaCometElements.setLongAscNode(anode);
				slaCometElements.setArgPeri(perih);
				slaCometElements.setPeriDist(aorq);
				slaCometElements.setEccentricity(ecc);
				orbitalElements = slaCometElements;
				break;
			case TargetTypes.SLA_MAJOR_PLANET_ELEMENTS:
				XSlaMajorPlanetElements slaMajorPlanetElements = new XSlaMajorPlanetElements();
				slaMajorPlanetElements.setElementEpoch(epoch);
				slaMajorPlanetElements.setOrbitalInc(orbinc);
				slaMajorPlanetElements.setLongAscNode(anode);
				slaMajorPlanetElements.setLongPeri(perih);
				slaMajorPlanetElements.setMeanDistance(aorq);
				slaMajorPlanetElements.setEccentricity(ecc);
				slaMajorPlanetElements.setMeanLongitude(aorl);
				slaMajorPlanetElements.setDailyMotion(dm);
				orbitalElements = slaMajorPlanetElements;
				break;
			case TargetTypes.SLA_MINOR_PLANET_ELEMENTS:
				XSlaMinorPlanetElements slaMinorPlanetElements = new XSlaMinorPlanetElements();
				slaMinorPlanetElements.setElementEpoch(epoch);
				slaMinorPlanetElements.setOrbitalInc(orbinc);
				slaMinorPlanetElements.setLongAscNode(anode);
				slaMinorPlanetElements.setArgPeri(perih);
				slaMinorPlanetElements.setMeanDistance(aorq);
				slaMinorPlanetElements.setEccentricity(ecc);
				slaMinorPlanetElements.setMeanAnomaly(aorl);
				orbitalElements = slaMinorPlanetElements;
				break;
			default:
				throw new Phase2Exception("unknown orbital elements target class");
		}
		
		return orbitalElements;
	}
	
	private XSlaNamedPlanetTarget getNamedPlanetTarget(Connection connection, long targetRef)  throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_CATALOG_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, targetRef);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_CATALOG_TARGET_SQL);
			XSlaNamedPlanetTarget target = null;
			if (resultSet.next()) {
				target = getNamedPlanetTargetFromResultSetCursor(resultSet);
			}
			return target;
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
	
	private XSlaNamedPlanetTarget getNamedPlanetTargetFromResultSetCursor(ResultSet resultSet) throws Exception {
		int catIndex = resultSet.getInt(1);

		XSlaNamedPlanetTarget slaNamedPlanetTarget = new XSlaNamedPlanetTarget();
		slaNamedPlanetTarget.setIndex(catIndex);
		return slaNamedPlanetTarget;
	}
	
	private XSlaNamedPlanetTarget getSelenographicTargetFromResultSetCursor(ResultSet resultSet) throws Exception {
		//TODO not a priority, implement DB table and accessor code
		return null;	
	}
	
	private XEphemerisTarget getEphemerisTarget(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_EPHEMERIS_TARGETS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_EPHEMERIS_TARGETS_SQL);
			XEphemerisTrackNode ephemerisTrackNode = null;
			XEphemerisTarget ephemerisTarget = new XEphemerisTarget();
			
			while (resultSet.next()) {
				ephemerisTrackNode = getEphemerisTrackNodeFromResultSetCursor(resultSet);
				ephemerisTarget.addTrackNode(ephemerisTrackNode);
			}
			return ephemerisTarget;
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
	
	private XEphemerisTrackNode getEphemerisTrackNodeFromResultSetCursor(ResultSet resultSet) throws Exception {
		long id, time;
		double ra, decl, raDot, decDot;
		
		id 			= resultSet.getLong(1);
		time 		= resultSet.getLong(2);
		ra			= resultSet.getDouble(3);
		decl		= resultSet.getDouble(4);
		raDot		= resultSet.getDouble(5);
		decDot	= resultSet.getDouble(6);
		XEphemerisTrackNode ephemerisTrackNode = new XEphemerisTrackNode(time, ra, decl, raDot, decDot);
		return ephemerisTrackNode;
	}
	
	private void deleteEphemerisTargets(Connection connection, long tid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_EPHEMERIS_TARGETS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, tid);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_EPHEMERIS_TARGETS_SQL, true);
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
	
	private void deletePlanetocentricTarget(Connection connection, long targetRef) throws Exception {
		//TODO not a priority, implement DB table and accessor code
	}
	
	private void deleteExtraSolarTarget(Connection connection, long targetRef) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_EXTRA_SOLAR_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, targetRef);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_EXTRA_SOLAR_TARGET_SQL, true);
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
	}
	
	private void deleteSelenographicTarget(Connection connection, long targetRef) throws Exception {
		//TODO not a priority, implement DB table and accessor code
	}
	
	private void deleteOrbitalElementsTarget(Connection connection, long targetRef) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_ORBITAL_ELEMENTS_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, targetRef);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_ORBITAL_ELEMENTS_TARGET_SQL, true);
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
	}
	
	private void deleteCatalogTarget(Connection connection, long targetRef) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_CATALOG_TARGET_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, targetRef);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_CATALOG_TARGET_SQL, true);
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
	}
	
	/**
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private long getProposalIdFromResultSetCursor(ResultSet resultSet) throws Exception {
		long pid = resultSet.getInt(1);
		return pid;
	}
}