package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.ConnectionPool;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.InstrumentConfigTypes;
import ngat.phase2.IDetectorConfig;
import ngat.phase2.IInstrumentConfig;
import ngat.phase2.XBlueTwoSlitSpectrographInstrumentConfig;
import ngat.phase2.XDualBeamSpectrographInstrumentConfig;
import ngat.phase2.XFilterDef;
import ngat.phase2.XFilterSpec;
import ngat.phase2.XImagerInstrumentConfig;
import ngat.phase2.XInstrumentConfig;
import ngat.phase2.XMoptopInstrumentConfig;
import ngat.phase2.XPolarimeterInstrumentConfig;
import ngat.phase2.XImagingSpectrographInstrumentConfig;
import ngat.phase2.XSpectrographInstrumentConfig;
import ngat.phase2.XTipTiltImagerInstrumentConfig;

import org.apache.log4j.Logger;

public class InstrumentConfigAccessor {
	
	static Logger logger = Logger.getLogger(InstrumentConfigAccessor.class);
	
	 /** Delimiter used to create filter descriptions by concatenating their names together.*/
    public static final String FILTER_DELIMITER = ":";
	
	/*
	 INSTRUMENT_CONFIG
		 id							int
		 pid						int
		 dcid						int
		 iConfigType			int
		 iConfigId				int
		 name					String
		 instrClassName	String
		 calibrateBefore		int  
		 calibrateAfter		int
	 */
	
	//statements
	public static final String INSERT_INST_CONFIG_SQL = 						
		"insert into INSTRUMENT_CONFIG (" +
		"pid, dcid, iConfigType, iConfigId, name, instrClassName" +
		") values (" + 
		"?, ?, ?, ?, ?, ?)";

	public static final String GET_INST_CONFIG_SQL = 							
		"select " +
		"id, pid, dcid, iConfigType, iConfigId, name, instrClassName " +
		"from " +
		"INSTRUMENT_CONFIG " +
		"where id=?";
	
	public static final String LIST_INST_CONFIGS_OF_PROG_SQL = 							
		"select " +
		"id, pid, dcid, iConfigType, iConfigId, name, instrClassName " +
		"from " + 
		"INSTRUMENT_CONFIG " + 
		"where pid=? " +
		"order by name";
	
	public static final String LIST_INST_CONFIG_OBS_SQL_D = 	
		"select " +
		"dcid, iConfigType, iConfigId, name, instrClassName " +
		"from " + 
		"INSTRUMENT_CONFIG " + 
		"where dcid=? " +
		"order by name";
	
	public static final String DEL_INST_CONFIG_SQL = 							
		"delete from INSTRUMENT_CONFIG where id = ?";
	
	public static final String UPDATE_INST_CONFIG_SQL =						
		"update INSTRUMENT_CONFIG "+
		"set " + 
		"dcid=?," +
		"iConfigType=?," +
		"iConfigId=?," +
		"name=?," +
		"instrClassName=? "+
		"where id=?";
		
    public static final String INSERT_INST_CONFIG_CCD_SQL =						
	"insert into INST_CONFIG_CCD ("+
	"filterType) values (" + 
	"?)";
    
    public static final String INSERT_INST_CONFIG_FRODO_SQL =
	"insert into INST_CONFIG_FRODO_SPEC ("+
	"resolution) values (" + 
	"?)";
	
	public static final String INSERT_INST_CONFIG_POLARIMETER_RINGO3_SQL =						
			"insert into INST_CONFIG_POLARIMETER ("+
			"gain) values (" + 
			"?)";
	
	public static final String INSERT_INST_CONFIG_MOPTOP_SQL =						
			"insert into INST_CONFIG_MOPTOP ("+
			"dichroicState) values (" + 
			"?)";

	public static final String INSERT_INST_CONFIG_IMAGING_SPECTROGRAPH_SQL =						
		"insert into INST_CONFIG_SPEC_IMAGER (grismPos, grismRot, slitPos) values (?, ?, ?)";

	public static final String INSERT_INST_CONFIG_BLUE_TWO_SLIT_SPECTROGRAPH_SQL =						
		"insert into INST_CONFIG_SPEC_TWO_SLIT (slitWidth) values (?)";
	
