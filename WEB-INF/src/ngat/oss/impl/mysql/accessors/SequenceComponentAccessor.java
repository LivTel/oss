package ngat.oss.impl.mysql.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ngat.oss.impl.mysql.DatabaseTransactor;
import ngat.oss.impl.mysql.reference.ConditionTypes;
import ngat.oss.impl.mysql.reference.ExecutiveActionElementTypes;
import ngat.oss.impl.mysql.reference.SequenceElementTypes;
import ngat.phase2.IAcquisitionConfig;
import ngat.phase2.IApertureConfig;
import ngat.phase2.IAutoguiderConfig;
import ngat.phase2.IBeamSteeringConfig;
import ngat.phase2.ICalibration;
import ngat.phase2.IExecutiveAction;
import ngat.phase2.IExposure;
import ngat.phase2.IFocusControl;
import ngat.phase2.IFocusOffset;
import ngat.phase2.IInstrumentConfig;
import ngat.phase2.IInstrumentConfigSelector;
import ngat.phase2.IMosaicOffset;
import ngat.phase2.IOpticalSlideConfig;
import ngat.phase2.IRotatorConfig;
import ngat.phase2.ISequenceComponent;
import ngat.phase2.ISlew;
import ngat.phase2.ITarget;
import ngat.phase2.ITargetSelector;
import ngat.phase2.ITipTiltAbsoluteOffset;
import ngat.phase2.XBranchComponent;
import ngat.phase2.XExecutiveComponent;
import ngat.phase2.XInstrumentConfigSelector;
import ngat.phase2.XIteratorComponent;
import ngat.phase2.XIteratorRepeatCountCondition;
import ngat.phase2.XTargetSelector;

import org.apache.log4j.Logger;

public class SequenceComponentAccessor {

	static Logger logger = Logger.getLogger(SequenceComponentAccessor.class);
	
	public static final String INSERT_OBS_SEQUENCE_SQL = 						
		"insert into SEQUENCE_COMPONENT (" +
		"gid, parent, type, condType, condVal, eaType, eaRef, name " +
		") values (" + 
		"?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static final String GET_OBS_SEQUENCE_SQL = 							
		"select " +
		"gid, parent, type, condType, condVal, eaType, eaRef, name " +
		"from " +
		"SEQUENCE_COMPONENT " +
		"where id=? ";
	
	public static final String GET_CHILD_OBS_IDS_SQL = 							
		"select " +
		"id " +
		"from " +
		"SEQUENCE_COMPONENT " +
		"where parent=? ";
	
	public static final String GET_OBS_SEQUENCE_ID_OF_GROUP_SQL = 							
		"select " +
		"osid " +
		"from " +
		"OBSERVATION_GROUP " +
		"where id=? ";
	
	public static final String SET_OBS_SEQUENCE_ID_OF_GROUP_SQL = 							
		"update OBSERVATION_GROUP "+
		"set " + 
		"osid=? " +
		"where id=?";
	
	public static final String DELETE_OBSERVATION_SEQUENCE_OF_GROUP_SQL = 							
		"delete from SEQUENCE_COMPONENT where gid= ?";
	
	public static final String CLEAR_OBSERVATION_SEQUENCE_OF_GROUP_SQL = 							
		"update OBSERVATION_GROUP " +
		"set osid = 0 " + 
		"where id= ?";
	
	public ISequenceComponent getObservationSequenceOfGroup(Connection connection, long groupID) throws Exception  {
		
		logger.info("getObservationSequenceOfGroup(" + groupID + ")");

		long osid = getObservationSequenceIDOfGroup(connection, groupID);
		if (osid == 0) {
			return null;
		}
		return getObservationSequenceComponent(connection, osid);
	}
	
	public boolean groupHasObservationSequence(Connection connection, long groupID) throws Exception  {
		long osid = getObservationSequenceIDOfGroup(connection, groupID);
		return (osid != 0);
	}
	
