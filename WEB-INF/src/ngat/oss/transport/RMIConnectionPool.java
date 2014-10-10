package ngat.oss.transport;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;

import ngat.icm.InstrumentDescriptor;
import ngat.icm.InstrumentRegistry;
import ngat.oss.configuration.OssWebserviceProperties;
import ngat.oss.reference.Const;

import org.apache.log4j.Logger;

/**
 * The purpose of this class is to hold references to Remote objects
 * that are used frequently. If a Remote has not been used for a period of <LIFE_SPAN> mS
 * then a new instance of the  Remote object is created and this is used to invoke the
 * RMI service.
 * @author nrc
 */
public class RMIConnectionPool {
	
	static Logger logger = Logger.getLogger(RMIConnectionPool.class);
	private static RMIConnectionPool instance = null;
	
	private static final int LIFE_SPAN = 60000;
	
	private HashMap mapStorage	    = new HashMap();
	private HashMap mapUrl 				= new HashMap();
	private HashMap mapFingerTime 	= new HashMap();
	
	//instance variables
	private java.util.Date instantiationTimeStamp;	
	
	//return the singleton instance of the RMIConnectionHandler
	public static RMIConnectionPool getInstance(){
		if (instance == null) {
			instance = new RMIConnectionPool();
		}
		return instance;
	}
	
	//constructor
	/**
	 * Saves a map of model names against RMI urls
	 * 
	 * Properties file used is often:  /usr/local/tomcat/conf/osswebservice.properties
	 */
	private RMIConnectionPool() {
		logger.info("... instantiating RMIConnectionHandler");
		
		// old example: 		rmi://localhost:1099/TagAccountModel
		// new example:  	rmi://localhost/TagAccountModel
		
		//create the urls and store them in the mapUrl HashMap
		
		String accessModelRMIURL;
		accessModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		accessModelRMIURL  += OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.ACCESS_MODEL_RMI_HOST_NAME_PROPERTY);
		accessModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.ACCESS_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("accessModelRMIURL=" +accessModelRMIURL);
		mapUrl.put(Const.ACCESS_MODEL_SERVICE, accessModelRMIURL);
		
