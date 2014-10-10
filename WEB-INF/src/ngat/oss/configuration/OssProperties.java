package ngat.oss.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * HARD CODED LOCATION OF RELATED PROPERTIES FILE: /oss/oss/config/oss.properties
 */
public class OssProperties extends Properties{
	
	private static final long serialVersionUID = 6421361677537753439L;

	private static Logger logger = Logger.getLogger(OssProperties.class);
	
	private static final String PROPERTIES_FILE_PATH 																= "/oss/oss/config/oss.properties";
	
	public static final String ACCESS_MODEL_RMI_OBJECT_NAME_PROPERTY						= "accessmodel.rmi.objectname";
	public static final String ACCESS_MODEL_RMI_PORT_PROPERTY 										= "accessmodel.rmi.port";
	
	public static final String PROPOSAL_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY	= "proposalaccountmodel.rmi.objectname";
	public static final String PROPOSAL_ACCOUNT_MODEL_RMI_PORT_PROPERTY 				= "proposalaccountmodel.rmi.port";
	
	public static final String TAG_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY			= "tagaccountmodel.rmi.objectname";
	public static final String TAG_ACCOUNT_MODEL_RMI_PORT_PROPERTY 							= "tagaccountmodel.rmi.port";
	
	public static final String HISTORY_MODEL_RMI_OBJECT_NAME_PROPERTY						= "historymodel.rmi.objectname";
	public static final String HISTORY_MODEL_RMI_PORT_PROPERTY 									= "historymodel.rmi.port";
	
	public static final String LOCKING_MODEL_RMI_OBJECT_NAME_PROPERTY						= "lockingmodel.rmi.objectname";
	public static final String LOCKING_MODEL_RMI_PORT_PROPERTY 									= "lockingmodel.rmi.port";
	
	public static final String PHASE2_MODEL_RMI_OBJECT_NAME_PROPERTY						= "phase2model.rmi.objectname";
	public static final String PHASE2_MODEL_RMI_PORT_PROPERTY 										= "phase2model.rmi.port";
	
	public static final String DATABASE_HOST_PROPERTY 														= "database.host";
	public static final String DATABASE_DATABASE_PROPERTY 												= "database.db";
	public static final String DATABASE_USER_PROPERTY 														= "database.user";
	public static final String DATABASE_PASSWORD_PROPERTY 											= "database.password";
	
	public static final String TRUE 																								= new Boolean(true).toString();
	public static final String FALSE 																							= new Boolean(false).toString();
	
	public static final String RMI_PREFIX = "rmi://";
	
	public static OssProperties instance;
	
	public static OssProperties getInstance() {
		if (instance == null) {
			try {
				instance = new OssProperties();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("IOException in loading " + PROPERTIES_FILE_PATH + " no properties loaded!!!");
			}
		}
		return instance;
	}
	
	private OssProperties() throws IOException {
		super(getPropertiesFromFile(PROPERTIES_FILE_PATH));
	}
	
	/*
	//reads from within .jar
	private static Properties getPropertiesFromFile(String sfp) throws IOException {
		Properties properties = new Properties();
		InputStream inputStream = OssProperties.class.getResourceAsStream(PROPERTIES_FILE_PATH);
		properties.load(inputStream);
		
		logger.info("using properties: " + properties);
		return properties;
	}
	*/
	
	private static Properties getPropertiesFromFile(String sfp) throws IOException {
		Properties properties = new Properties();
		
		InputStream inputStream = new FileInputStream(PROPERTIES_FILE_PATH);
		properties.load(inputStream);
		
		logger.info("using properties: " + properties);
		return properties;
	}
	
	public void debugShowProperties() {

		Enumeration keysE = defaults.keys();
		String s = this.getClass().getName() +"[";
		
		while (keysE.hasMoreElements()) {
			Object key = keysE.nextElement();
			Object value = defaults.get(key);
			s = key + ":" + value;
		}
		s += "]";
		System.out.println(s);
	}
}
