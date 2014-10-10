package ngat.oss.impl.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;

import ngat.oss.configuration.OssProperties;

import org.apache.log4j.Logger;

public class ConnectionPool {

	private static final String DATABASE_DRIVER_URL = "com.mysql.jdbc.Driver";
	private static final int MAX_NUM_AVAILABLE_CONNECTIONS = 49;
	
	private static Logger logger = Logger.getLogger(ConnectionPool.class);
	private static ConnectionPool instance;

	private String url;
	private ArrayList<Connection> availableConnections = new ArrayList<Connection>();
	private ArrayList<Connection> allocatedConnections = new ArrayList<Connection>();
	
	/*
	public ********************************
	*/
	
	public static ConnectionPool getInstance() {
		if (instance == null) {
			instance = new ConnectionPool();
		}
		return instance;
	}
	
	public Connection getConnection() throws SQLException {
		
		Connection connection;
		Savepoint savePoint;
		
		if (availableConnections.size() > 0) {
			logger.info("retrieving connection from availableConnections list");
			connection = (Connection)availableConnections.get(availableConnections.size()-1);
			logger.info("... success");
		} else {
			//no available connection, make one, put it on the available list and return it
			logger.warn("availableConnections list too small");
			connection = makeConnectionAvailable();
		}

		//set save point on connection (first operation on the connection so if it fails we know the connection is problematic)
		try {
			logger.info("attempting to setAutoCommit(false) and setSavepoint() on connection");
			connection.setAutoCommit(false);
			savePoint = connection.setSavepoint();
			logger.info("... success");
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("FAILED, removing connection from availableConnections list");
			//The connection is problematic, remove it from the available connections list. 
			availableConnections.remove(connection);
			//create a new connection in it's place.
			connection = makeConnectionAvailable(); //may need to check that the new connection is ok, for now I'm simply assuming here that it is.
		}
		
		//we have a healthy connection, move it from available to allocated
		logger.info("moving connection to allocatedConnections list");
		availableConnections.remove(connection);
		allocatedConnections.add(connection);
		logger.info("... success");
		logger.info("... availableConnections size=" + availableConnections.size() + ", allocatedConnections size=" + allocatedConnections.size());
		logger.info("... returning connection");
		return connection;
	}
	
	//called to put the connection back onto the available connection list (for later usage)
	public void surrenderConnection(Connection connection) {
		logger.info("surrenderConnection(Connection)");
		
		if (!allocatedConnections.contains(connection)) {
			logger.error("connection to surrender was not found in allocated connections, weird");
			return;
		}
		
		logger.info("... removing connection from allocated list");
		allocatedConnections.remove(connection);
		logger.info("... adding connection to available list");
		availableConnections.add(connection);
		logger.info("... connection successfully returned to available list");
		logger.info("... availableConnections size=" + availableConnections.size() + ", allocatedConnections size=" + allocatedConnections.size());
	}
	
	/*
	private ********************************
	*/
	
	//private constructor for singleton
	private ConnectionPool() {
		logger.info("ConnectionPool singleton instance created");
		
		String host, database, user, password;
		host = OssProperties.getInstance().getProperty(OssProperties.DATABASE_HOST_PROPERTY);
		database = OssProperties.getInstance().getProperty(OssProperties.DATABASE_DATABASE_PROPERTY);
		user = OssProperties.getInstance().getProperty(OssProperties.DATABASE_USER_PROPERTY);
		password = OssProperties.getInstance().getProperty(OssProperties.DATABASE_PASSWORD_PROPERTY);
		
		url = "jdbc:mysql://"+host+"/"+database +"?user="+user+"&password="+password;

		logger.info("database url set to :" + url);
		
		try {
			logger.info("Registering database driver: " + DATABASE_DRIVER_URL);
			Class.forName(DATABASE_DRIVER_URL).newInstance();
			logger.info("... registered");
		} catch (Exception e) {
			//show stopper, we can't register the driver, shut down
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		try {
			logger.info("Creating and loading database connections");
			loadConnections();
			logger.info("... all connections loaded");
			logger.info("... availableConnections size=" + availableConnections.size() + ", allocatedConnections size=" + allocatedConnections.size());
		} catch (Exception e) {
			//show stopper, we can't make connections, shut down
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	//populate the availableConnections list 
	private void loadConnections() throws SQLException {
		for (int i=0; i< MAX_NUM_AVAILABLE_CONNECTIONS; i++) {
			makeConnectionAvailable();
		}
	}
	
	//creates a connection and moves it onto the availableConnections list
	private Connection makeConnectionAvailable() throws SQLException {
		Connection connection = createConnection();
		logger.info("adding connection to availableConnections list");
		availableConnections.add(connection);
		logger.info("... connection added");
		return connection;
	}
	
	private Connection createConnection() throws SQLException {
		logger.info("creating connection using :" + url);
		Connection connection = DriverManager.getConnection(url);
		logger.info("... connection created");
		return connection;
	}
	
}