		String proposalAccountModelRMIURL;
		proposalAccountModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		proposalAccountModelRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.PROPOSAL_ACCOUNT_MODEL_RMI_HOST_NAME_PROPERTY);
		proposalAccountModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.PROPOSAL_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("proposalAccountModelRMIURL=" +proposalAccountModelRMIURL);
		mapUrl.put(Const.PROPOSAL_ACCOUNT_SERVICE, proposalAccountModelRMIURL);
		
		String userAccountModelRMIURL;
		userAccountModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		userAccountModelRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.USER_ACCOUNT_MODEL_RMI_HOST_NAME_PROPERTY);
		userAccountModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.USER_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("userAccountModelRMIURL=" +userAccountModelRMIURL);
		mapUrl.put(Const.USER_ACCOUNT_SERVICE, userAccountModelRMIURL);
		
		String tagAccountModelRMIURL;
		tagAccountModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		tagAccountModelRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.TAG_ACCOUNT_MODEL_RMI_HOST_NAME_PROPERTY);
		tagAccountModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.TAG_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("tagAccountModelRMIURL=" +tagAccountModelRMIURL);
		mapUrl.put(Const.TAG_ACCOUNT_SERVICE, tagAccountModelRMIURL);
		
		String historyModelRMIURL;
		historyModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		historyModelRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.HISTORY_MODEL_RMI_HOST_NAME_PROPERTY);
		historyModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.HISTORY_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("historyModelRMIURL=" +historyModelRMIURL);
		mapUrl.put(Const.HISTORY_MODEL_SERVICE, historyModelRMIURL);
		
		String lockingModelRMIURL;
		lockingModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		lockingModelRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.LOCKING_MODEL_RMI_HOST_NAME_PROPERTY);
		lockingModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.LOCKING_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("lockingModelRMIURL=" +lockingModelRMIURL);
		mapUrl.put(Const.LOCKING_MODEL_SERVICE, lockingModelRMIURL);

		String phase2ModelRMIURL;
		phase2ModelRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		phase2ModelRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.PHASE2_MODEL_RMI_HOST_NAME_PROPERTY);
		phase2ModelRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.PHASE2_MODEL_RMI_OBJECT_NAME_PROPERTY);
		logger.info("phase2ModelRMIURL=" +phase2ModelRMIURL);
		mapUrl.put(Const.PHASE2_MODEL_SERVICE, phase2ModelRMIURL);

		String occTelescopePropertiesRMIURL;
		occTelescopePropertiesRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		occTelescopePropertiesRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.TELESCOPE_PROPERTIES_RMI_HOST_NAME_PROPERTY);
		occTelescopePropertiesRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.TELESCOPE_PROPERTIES_RMI_OBJECT_NAME_PROPERTY);
		logger.info("occTelescopePropertiesRMIURL=" +occTelescopePropertiesRMIURL);
		mapUrl.put(Const.OCC_TELESCOPE_PROPERTIES, occTelescopePropertiesRMIURL);
		
		String occInstrumentRegistryRMIURL;
		occInstrumentRegistryRMIURL 	= OssWebserviceProperties.RMI_PREFIX;
		occInstrumentRegistryRMIURL  +=  OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.INSTRUMENT_REGISTRY_RMI_HOST_NAME_PROPERTY);
		occInstrumentRegistryRMIURL  += "/" +OssWebserviceProperties.getInstance().getProperty(OssWebserviceProperties.INSTRUMENT_REGISTRY_RMI_OBJECT_NAME_PROPERTY);
		logger.info("occInstrumentRegistryRMIURL=" +occInstrumentRegistryRMIURL);
		mapUrl.put(Const.OCC_INSTRUMENT_REGISTRY, occInstrumentRegistryRMIURL);
	}
	
	/**
	 * Given the service name (i.e. model name / telescope object / instrument registry) Look up the remote object in the map and return the RMI stub for that service
	 * @param serviceName Service / model to look up  (one of ngat.oss.reference.*)
	 * @return Remote stub of the model
	 * @throws MalformedURLException
	 * @throws RemoteException 
	 * @throws NotBoundException
	 */
	public Remote getRemoteServiceObject(String serviceName) throws MalformedURLException, RemoteException, NotBoundException {
		logger.info("getRemoteServiceObject(" +serviceName +")" );
		
		//look up the model from the mapNameClient HashMap
		long timeNow = new Date().getTime();
		Remote model = (Remote)mapStorage.get(serviceName);
		
		if (model == null) {
			logger.info("... model==null, will lookup new reference");
			//model not found in the map
			//it may be the first invocation
			//instantiate the required RMI Model
			//update the HashMaps and return it
			model = lookupModel(serviceName, timeNow);
			logger.info("... returning new reference");
			return model;
		} else {
			//SOAPClient found
			//check the last usage time of the client, if it's older than LIFE_SPAN
			//dump it and instantiate a new one, otherwise return it
			Long lastFingerTime = getLastFingerTime(serviceName);
			if (lastFingerTime != null) {
				if (lastFingerTime.longValue() < (timeNow - LIFE_SPAN) ) {
					//reference is too old, create and return a new one
					logger.info("... reference is too old, creating new one");
					model = lookupModel(serviceName, timeNow);
					logger.info("... returning new reference");
					return model;
				} else {
					//reference is still valid, update the finger-time and return the reference
					logger.info("... reference is still valid");
					finger(serviceName, timeNow);
					return (Remote)mapStorage.get(serviceName);
				}
			} 
		}
		return null;
	}
	
	/**
	 * The Naming.lookup() operation
	 * @param serviceName
	 * @param timeNow
	 * @return
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	private Remote lookupModel(String serviceName, long timeNow) throws MalformedURLException, RemoteException, NotBoundException {
		logger.info("... lookupModel(" +serviceName +"," +timeNow +")");
		String url = (String)mapUrl.get(serviceName);
		logger.info("... looking up Remote object at " +url);
		Remote model =  Naming.lookup(url);
		logger.info("... received " +model);
		mapStorage.put(serviceName, model);
		finger(serviceName, timeNow);
		return model;
	}
	
	/**
	 * Save a new access time to the map tracking how old an object reference is
	 * @param serviceName
	 * @param fingerTime
	 */
	private void finger(String serviceName, long fingerTime) {
		mapFingerTime.put(serviceName, new Long(fingerTime));
	}
	
	/**
	 * Find out how log ago an object was used
	 * @param serviceName
	 * @return
	 */
	private Long getLastFingerTime(String serviceName) {
		return (Long)mapFingerTime.get(serviceName);
	}
	
	public String toString() {
		return this.getClass().getName() +" [instantiationTimeStamp:" +instantiationTimeStamp +"]";
	}	
}