	public void setObservationSequenceIDOfGroup(Connection connection, long groupID, long obsSeqID) throws Exception {
		PreparedStatement stmt = null;
		try{
			stmt = connection.prepareStatement(SET_OBS_SEQUENCE_ID_OF_GROUP_SQL, Statement.RETURN_GENERATED_KEYS);
			
			stmt.setLong(1, obsSeqID);
			stmt.setLong(2, groupID);
			
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, SET_OBS_SEQUENCE_ID_OF_GROUP_SQL, false);
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
	
	public void updateObservationSequenceOfGroup(Connection connection, long groupID, ISequenceComponent sequence, long keyID) throws Exception  {
		
		if (groupHasObservationSequence(connection, groupID)) {
			deleteObservationSequenceOfGroup(connection, groupID);
			long newObsSeqID = addObservationSequence(connection, groupID, sequence);
			setObservationSequenceIDOfGroup(connection, groupID, newObsSeqID);
		} else {
			throw new Exception("updateObservationSequenceOfGroup failed, group " +groupID + " doesn't have an observation sequence");
		}
	}
	
	public ISequenceComponent getObservationSequenceComponent(Connection connection, long osid) throws Exception {
		logger.info("getObservationSequenceComponent(" + osid + ")");
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_OBS_SEQUENCE_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, osid);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_OBS_SEQUENCE_SQL);
			
			long gid = -1;
			long parent = -1;
			int type = -1;
			int condType = -1;
			int condVal = -1;
			int eaType = -1;
			int eaRef = -1;
			String name = "UNKNOWN";
			
			if (resultSet.next()) {
				gid = resultSet.getLong(1);
				parent = resultSet.getLong(2);
				type	= resultSet.getInt(3);
				condType = resultSet.getInt(4);
				condVal = resultSet.getInt(5);
				eaType = resultSet.getInt(6);
				eaRef = resultSet.getInt(7);
				name = resultSet.getString(8);
			} else {
				throw new Exception("unable to locate observation sequence with id: " + osid);
			}
			
