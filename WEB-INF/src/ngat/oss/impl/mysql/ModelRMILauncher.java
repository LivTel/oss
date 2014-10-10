package ngat.oss.impl.mysql;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;

import ngat.oss.configuration.OssProperties;
import ngat.oss.impl.mysql.model.AccessModel;
import ngat.oss.impl.mysql.model.AccountModel;
import ngat.oss.impl.mysql.model.HistoryModel;
import ngat.oss.impl.mysql.model.LockingModel;
import ngat.oss.impl.mysql.model.Phase2Model;
import ngat.oss.reference.AccountModelTypes;

import org.apache.log4j.Logger;

public class ModelRMILauncher {
	
	private static final String RMI_URL_PREFIX = "rmi://";
	private static final String RMI_HOST="localhost";
	
	private static volatile boolean shouldStayBound = true;
	
	static Logger logger = Logger.getLogger(ModelRMILauncher.class);
	
	public static void main(String args[]) {
		
		try {
			//connect to the database and instantiate the singleton instance
			ConnectionPool.getInstance();
			
			//from the properties file, get the model names and ports
			
			String accessModelRMIName = OssProperties.getInstance().getProperty(OssProperties.ACCESS_MODEL_RMI_OBJECT_NAME_PROPERTY);
			int accessModelRMIPort = Integer.parseInt(OssProperties.getInstance().getProperty(OssProperties.ACCESS_MODEL_RMI_PORT_PROPERTY));
			
			String proposalAccountModelRMIName = OssProperties.getInstance().getProperty(OssProperties.PROPOSAL_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY);
			int proposalAccountModelRMIPort = Integer.parseInt(OssProperties.getInstance().getProperty(OssProperties.PROPOSAL_ACCOUNT_MODEL_RMI_PORT_PROPERTY));
			
			String tagAccountModelRMIName = OssProperties.getInstance().getProperty(OssProperties.TAG_ACCOUNT_MODEL_RMI_OBJECT_NAME_PROPERTY);
			int tagAccountModelRMIPort = Integer.parseInt(OssProperties.getInstance().getProperty(OssProperties.TAG_ACCOUNT_MODEL_RMI_PORT_PROPERTY));
			
			String historyModelRMIName = OssProperties.getInstance().getProperty(OssProperties.HISTORY_MODEL_RMI_OBJECT_NAME_PROPERTY);
			int historyModelRMIPort = Integer.parseInt(OssProperties.getInstance().getProperty(OssProperties.HISTORY_MODEL_RMI_PORT_PROPERTY));
			
			String lockingModelRMIName = OssProperties.getInstance().getProperty(OssProperties.LOCKING_MODEL_RMI_OBJECT_NAME_PROPERTY);
			int lockingModelRMIPort = Integer.parseInt(OssProperties.getInstance().getProperty(OssProperties.LOCKING_MODEL_RMI_PORT_PROPERTY));
			
			String phase2ModelRMIName = OssProperties.getInstance().getProperty(OssProperties.PHASE2_MODEL_RMI_OBJECT_NAME_PROPERTY);
			int phase2ModelRMIPort = Integer.parseInt(OssProperties.getInstance().getProperty(OssProperties.PHASE2_MODEL_RMI_PORT_PROPERTY));
			
			//instantiate and bind AccessModel
			AccessModel accessModel = new AccessModel(accessModelRMIPort);
			//bind(accessModelRMIName, accessModel, accessModelRMIPort);
			asynchRebind(accessModelRMIName, accessModel, accessModelRMIPort);

			//instantiate and bind AccountModel for Proposals
			AccountModel proposalAccountModel = new AccountModel(proposalAccountModelRMIPort, AccountModelTypes.PROPOSAL_ACCOUNT_TYPE);
			//bind(proposalAccountModelRMIName, proposalAccountModel, proposalAccountModelRMIPort);
			asynchRebind(proposalAccountModelRMIName, proposalAccountModel, proposalAccountModelRMIPort);

			//instantiate and bind AccountModel for Users
			AccountModel tagAccountModel = new AccountModel(tagAccountModelRMIPort, AccountModelTypes.TAG_ACCOUNT_TYPE);
			//bind(tagAccountModelRMIName, tagAccountModel, tagAccountModelRMIPort);
			asynchRebind(tagAccountModelRMIName, tagAccountModel, tagAccountModelRMIPort);

			//instantiate and bind HistoryModel
			HistoryModel historyModel = new HistoryModel(historyModelRMIPort);
			//bind(historyModelRMIName, historyModel, historyModelRMIPort);
			asynchRebind(historyModelRMIName, historyModel, historyModelRMIPort);

			//instantiate and bind LockingMode
			LockingModel lockingModel = new LockingModel(lockingModelRMIPort);
			//bind(lockingModelRMIName, lockingModel, lockingModelRMIPort);
			asynchRebind(lockingModelRMIName, lockingModel, lockingModelRMIPort);

			//instantiate and bind Phase2Model
			Phase2Model phase2Model = new Phase2Model(phase2ModelRMIPort);
			//bind(phase2ModelRMIName, phase2Model, phase2ModelRMIPort);
			asynchRebind(phase2ModelRMIName, phase2Model, phase2ModelRMIPort);

			//keep alive thread 
			while (shouldStayBound) {
				try {Thread.sleep(60000L);} catch (InterruptedException ix) {}
				System.err.print(".");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    private static void asynchRebind(String modelName, Remote modelInterface, int port) {
		final int fport = port;
		final String fmodelName = modelName;
		final Remote fmodelInterface = modelInterface;
		Runnable r = new Runnable() {
			public void run() {
			    rebind(fmodelName, fmodelInterface, fport);
			}};
	
		(new Thread(r)).start();
    }

    private static void rebind(String modelName, Remote modelInterface, int port) {
	
	while (true) {

	    try { 
	    	bind(modelName, modelInterface, port);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    try {Thread.sleep(300000L);} catch (InterruptedException ix) {}
	}

    }


	private static void bind(String modelName, Remote modelInterface, int port) throws RemoteException, MalformedURLException {
		String bindURL = RMI_URL_PREFIX +RMI_HOST + "/" + modelName;
		
		logger.info("attempting bind of " + modelInterface.getClass().getName() + " to URL: " +bindURL + " using port: " + port );

		//System.err.println("Binding: "+modelInterface.getClass().getName() + " as " + bindURL + " on port " + port);
		Naming.rebind(bindURL, modelInterface);
		logger.info("bound: "+modelInterface.getClass().getName() + " as " + bindURL + " on port " + port);
		//System.err.println("Bind: "+bindURL);
	}
}
