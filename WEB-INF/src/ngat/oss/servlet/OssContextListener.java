package ngat.oss.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ngat.oss.transport.RMIConnectionPool;

import org.apache.log4j.Logger;

/**
 * Servlet started at oss startup, as defined in web.xml:
 * 
  
  <listener>
    <listener-class>ngat.oss.servlet.OssContextListener</listener-class>
  </listener>
  
 *
 */
public class OssContextListener implements ServletContextListener {

	static Logger logger = Logger.getLogger(OssContextListener.class);
	
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub		
	}

	public void contextInitialized(ServletContextEvent sce) {
		logger.info("OSS context started: " + sce.getServletContext().getContextPath().toString());
		
		/*
		 * Write any code into here that we want to have executed when the servlet context starts
		 */
		
		//1. Start RMI connection pool.
		RMIConnectionPool.getInstance();
		
	}

}
