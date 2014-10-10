package ngat.oss.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * HARD CODED LOCATION OF RELATED PROPERTIES FILE:  /usr/local/tomcat/conf/osswebservice.properties
 */
public class OssWebserviceProperties extends Properties{
	
	/**
	 	accessmodel.rmi.objectname=AccessModel
		accessmodel.rmi.hostname=oss
	 */
	private static final long serialVersionUID = 6421361677537753439L;

	private static Logger logger = Logger.getLogger(OssWebserviceProperties.class);
	
	private static final String PROPERTIES_FILE_PATH 																			= "/usr/local/tomcat/conf/osswebservice.properties";
	
	public static final String ACCESS_MODEL_RMI_OBJECT_NAME_PROPERTY									= "accessmodel.rmi.objectname";
	public static final String ACCESS_MODEL_RMI_HOST_NAME_PROPERTY 									= "accessmodel.rmi.hostname";
	
	public static final String PROPOSAL_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY		= "proposalaccountmodel.rmi.objectname";
	public static final String PROPOSAL_ACCOUNT_MODEL_RMI_HOST_NAME_PROPERTY 			= "proposalaccountmodel.rmi.hostname";
	
	public static final String USER_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY					= "useraccountmodel.rmi.objectname";
	public static final String USER_ACCOUNT_MODEL_RMI_HOST_NAME_PROPERTY 					= "useraccountmodel.rmi.hostname";
	
	public static final String TAG_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY					= "tagaccountmodel.rmi.objectname";
	public static final String TAG_ACCOUNT_MODEL_RMI_HOST_NAME_PROPERTY 						= "tagaccountmodel.rmi.hostname";
	
	public static final String HISTORY_MODEL_RMI_OBJECT_NAME_PROPERTY								= "historymodel.rmi.objectname";
	public static final String HISTORY_MODEL_RMI_HOST_NAME_PROPERTY 									= "historymodel.rmi.hostname";
	
	public static final String LOCKING_MODEL_RMI_OBJECT_NAME_PROPERTY								= "lockingmodel.rmi.objectname";
	public static final String LOCKING_MODEL_RMI_HOST_NAME_PROPERTY 									= "lockingmodel.rmi.hostname";
	
	public static final String PHASE2_MODEL_RMI_OBJECT_NAME_PROPERTY									= "phase2model.rmi.objectname";
	public static final String PHASE2_MODEL_RMI_HOST_NAME_PROPERTY 									= "phase2model.rmi.hostname";
	
	public static final String INSTRUMENT_REGISTRY_RMI_OBJECT_NAME_PROPERTY					= "instrumentregistry.rmi.objectname";
	public static final String INSTRUMENT_REGISTRY_RMI_HOST_NAME_PROPERTY 						= "instrumentregistry.rmi.hostname";
	
	public static final String TELESCOPE_PROPERTIES_RMI_OBJECT_NAME_PROPERTY					= "telescopeproperties.rmi.objectname";
	public static final String TELESCOPE_PROPERTIES_RMI_HOST_NAME_PROPERTY 					= "telescopeproperties.rmi.hostname";
	
	public static final String TRUE	= new Boolean(true).toString();
	public static final String FALSE = new Boolean(false).toString();
	
	public static final String RMI_PREFIX = "rmi://";
	
	public static OssWebserviceProperties instance;
	
	public static OssWebserviceProperties getInstance() {
		if (instance == null) {
			try {
				instance = new OssWebserviceProperties();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("IOException in loading " + PROPERTIES_FILE_PATH + " no properties loaded!!!");
			}
		}
		return instance;
	}
	
	private OssWebserviceProperties() throws IOException {
		super(getPropertiesFromFile(PROPERTIES_FILE_PATH));
	}
	
	/*
	 //used to get properties in a .jar file
	private static Properties getPropertiesFromFile(String sfp) throws IOException {
		Properties properties = new Properties();
		InputStream inputStream = TomcatProperties.class.getResourceAsStream(PROPERTIES_FILE_PATH);
		properties.load(inputStream);
		
		logger.info("using properties: " + properties);
		return properties;
	}
	*/
	
	private static Properties getPropertiesFromFile(String sfp) throws IOException {
		Properties properties = new Properties();
		
		InputStream inputStream = new FileInputStream(PROPERTIES_FILE_PATH);
		properties.load(inputStream);
		
		logger.info("using properties: " +properties);
		return properties;
	}
	
	public void debugShowProperties() {

		Enumeration keysE = defaults.keys();
		String s = this.getClass().getName() +"[";
		
		while (keysE.hasMoreElements()) {
			Object key = keysE.nextElement();
			Object value = defaults.get(key);
			s = key +":" +value;
		}
		s += "]";
		System.out.println(s);
	}
}