	public static final String INSERT_INST_CONFIG_TIP_TILT_SQL =						
		"insert into INST_CONFIG_TIP_TILT ("+
		"gain) values (" + 
		"?)";
	
	public static final String GET_INST_CONFIG_CCD_SQL = 
		"select filterType " +
		"from " +
		"INST_CONFIG_CCD " +
		"where id=?";
	
	public static final String GET_INST_CONFIG_IMAGING_SPECTROGRAPH_SQL = 
		"select grismPos, grismRot, slitPos  " +
		"from " +
		"INST_CONFIG_SPEC_IMAGER " +
		"where id=?";
	
	public static final String GET_INST_CONFIG_TWO_SLIT_SPECTROGRAPH_SQL = 
		"select slitWidth  " +
		"from " +
		"INST_CONFIG_SPEC_TWO_SLIT " +
		"where id=?";
	
	public static final String GET_INST_CONFIG_POLARIMETER_RINGO3_SQL = 
		"select gain " +
		"from " +
		"INST_CONFIG_POLARIMETER " +
		"where id=?";
	
	public static final String GET_INST_CONFIG_MOPTOP_SQL = 
			"select dichroicState " +
			"from " +
			"INST_CONFIG_MOPTOP " +
			"where id=?";
		
	public static final String GET_INST_CONFIG_FRODO_SQL = 
		"select resolution " +
		"from " +
		"INST_CONFIG_FRODO_SPEC " +
		"where id=?";
	
	public static final String GET_INST_CONFIG_TIP_TILT_SQL = 
		"select gain " +
		"from " +
		"INST_CONFIG_TIP_TILT " +
		"where id=?";

	
	/** Public methods ******************************************************************
	 * @throws Phase2Exception */ 
	