			switch (type) {
				case SequenceElementTypes.TYPE_ITERATOR: {
					logger.info("... type = ITERATOR"); 
					XIteratorComponent iteratorSequenceComponent; 
					
					switch (condType) {
						case ConditionTypes.REPEAT_COUNT_CONDITION: {
							logger.info("... ... condType = REPEAT_COUNT_CONDITION"); 
							XIteratorRepeatCountCondition iteratorRepeatCountCondition = new XIteratorRepeatCountCondition(condVal);
							iteratorSequenceComponent = new XIteratorComponent(name, iteratorRepeatCountCondition);
							
							//populate iteratorSequenceComponent list
							ArrayList iteratorSequenceList = getChildrenOfComponent(connection, osid);
							iteratorSequenceComponent.setSequence(iteratorSequenceList);
							logger.info("... returns " + iteratorSequenceComponent);
							return iteratorSequenceComponent;
						} // /REPEAT_COUNT_CONDITION
						case ConditionTypes.DURATION_CONDITION: {
							logger.info("... ... condType = DURATION_CONDITION"); 
							//TBD
							break;
						} // /DURATION_CONDITION
						case ConditionTypes.ONE_SHOT_CONDITION: {
							logger.info("... ... condType = ONE_SHOT_CONDITION"); 
							//TBD
							break;
						} // /ONE_SHOT_CONDITION
						default: {
							throw new Exception("unable to determine condition type of Iterator element, condType = " + condType); 
						}
					} 
					break;
				} // /TYPE_ITERATOR
				case SequenceElementTypes.TYPE_BRANCH: {
					logger.info("... type = BRANCH"); 
					XBranchComponent branchComponent = new XBranchComponent();
					
					//populate branchComponent
					branchComponent.setComponentName(name);
					List iteratorSequenceList = getChildrenOfComponent(connection, osid);
					branchComponent.setSequence(iteratorSequenceList);
					
					logger.info("... returns " + branchComponent);
					return branchComponent;
				} // /TYPE_BRANCH
				case SequenceElementTypes.TYPE_EXECUTIVE: {
					logger.info("... type = EXECUTIVE"); 
					switch(eaType) {
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_TARGET_SELECTOR: {
							logger.info("... ... eaType = TARGET"); 
							TargetAccessor targetAccessor = new TargetAccessor();
							ITarget target = targetAccessor.getTarget(connection, eaRef);
							XTargetSelector targetSelector = new XTargetSelector(target);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  targetSelector);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_INSTRUMENT_CONFIG_SELECTOR: {
							logger.info("... ... eaType = CONFIG"); 
							InstrumentConfigAccessor instrumentConfigAccessor = new InstrumentConfigAccessor();
							IInstrumentConfig instrumentConfig = instrumentConfigAccessor.getInstrumentConfig(connection, eaRef);
							XInstrumentConfigSelector instrumentConfigSelector = new XInstrumentConfigSelector(instrumentConfig);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  instrumentConfigSelector);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_MOSAIC_OFFSET: {
							logger.info("... ... eaType = MOSAIC_OFFSET"); 
							MosaicOffsetAccessor mosaicOffsetAccessor = new MosaicOffsetAccessor();
							IMosaicOffset mosaicOffset =  mosaicOffsetAccessor.getMosaicOffset(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  mosaicOffset);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_FOCUS_OFFSET: {
							logger.info("... ... eaType = FOCUS_OFFSET"); 
							FocusOffsetAccessor focusOffsetAccessor = new FocusOffsetAccessor();
							IFocusOffset focusOffset =  focusOffsetAccessor.getFocusOffset(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  focusOffset);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_ROTATOR_CONFIG: {
							logger.info("... ... eaType = ROTATOR_CONFIG");
							RotatorConfigAccessor rotatorConfigAccessor = new RotatorConfigAccessor();
							IRotatorConfig rotatorConfig =  rotatorConfigAccessor.getRotatorConfig(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  rotatorConfig);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_AUTOGUIDER_CONFIG: {
							logger.info("... ... eaType = AUTOGUIDER_CONFIG");
							AutoguiderConfigAccessor  autoguiderConfigAccessor= new AutoguiderConfigAccessor();
							IAutoguiderConfig autoguiderConfig =  autoguiderConfigAccessor.getAutoguiderConfig(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  autoguiderConfig);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_ACQUISITION_CONFIG: {
							logger.info("... ... eaType = ACQUISITION_CONFIG");
							AcquisitionConfigAccessor acquisitionConfigAccessor = new AcquisitionConfigAccessor();
							IAcquisitionConfig acquisitionConfig = acquisitionConfigAccessor.getAcquisitionConfig(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  acquisitionConfig);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_EXPOSURE: {
							logger.info("... ... eaType = ACQUISITION_EXPOSURE");
							ExposureAccessor exposureAccessor = new ExposureAccessor();
							IExposure exposure =  exposureAccessor.getExposure(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  exposure);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_APERTURE_OFFSET: {
							logger.info("... ... eaType = APERTURE_OFFSET");
							ApertureConfigAccessor apertureConfigAccessor = new ApertureConfigAccessor();
							IApertureConfig apertureConfig = apertureConfigAccessor.getApertureConfig(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  apertureConfig);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_CALIBRATION: {
							logger.info("... ... eaType = EXECUTIVE_TYPE_CALIBRATION");
							CalibrationAccessor calibrationAccessor = new CalibrationAccessor();
							ICalibration calibration = calibrationAccessor.getCalibration(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  calibration);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_SLEW: {
							logger.info("... ... eaType = SLEW");
							SlewAccessor slewAccessor = new SlewAccessor();
							ISlew slew =  slewAccessor.getSlew(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  slew);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_FOCUSCONTROL: {
							logger.info("... ... eaType = FOCUSCONTROL");
							FocusControlAccessor focusControlAccessor = new FocusControlAccessor();
							IFocusControl focusControl =  focusControlAccessor.getFocusControl(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  focusControl);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_BEAMSTEERINGCONFIG: {
							logger.info("... ... eaType = BEAMSTEERINGCONFIG");
							BeamSteeringConfigAccessor beamSteeringConfigAccessor = new BeamSteeringConfigAccessor();
							IBeamSteeringConfig beamSteeringConfig =  beamSteeringConfigAccessor.getBeamSteeringConfig(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  beamSteeringConfig);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_OPTICALSLIDECONFIG: {
							logger.info("... ... eaType = OPTICALSLIDECONFIG");
							OpticalSlideConfigAccessor opticalSlideConfigAccessor = new OpticalSlideConfigAccessor();
							IOpticalSlideConfig opticalSlideConfig =  opticalSlideConfigAccessor.getOpticalSlideConfig(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  opticalSlideConfig);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						case ExecutiveActionElementTypes.EXECUTIVE_TYPE_TIPTILTABSOLUTEOFFSET: {
							logger.info("... ... eaType = TIPTILTABSOLUTEOFFSET");
							TipTiltAbsoluteOffsetAccessor tipTiltAbsoluteOffsetAccessor = new TipTiltAbsoluteOffsetAccessor();
							ITipTiltAbsoluteOffset tipTiltAbsoluteOffset =  tipTiltAbsoluteOffsetAccessor.getTipTiltAbsoluteOffset(connection, eaRef);
							XExecutiveComponent executiveComponent = new XExecutiveComponent(name,  tipTiltAbsoluteOffset);
							logger.info("... returns " + executiveComponent);
							return executiveComponent;
						}
						default: {
							throw new Exception("unable to determine eaType of ISequenceComponent, eaType = " + eaType);
						}
					}
				} // /TYPE_EXECUTIVE
				default: {
					throw new Exception("unable to determine type of ISequenceComponent, type = " + type);
				}	 
			}
			throw new Exception("ISequenceComponent was incorrectly built, no object was returned");
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
	
	public ArrayList getChildrenOfComponent(Connection connection, long parentId) throws Exception  {
		logger.info("getChildrenOfComponent(" + parentId + ")");
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_CHILD_OBS_IDS_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, parentId);
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_CHILD_OBS_IDS_SQL);
			
			int id;
			ArrayList children = new ArrayList();
			
			while (resultSet.next()) {
				//get the id of the child
				id				= resultSet.getInt(1);
				//get the child and add it to the list 
				children.add(getObservationSequenceComponent(connection, id));
			}
			return children;
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
	
	/* private ************************************************************************************************************************/
	private long getObservationSequenceIDOfGroup(Connection connection, long groupId) throws Exception  {
		logger.info("getObservationSequenceIDOfGroup(" +groupId + ")" );
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		
		try {
			stmt = connection.prepareStatement(GET_OBS_SEQUENCE_ID_OF_GROUP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, groupId);
			
			resultSet = DatabaseTransactor.getInstance().executeQueryStatement(stmt, GET_OBS_SEQUENCE_ID_OF_GROUP_SQL);
			
			int osid;
			
			if (resultSet.next()) {
				osid	= resultSet.getInt(1);
			} else {
				throw new Exception("Group " +groupId + " does not have an observation sequence ID associated aith it");
			}
			logger.info("... returns " + osid);
			return osid;
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
	
	public void deleteObservationSequenceOfGroup(Connection connection, long groupID) throws Exception {
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		try {
			//clear the reference in OBSERVATION_GROUP
			stmt1 = connection.prepareStatement(CLEAR_OBSERVATION_SEQUENCE_OF_GROUP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt1.setLong(1, groupID);
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt1, CLEAR_OBSERVATION_SEQUENCE_OF_GROUP_SQL, false);
			
			//delete all SEQUENCE_COMPONENT entries where gid = groupID;
			stmt2 = connection.prepareStatement(DELETE_OBSERVATION_SEQUENCE_OF_GROUP_SQL, Statement.RETURN_GENERATED_KEYS);
			stmt2.setLong(1, groupID);
			DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt2, DELETE_OBSERVATION_SEQUENCE_OF_GROUP_SQL, false);
		} finally {
			try {
				if (stmt1 != null) {
					stmt1.close();
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

	public long addObservationSequence(Connection connection, long groupID, ISequenceComponent sequenceComponent) throws Exception {
		
		long obsSeqID = -1;

		if (sequenceComponent instanceof XExecutiveComponent) {
			//no parent, therefore parent id = 0 ?
			return insertExecutiveComponent(connection, groupID, 0, (XExecutiveComponent)sequenceComponent);
		} else if (sequenceComponent instanceof XIteratorComponent) {
			return insertIteratorComponent(connection, groupID, 0, (XIteratorComponent)sequenceComponent);
		} else if (sequenceComponent instanceof XBranchComponent) {
			return insertBranchComponent(connection, groupID, 0, (XBranchComponent)sequenceComponent);
		}
		
		//remember, need to return first obs sequence ID
		return obsSeqID;
	}
	
    private long insertExecutiveComponent(Connection connection, long groupID, long parent, XExecutiveComponent executiveComponent) throws Exception{
	
    	PreparedStatement stmt = null;
		
		try {
			IExecutiveAction action =  executiveComponent.getExecutiveAction();
			Class actionClass = (action != null ? action.getClass():null);
		
			//initial insertion into the underlying EA_ database table:
			long eaRef;
			int eaType;
			if (action instanceof IAcquisitionConfig) {
		
			    eaRef = new AcquisitionConfigAccessor().insertAcquisitionConfig(connection, (IAcquisitionConfig)action);
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_ACQUISITION_CONFIG;
		
			} else if (action instanceof IApertureConfig) {
			    
			    eaRef = new ApertureConfigAccessor().insertApertureConfig(connection, (IApertureConfig)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_APERTURE_OFFSET;
			    
			} else if (action instanceof IAutoguiderConfig) {
			    
			    eaRef = new AutoguiderConfigAccessor().insertAutoguiderConfig(connection, (IAutoguiderConfig)action);
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_AUTOGUIDER_CONFIG;
			
			} else if (action instanceof IExposure) {
			
			    eaRef = new ExposureAccessor().insertExposure(connection, (IExposure)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_EXPOSURE;
			
			} else if (action instanceof IFocusOffset) {
			
			    eaRef = new FocusOffsetAccessor().insertFocusOffset(connection, (IFocusOffset)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_FOCUS_OFFSET;
			
			} else if (action instanceof IInstrumentConfigSelector) {
			
			    eaRef = ((IInstrumentConfigSelector)action).getInstrumentConfig().getID();			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_INSTRUMENT_CONFIG_SELECTOR;
			
			} else if (action instanceof IMosaicOffset) {
			
			    eaRef = new MosaicOffsetAccessor().insertMosaicOffset(connection, (IMosaicOffset)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_MOSAIC_OFFSET;
			
			} else if (action instanceof IRotatorConfig) {
			
				eaRef = new RotatorConfigAccessor().insertRotatorConfig(connection, (IRotatorConfig)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_ROTATOR_CONFIG;
			
			} else if (action instanceof ISlew) {
			
			    eaRef = new SlewAccessor().insertSlew(connection, (ISlew)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_SLEW;
			    
			} else if (action instanceof ITargetSelector) {
			    
			    eaRef = ((ITargetSelector)action).getTarget().getID();			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_TARGET_SELECTOR;
			
			} else if (action instanceof ICalibration) {
			    
			    eaRef =  new CalibrationAccessor().insertCalibration(connection, (ICalibration)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_CALIBRATION;
			    
	        } else if (action instanceof IFocusControl) {
			    
			    eaRef =  new FocusControlAccessor().insertFocusControl(connection, (IFocusControl)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_FOCUSCONTROL;
	
			} else if (action instanceof IBeamSteeringConfig) {
			    
			    eaRef =  new BeamSteeringConfigAccessor().insertBeamSteeringConfig(connection, (IBeamSteeringConfig)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_BEAMSTEERINGCONFIG;
			    
			} else if (action instanceof IOpticalSlideConfig) {
			    
			    eaRef =  new OpticalSlideConfigAccessor().insertOpticalSlideConfig(connection, (IOpticalSlideConfig)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_OPTICALSLIDECONFIG;
			    
			} else if (action instanceof ITipTiltAbsoluteOffset) {
			    
			    eaRef =  new TipTiltAbsoluteOffsetAccessor().insertTipTiltAbsoluteOffset(connection, (ITipTiltAbsoluteOffset)action);			
			    eaType = ExecutiveActionElementTypes.EXECUTIVE_TYPE_TIPTILTABSOLUTEOFFSET;
			    
			} else {
			    throw new Exception("unknown IExecutiveAction class type: " +actionClass.getName()); 
			} 
		    
			int type = SequenceElementTypes.TYPE_EXECUTIVE;
			
			//insertion into the SEQUENCE_COMPONENT table
			stmt = connection.prepareStatement(INSERT_OBS_SEQUENCE_SQL, Statement.RETURN_GENERATED_KEYS);
	
			//gid, parent, type, condType, condVal, eaType, eaRef, name
			
			stmt.setLong(1, groupID);
			stmt.setLong(2, parent);
			stmt.setInt(3, type);
			//stmt.setInt(4, condType);  	NB: This value is not inserted for an executive component
			//stmt.setInt(5, condVal);		NB: This value is not inserted for an executive component
			stmt.setInt(4, -1);  
			stmt.setInt(5, -1);
			stmt.setInt(6, eaType);
			stmt.setLong(7, eaRef);
			stmt.setString(8, executiveComponent.getComponentName());
			
			//execute query
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_OBS_SEQUENCE_SQL, true);
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
	
	private long insertIteratorComponent(Connection connection, long groupID, long parent, XIteratorComponent iteratorComponent) throws Exception {
		
		PreparedStatement stmt = null;
		
		try {
			XIteratorRepeatCountCondition repeatCountCondition = (XIteratorRepeatCountCondition)iteratorComponent.getCondition(); 
			String name = iteratorComponent.getComponentName();
			
			//insertion into the SEQUENCE_COMPONENT table
			stmt = connection.prepareStatement(INSERT_OBS_SEQUENCE_SQL, Statement.RETURN_GENERATED_KEYS);
	
			int type = SequenceElementTypes.TYPE_ITERATOR;
			stmt.setLong(1, groupID);
			stmt.setLong(2, parent);
			stmt.setInt(3, type);
			stmt.setInt(4, ConditionTypes.REPEAT_COUNT_CONDITION);
			stmt.setInt(5, repeatCountCondition.getCount());
			stmt.setInt(6, -1); //eaType
			stmt.setLong(7, -1); //eaRef
			stmt.setString(8, name);
			
			//execute query, inserting parent iterator component
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_OBS_SEQUENCE_SQL, true);
			
			//now insert the underlying components
			List childComponents = iteratorComponent.listChildComponents();
			if (childComponents == null) {
				return id;
			}
			Iterator childIterator = childComponents.iterator();
			while (childIterator.hasNext()) {
				ISequenceComponent component = (ISequenceComponent)childIterator.next();
				//addObservationSequence(connection, groupID, component);
	
				if (component instanceof XExecutiveComponent) {
				    insertExecutiveComponent(connection, groupID, id, (XExecutiveComponent)component);
				} else if 
				      (component instanceof XIteratorComponent) {
				    insertIteratorComponent(connection, groupID, id, (XIteratorComponent)component);
				} else if
				      (component instanceof XBranchComponent) {
				    insertBranchComponent(connection, groupID, id, (XBranchComponent)component);
				}
			}
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
	
	private long insertBranchComponent(Connection connection, long groupID, long parent, XBranchComponent branchComponent) throws Exception {

		PreparedStatement stmt = null;
		try {
			//insertion into the SEQUENCE_COMPONENT table
			stmt = connection.prepareStatement(INSERT_OBS_SEQUENCE_SQL, Statement.RETURN_GENERATED_KEYS);
			
			int type = SequenceElementTypes.TYPE_BRANCH;
			String name = branchComponent.getComponentName();
			
			stmt.setLong(1, groupID);
			stmt.setLong(2, parent);
			stmt.setInt(3, type);
			stmt.setInt(4, -1);
			stmt.setInt(5, -1);
			stmt.setInt(6, -1);
			stmt.setLong(7, -1);
			stmt.setString(8, name);
			
			//execute query, inserting parent iterator component
			long id =DatabaseTransactor.getInstance().executeUpdateStatement(connection, stmt, INSERT_OBS_SEQUENCE_SQL, true);
			
			Iterator childIterator = branchComponent.listChildComponents().iterator();
			while (childIterator.hasNext()) {
				XIteratorComponent component = (XIteratorComponent)childIterator.next();
				insertIteratorComponent(connection, groupID, id, component);
			}
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

