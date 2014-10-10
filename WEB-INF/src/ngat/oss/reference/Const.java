package ngat.oss.reference;

public class Const {

	/*
	 * Names are reflected in *-service.xml properties files, most are directly tied to the RMIConnectionPool as well (one to one mapping) 
	 */
	//These are all one-to-one mappings from the web-service to the RMI objects
	public static final String ACCESS_MODEL_SERVICE = "access-service";			
	public static final String PROPOSAL_ACCOUNT_SERVICE	= "proposal-account-service";
	public static final String USER_ACCOUNT_SERVICE = "user-account-service";
	public static final String TAG_ACCOUNT_SERVICE = "tag-account-service";
	public static final String HISTORY_MODEL_SERVICE = "history-service";
	public static final String LOCKING_MODEL_SERVICE = "locking-service";
	public static final String PHASE2_MODEL_SERVICE = "phase2-service";
	
	//The status service connects the 2 occ rmi objects to the status web-service
	public static final String STATUS_MODEL_SERVICE = "status-service";
	public static final String OCC_TELESCOPE_PROPERTIES = "occ-telescope-properties";
	public static final String OCC_INSTRUMENT_REGISTRY = "occ-instrument-registry";
	
	public static final long ERROR_ID = -1;
}