	public long addInstrumentConfig(Connection connection, long progId, IInstrumentConfig instConfig) throws Exception { 
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			DetectorConfigAccessor detectorConfigAccessor = new DetectorConfigAccessor();
			long dcid = detectorConfigAccessor.addDetectorConfig(connection, instConfig.getDetectorConfig());
			
			int iConfigType;
			long iConfigId;
			
			if (instConfig instanceof XImagerInstrumentConfig) {
				//sub type of XImagerInstrumentConfig
				if (instConfig instanceof XTipTiltImagerInstrumentConfig) {
					iConfigType = InstrumentConfigTypes.TIP_TILT;
					XTipTiltImagerInstrumentConfig tiptiltInstrumentConfig = (XTipTiltImagerInstrumentConfig)instConfig;
					iConfigId = insertInstConfigTipTilt(connection, tiptiltInstrumentConfig);
				} else {
					iConfigType = InstrumentConfigTypes.CCD;
					XImagerInstrumentConfig imagerInstrumentConfig = (XImagerInstrumentConfig)instConfig;
					iConfigId = insertInstConfigCCD(connection, imagerInstrumentConfig);
				}
			} else if (instConfig instanceof XImagingSpectrographInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.IMAGING_SPECTROGRAPH;
				XImagingSpectrographInstrumentConfig imagingSpectrographInstrumentConfig = (XImagingSpectrographInstrumentConfig)instConfig;
				iConfigId = insertInstConfigImagingSpectrograph(connection, imagingSpectrographInstrumentConfig);
			} else if (instConfig instanceof XBlueTwoSlitSpectrographInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.TWO_SLIT_SPECTROGRAPH;
				XBlueTwoSlitSpectrographInstrumentConfig imagingSpectrographInstrumentConfig = (XBlueTwoSlitSpectrographInstrumentConfig)instConfig;
				iConfigId = insertInstConfigBlueTwoSlitSpectrograph(connection, imagingSpectrographInstrumentConfig);
			} else if (instConfig instanceof XDualBeamSpectrographInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.FRODO;
				XDualBeamSpectrographInstrumentConfig dualBeamSpectrographInstrumentConfig = (XDualBeamSpectrographInstrumentConfig)instConfig;
				iConfigId = insertInstConfigFrodo(connection, dualBeamSpectrographInstrumentConfig);
			} else if (instConfig instanceof XPolarimeterInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.POLAR;
				XPolarimeterInstrumentConfig polarimeterInstrumentConfig = (XPolarimeterInstrumentConfig)instConfig;
				iConfigId = insertInstConfigPolar(connection, polarimeterInstrumentConfig); 
			} else if (instConfig instanceof XMoptopInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.MOPTOP;
				XMoptopInstrumentConfig moptopInstrumentConfig = (XMoptopInstrumentConfig)instConfig;
				iConfigId = insertInstConfigMoptop(connection, moptopInstrumentConfig); 
			} else {
				throw new Phase2Exception("unknown instrument config type: " +(instConfig != null ? instConfig.getClass().getName() : "null") );
			}
			
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, progId);
			stmt.setLong(2, dcid);
			stmt.setLong(3, iConfigType);
			stmt.setLong(4, iConfigId);
			stmt.setString(5, instConfig.getName());
			stmt.setString(6, instConfig.getInstrumentName());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_SQL, true);
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

	public void deleteInstrumentConfig(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DEL_INST_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			
			//execute query
			int numRows = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, DEL_INST_CONFIG_SQL, true);
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
	 * Updates the InstrumentConfig with the specified key. 
	 * @param connection The database connection to use for the operation
	 * @param instConfig The new InstrumentConfig
	 * @param keyId The LockingModel key allowing updating
	 * @throws Exception If anything goes wrong during the process
	 */
	public void updateInstrumentConfig(Connection connection, IInstrumentConfig instConfig, long keyId) throws Exception {
		PreparedStatement stmt = null;
		try {
			//delete old detectorConfig reference
			long oldDcid = instConfig.getDetectorConfig().getID();
			DetectorConfigAccessor detectorConfigAccessor = new DetectorConfigAccessor();
			detectorConfigAccessor.deleteDetectorConfig(connection, oldDcid);
			
			//add new instConfig
			long dcid = detectorConfigAccessor.addDetectorConfig(connection, instConfig.getDetectorConfig());
			
			int iConfigType;
			long iConfigId;
			
			if (instConfig instanceof XImagerInstrumentConfig) {
				//sub type
				if (instConfig instanceof XTipTiltImagerInstrumentConfig) {
					iConfigType = InstrumentConfigTypes.TIP_TILT;
					XTipTiltImagerInstrumentConfig tipTiltImagerInstrumentConfig = (XTipTiltImagerInstrumentConfig)instConfig;
					iConfigId = insertInstConfigTipTilt(connection, tipTiltImagerInstrumentConfig);
				} else {
					iConfigType = InstrumentConfigTypes.CCD;
					XImagerInstrumentConfig imagerInstrumentConfig = (XImagerInstrumentConfig)instConfig;
					iConfigId = insertInstConfigCCD(connection, imagerInstrumentConfig);
				}
			} else if (instConfig instanceof XImagingSpectrographInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.IMAGING_SPECTROGRAPH;
				XImagingSpectrographInstrumentConfig imagingSpectrographInstrumentConfig = (XImagingSpectrographInstrumentConfig)instConfig;
				iConfigId = insertInstConfigImagingSpectrograph(connection, imagingSpectrographInstrumentConfig);
			} else if (instConfig instanceof XBlueTwoSlitSpectrographInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.TWO_SLIT_SPECTROGRAPH;
				XBlueTwoSlitSpectrographInstrumentConfig blueTwoSlitSpectrographInstrumentConfig = (XBlueTwoSlitSpectrographInstrumentConfig)instConfig;
				iConfigId = insertInstConfigBlueTwoSlitSpectrograph(connection, blueTwoSlitSpectrographInstrumentConfig);
			} else if (instConfig instanceof XDualBeamSpectrographInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.FRODO;
				XDualBeamSpectrographInstrumentConfig dualBeamSpectrographInstrumentConfig = (XDualBeamSpectrographInstrumentConfig)instConfig;
				iConfigId = insertInstConfigFrodo(connection, dualBeamSpectrographInstrumentConfig);
			} else if (instConfig instanceof XPolarimeterInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.POLAR;
				XPolarimeterInstrumentConfig polarimeterInstrumentConfig = (XPolarimeterInstrumentConfig)instConfig;
				iConfigId = insertInstConfigPolar(connection, polarimeterInstrumentConfig);
			} else if (instConfig instanceof XMoptopInstrumentConfig) {
				iConfigType = InstrumentConfigTypes.MOPTOP;
				XMoptopInstrumentConfig moptopInstrumentConfig = (XMoptopInstrumentConfig)instConfig;
				iConfigId = insertInstConfigMoptop(connection, moptopInstrumentConfig);
			} else {
				throw new Phase2Exception("unknown instrument config type: " +(instConfig != null ? instConfig.getClass().getName() : "null") );
			}
			
			stmt = connection.prepareStatement(UPDATE_INST_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, dcid);
			stmt.setLong(2, iConfigType);
			stmt.setLong(3, iConfigId);
			stmt.setString(4, instConfig.getName());
			stmt.setString(5, instConfig.getInstrumentName());
			stmt.setLong(6, instConfig.getID());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, UPDATE_INST_CONFIG_SQL, true);
			ConnectionPool.getInstance().surrenderConnection(connection);
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

	public IInstrumentConfig getInstrumentConfig(Connection connection, long cid) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, cid);
			
			IInstrumentConfig instrumentConfig = null;
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_SQL);
			if (resultSet.next()) {
				instrumentConfig = getInstrumentConfigFromResultSetCursor(connection, resultSet);
			}
			return instrumentConfig;	
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

	public List listInstrumentConfigs(Connection connection, long progId) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(LIST_INST_CONFIGS_OF_PROG_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setLong(1, progId);
			
			ResultSet resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, LIST_INST_CONFIGS_OF_PROG_SQL);
			List configList = new ArrayList();
			while (resultSet.next()) {
				IInstrumentConfig config = getInstrumentConfigFromResultSetCursor(connection, resultSet);
				configList.add(config);
			}
			return configList;
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
	
	
	/* Private  methods ******************************************************************/

	/**  
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 * @throws Phase2Exception 
	 */
	private IInstrumentConfig getInstrumentConfigFromResultSetCursor(Connection connection, ResultSet resultSet) throws Exception {
		
		long id, pid, dcid, iConfigId;
		int iConfigType;
		String name, instrClassName;
	
		id					= resultSet.getLong(1);
		pid					= resultSet.getLong(2);
		dcid				= resultSet.getLong(3);
		iConfigType			= resultSet.getInt(4);
		iConfigId			= resultSet.getLong(5);
		name				= resultSet.getString(6);
		instrClassName		= resultSet.getString(7);

		XInstrumentConfig instrumentConfig = (XInstrumentConfig)getInstrumentConfigFromFields(connection, id, dcid, iConfigType, iConfigId, name, instrClassName);
		
		return instrumentConfig;
	}
	
	private IInstrumentConfig getInstrumentConfigFromFields(Connection connection, long id, long dcid, int iConfigType, long iConfigId, String name, String instrClassName) throws Exception {
		
		XInstrumentConfig instrumentConfig;
		
		if (iConfigType == InstrumentConfigTypes.CCD) {
			XImagerInstrumentConfig imagerInstrumentConfig = getImagerInstrumentConfig(connection, iConfigId);
			if (imagerInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)imagerInstrumentConfig;
		} else if (iConfigType == InstrumentConfigTypes.IMAGING_SPECTROGRAPH) {
			XImagingSpectrographInstrumentConfig imagingSpectrographInstrumentConfig =  getImagingSpectrographInstrumentConfig(connection, iConfigId);
			if (imagingSpectrographInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)imagingSpectrographInstrumentConfig;
		} else if (iConfigType == InstrumentConfigTypes.TWO_SLIT_SPECTROGRAPH) {
			XBlueTwoSlitSpectrographInstrumentConfig blueTwoSlitSpectrographInstrumentConfig =  getTwoSlitSpectrographInstrumentConfig(connection, iConfigId);
			if (blueTwoSlitSpectrographInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)blueTwoSlitSpectrographInstrumentConfig;
		} else if (iConfigType == InstrumentConfigTypes.FRODO) {
			XDualBeamSpectrographInstrumentConfig dualBeamSpectrographInstrumentConfig = getDualBeamSpectrographInstrumentConfig(connection, iConfigId);
			if (dualBeamSpectrographInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)dualBeamSpectrographInstrumentConfig;
		} else if (iConfigType == InstrumentConfigTypes.POLAR) {
			XPolarimeterInstrumentConfig polarimeterInstrumentConfig = getPolarimeterInstrumentConfig(connection, iConfigId);
			if (polarimeterInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)polarimeterInstrumentConfig;
		} else if (iConfigType == InstrumentConfigTypes.MOPTOP) {
			XMoptopInstrumentConfig moptopInstrumentConfig = getMoptopInstrumentConfig(connection, iConfigId);
			if (moptopInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)moptopInstrumentConfig;
	    } else if (iConfigType == InstrumentConfigTypes.TIP_TILT) {
			XTipTiltImagerInstrumentConfig tipTiltImagerInstrumentConfig = getTipTiltImagerInstrumentConfig(connection, iConfigId);
			if (tipTiltImagerInstrumentConfig == null) {
				return null;
			}
			instrumentConfig = (XInstrumentConfig)tipTiltImagerInstrumentConfig;
		} else {
			throw new Phase2Exception("unknown instrument config type: " +iConfigType);
		}
		
		instrumentConfig.setID(id);
		instrumentConfig.setName(name);
		instrumentConfig.setInstrumentName(instrClassName);
		
		DetectorConfigAccessor detectorConfigAccessor = new DetectorConfigAccessor();
		IDetectorConfig detectorConfig = detectorConfigAccessor.getDetectorConfig(connection, dcid);
		instrumentConfig.setDetectorConfig(detectorConfig);
		
		return instrumentConfig;
	}
	
	private XImagerInstrumentConfig getImagerInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_CCD_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_CCD_SQL);
			XImagerInstrumentConfig instrumentConfig = new XImagerInstrumentConfig();
			if (resultSet.next()) {
				XFilterSpec filterSpec = new XFilterSpec();
				String filterString = resultSet.getString(1);
				
				StringTokenizer st = new StringTokenizer(filterString, XFilterSpec.DELIMETER);
			     while (st.hasMoreTokens()) {
					 String filter = st.nextToken();
					 filterSpec.addFilter(new XFilterDef(filter));
			     }
				
				try {
					instrumentConfig.setFilterSpec(filterSpec);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return instrumentConfig;
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
	
	private XImagingSpectrographInstrumentConfig getImagingSpectrographInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_IMAGING_SPECTROGRAPH_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_IMAGING_SPECTROGRAPH_SQL);
			if (resultSet == null) { 
				return null; 
			}
			
			XImagingSpectrographInstrumentConfig imagingSpectrographInstrumentConfig = null;
			if (resultSet.next()) {
				//int grismPos, grismRot, slitPos
				int grismPos = resultSet.getInt(1);
				int grismRot = resultSet.getInt(2);
				int slitPos = resultSet.getInt(3);
				
				imagingSpectrographInstrumentConfig = new XImagingSpectrographInstrumentConfig();
				imagingSpectrographInstrumentConfig.setGrismPosition(grismPos);
				imagingSpectrographInstrumentConfig.setGrismRotation(grismRot);
				imagingSpectrographInstrumentConfig.setSlitPosition(slitPos);
			}
			return imagingSpectrographInstrumentConfig;
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
	
	private XBlueTwoSlitSpectrographInstrumentConfig getTwoSlitSpectrographInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_TWO_SLIT_SPECTROGRAPH_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_TWO_SLIT_SPECTROGRAPH_SQL);
			if (resultSet == null) { 
				return null; 
			}
			
			XBlueTwoSlitSpectrographInstrumentConfig blueTwoSlitSpectrographInstrumentConfig = null;
			if (resultSet.next()) {
				int slitWidth = resultSet.getInt(1);
				
				blueTwoSlitSpectrographInstrumentConfig = new XBlueTwoSlitSpectrographInstrumentConfig();
				blueTwoSlitSpectrographInstrumentConfig.setSlitWidth(slitWidth);
			}
			return blueTwoSlitSpectrographInstrumentConfig;
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

	private XDualBeamSpectrographInstrumentConfig getDualBeamSpectrographInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_FRODO_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_FRODO_SQL);
			
			if (resultSet == null) { 
				return null; 
			}
			
			XDualBeamSpectrographInstrumentConfig dualBeamSpectrographInstrumentConfig = null;
			if (resultSet.next()) {
				int resolution = resultSet.getInt(1);
				
				dualBeamSpectrographInstrumentConfig = new XDualBeamSpectrographInstrumentConfig();
				dualBeamSpectrographInstrumentConfig.setResolution(resolution);
			}
			return dualBeamSpectrographInstrumentConfig;
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
	
	private XPolarimeterInstrumentConfig getPolarimeterInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_POLARIMETER_RINGO3_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_POLARIMETER_RINGO3_SQL);
			
			if (resultSet == null) { 
				return null; 
			}
			
			XPolarimeterInstrumentConfig polarimeterInstrumentConfig = null;
			if (resultSet.next()) {
				int gain = resultSet.getInt(1);
				polarimeterInstrumentConfig = new XPolarimeterInstrumentConfig();
				polarimeterInstrumentConfig.setGain(gain);
			}
			return polarimeterInstrumentConfig;
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
	
	private XMoptopInstrumentConfig getMoptopInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_MOPTOP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_MOPTOP_SQL);
			
			if (resultSet == null) { 
				return null; 
			}
			
			XMoptopInstrumentConfig moptopInstrumentConfig = null;
			if (resultSet.next()) {
				int dichroicState = resultSet.getInt(1);
				moptopInstrumentConfig = new XMoptopInstrumentConfig();
				moptopInstrumentConfig.setDichroicState(dichroicState);
			}
			return moptopInstrumentConfig;
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

	private XTipTiltImagerInstrumentConfig getTipTiltImagerInstrumentConfig(Connection connection, long iConfigId) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(GET_INST_CONFIG_TIP_TILT_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, iConfigId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_INST_CONFIG_TIP_TILT_SQL);
			
			if (resultSet == null) { 
				return null; 
			}
			
			XTipTiltImagerInstrumentConfig tipTiltImagerInstrumentConfig = new XTipTiltImagerInstrumentConfig();
			if (resultSet.next()) {
				int gain = resultSet.getInt(1);
				try {
					tipTiltImagerInstrumentConfig.setGain(gain);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return tipTiltImagerInstrumentConfig;
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
	
	private long insertInstConfigCCD(Connection connection, XImagerInstrumentConfig imagerInstrumentConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_CCD_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, imagerInstrumentConfig.getFilterSpec().getFiltersString());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_CCD_SQL, true);
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

	private long insertInstConfigImagingSpectrograph(Connection connection, XImagingSpectrographInstrumentConfig imagingSpectrographInstrumentConfig) throws Exception {
		
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_IMAGING_SPECTROGRAPH_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, imagingSpectrographInstrumentConfig.getGrismPosition());
			stmt.setInt(2, imagingSpectrographInstrumentConfig.getGrismRotation());
			stmt.setInt(3, imagingSpectrographInstrumentConfig.getSlitPosition());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_IMAGING_SPECTROGRAPH_SQL, true);
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
	
	private long insertInstConfigBlueTwoSlitSpectrograph(Connection connection, XBlueTwoSlitSpectrographInstrumentConfig twoSlitSpectrographInstrumentConfig) throws Exception {
		
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_BLUE_TWO_SLIT_SPECTROGRAPH_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, twoSlitSpectrographInstrumentConfig.getSlitWidth());

			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_BLUE_TWO_SLIT_SPECTROGRAPH_SQL, true);
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
	
	private long insertInstConfigFrodo(Connection connection, XDualBeamSpectrographInstrumentConfig dualBeamSpectrographInstrumentConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_FRODO_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, dualBeamSpectrographInstrumentConfig.getResolution());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_FRODO_SQL, true);
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
	
	private long insertInstConfigPolar(Connection connection, XPolarimeterInstrumentConfig polarimeterInstrumentConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_POLARIMETER_RINGO3_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, polarimeterInstrumentConfig.getGain());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_POLARIMETER_RINGO3_SQL, true);
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
	
	private long insertInstConfigMoptop(Connection connection, XMoptopInstrumentConfig moptopInstrumentConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_MOPTOP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, moptopInstrumentConfig.getDichroicState());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_MOPTOP_SQL, true);
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
	
	private long insertInstConfigTipTilt(Connection connection, XTipTiltImagerInstrumentConfig tipTiltImagerInstrumentConfig) throws Exception {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_INST_CONFIG_TIP_TILT_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, tipTiltImagerInstrumentConfig.getGain());
			
			//execute query
			long id = DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_INST_CONFIG_TIP_TILT_SQL, true);
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
	
}
