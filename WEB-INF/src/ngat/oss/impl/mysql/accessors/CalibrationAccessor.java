package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ngat.oss.exception.Phase2Exception;
import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.CalibrationTypes;
import ngat.phase2.IApertureConfig;
import ngat.phase2.ICalibration;
import ngat.phase2.XApertureConfig;
import ngat.phase2.XArc;
import ngat.phase2.XBias;
import ngat.phase2.XDark;
import ngat.phase2.XLampDef;
import ngat.phase2.XLampFlat;

import org.apache.log4j.Logger;

public class CalibrationAccessor  {
	
	static Logger logger = Logger.getLogger(CalibrationAccessor.class);
	
	/*
	 EA_CALIBRATION;
	 	id           			int
	 	name         		String
	 	type         			int one of CalibrationTypes.ARC | BIAS | DARK | LAMP
	 	exposureTime 	double  - relates to XDark
	 	lampName     	String - relates to XLampFlat and XArc
	*/

	//statements
	public static final String GET_CALIBRATION_SQL = 							
		"select " +
		"name, type, exposureTime, lampName " +
		"from " +
		"EA_CALIBRATION " +
		"where id=?";
	
	public static final String INSERT_CALIBRATION_SQL = 						
		"insert into EA_CALIBRATION (" +
		"name, type, exposureTime, lampName " +
		") values (" + 
		"?, ?, ?, ?)";
	
	/** Public methods *******************************************************************/
	
	public ICalibration getCalibration(Connection connection, long id) throws Exception {
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_CALIBRATION_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, id);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_CALIBRATION_SQL);
			
			String name;
			int type;
			double exposureTime;
			String lampName;
	
			ICalibration calibration;
			
			if (resultSet.next()) {
				name =  resultSet.getString(1);
				type =  resultSet.getInt(2);
				exposureTime =  resultSet.getDouble(3);
				lampName =  resultSet.getString(4);
				
				switch (type) {
					case CalibrationTypes.ARC:
						XArc arc = new XArc();
						arc.setID(id);
						arc.setName(name);
						arc.setLamp(new XLampDef(lampName));
						return arc;
					case CalibrationTypes.BIAS:
						XBias bias = new XBias();
						bias.setID(id);
						bias.setName(name);
						return bias;
					case CalibrationTypes.DARK:
						XDark dark = new XDark();
						dark.setID(id);
						dark.setName(name);
						dark.setExposureTime(exposureTime);
						return dark;
					case CalibrationTypes.LAMP_FLAT:
						XLampFlat lampFlat = new XLampFlat();
						lampFlat.setID(id);
						lampFlat.setName(name);
						lampFlat.setLamp(new XLampDef(lampName));
						return lampFlat;
				}
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
	
	public long insertCalibration(Connection connection, ICalibration calibration) throws Exception {
		PreparedStatement stmt = null;
		try {
			String name = "";
			int calibrationType = -1;
			double exposureTime = 0;
			String lampName = "";
			
			if (calibration instanceof XArc) {
				XArc arc =(XArc)calibration; 
				name = arc.getName();
				calibrationType = CalibrationTypes.ARC;
				exposureTime = 0;
				lampName = arc.getLamp().getLampName();
				
			} else if (calibration instanceof XBias) {
				XBias bias =(XBias)calibration; 
				name = bias.getName();
				calibrationType = CalibrationTypes.BIAS;
				exposureTime = 0;
				lampName = "";
				
			} else if (calibration instanceof XDark) {
				XDark dark =(XDark)calibration; 
				name = dark.getName();
				calibrationType = CalibrationTypes.DARK;
				exposureTime = dark.getExposureTime();
				lampName = "";
				
			} else if (calibration instanceof XLampFlat) {
				XLampFlat lampFlat =(XLampFlat)calibration; 
				name = lampFlat.getName();
				calibrationType = CalibrationTypes.LAMP_FLAT;
				exposureTime = 0;
				lampName = lampFlat.getLamp().getLampName();
			} else {
				throw new Phase2Exception("unknown calibration type: " + calibration);
			}
			
			stmt = connection.prepareStatement(INSERT_CALIBRATION_SQL, Statement.RETURN_GENERATED_KEYS);
	
			stmt.setString(1, name);
			stmt.setInt(2, calibrationType);
			stmt.setDouble(3, exposureTime);
			stmt.setString(4, lampName);
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_CALIBRATION_SQL, true);
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
