package ngat.oss.handler;

import java.lang.reflect.Method;

import ngat.jibxsoap.Request;
import ngat.jibxsoap.Response;
import ngat.oss.impl.rmi.model.StatusModel;
import ngat.oss.model.IPhase2Model;
import ngat.oss.reference.Const;
import ngat.oss.transport.RMIConnectionPool;
import ngat.oss.util.ReflectionUtil;
import ngat.oss.util.RequestUtil;

import org.apache.log4j.Logger;

public class StatusRequestHandler {
	
	static Logger logger = Logger.getLogger(StatusRequestHandler.class);
	
	public StatusRequestHandler() {
		logger.info("instantiated " + this.getClass().getName());
	}
	
	public static Response handleRequest(Request request) {
		try {
			logger.info("received request: " +request);
		
			String methodName = request.getMethodName();
			Object[] pojoParameters = request.getPojoMethodParameters();
			
			//no rmi lookup required. We aren't invoking directly on an rmi object
			StatusModel model = new StatusModel();
			
			Method method = ReflectionUtil.findMethodFromName(model, methodName);
			if (method == null) {
				throw new NoSuchMethodException("Unknown method '" + methodName + "' on object of type " +model.getClass().getName() );
			}
			logger.info("... invoking method: " + method.getName() + " on " +model.getClass().getName() + " using parameters: " + ReflectionUtil.toString(pojoParameters));
			Object returnObject = method.invoke(model, pojoParameters);
			Response response;
			if (returnObject == null) {
				logger.info("... got null return object");
				response = RequestUtil.wrapResponse(method, returnObject);
				logger.info("... encoded response from " + returnObject  + " successfully");
			} else {
				logger.info("... got return object of type " + returnObject.getClass().getName());
				response = RequestUtil.wrapResponse(method, returnObject);
				logger.info("... encoded response from " + returnObject.getClass().getName() + " successfully");
			}
			
			logger.info("... returning response: " + response);
			
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return RequestUtil.getExceptionResponse(e);
		}
	}
}
