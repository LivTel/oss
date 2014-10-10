package ngat.oss.impl.rmi.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ngat.oss.impl.rmi.accessors.StatusAccessor;
import ngat.oss.model.IStatusModel;
import ngat.oss.transport.RemotelyPingable;
import ngat.phase2.IPublishedSystemProperties;

import org.apache.log4j.Logger;

public class StatusModel extends UnicastRemoteObject implements IStatusModel, RemotelyPingable {

	static Logger logger = Logger.getLogger(StatusModel.class);
	
	StatusAccessor	statusAccessor;
	
	public StatusModel() throws RemoteException {
		statusAccessor	= new StatusAccessor();
	}
	
	public void ping() throws RemoteException {
		//doesn't have to do anything
		logger.info("ping()");
	}
	
	public IPublishedSystemProperties getPublishedSystemProperties() throws RemoteException {
		return statusAccessor.getPublishedSystemProperties();
	}

}
