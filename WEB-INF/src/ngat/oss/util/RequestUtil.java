package ngat.oss.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

import ngat.jibxsoap.BooleanTypeParameter;
import ngat.jibxsoap.IntTypeParameter;
import ngat.jibxsoap.LongTypeParameter;
import ngat.jibxsoap.NullTypeParameter;
import ngat.jibxsoap.Response;
import ngat.jibxsoap.StringTypeParameter;
import ngat.jibxsoap.VoidTypeParameter;

import org.apache.log4j.Logger;

public class RequestUtil {
	static Logger logger = Logger.getLogger(RequestUtil.class);
	
	public static Response wrapResponse(Method method, Object invocationResponse) {
		logger.info("wrapResponse(" + method + "," +invocationResponse + ")");
		Response phase2Response = new Response();
		
		if (method.getReturnType().equals(void.class)) {
			//void method, no return object
			logger.info("... invocationResponse is 'void'");
			phase2Response.setCollection(false);
			ArrayList responseParameters = new ArrayList();
			responseParameters.add(new VoidTypeParameter());
			phase2Response.setResponseParameters(responseParameters);
			return phase2Response;
		}
		
		if (invocationResponse == null) {
			logger.info("... got invocationResponse of type null, returning NullTypeParameter response");
			phase2Response.setCollection(false);
			ArrayList responseParameters = new ArrayList();
			responseParameters.add(new NullTypeParameter());
			phase2Response.setResponseParameters(responseParameters);
			return phase2Response;
		} 
		
		//there's a return object that's not null and not void
		//if the return is a collection, set the collection parameter = true
		if (invocationResponse.getClass().equals(ArrayList.class)) {
			logger.info("... invocationResponse is a collection");
			phase2Response.setCollection(true);
			ArrayList responseParameters = (ArrayList)invocationResponse;
			phase2Response.setResponseParameters(responseParameters);
		} else {
			logger.info("... invocationResponse is not a collection");
			phase2Response.setCollection(false);
			ArrayList responseParameters = new ArrayList();
			//if the invocationResponse is a basic type, wrap it with a transportable object
			if (invocationResponse.getClass().equals(Long.class)) {
				logger.info("... invocationResponse is Long, returning LongTypeParameter");
				responseParameters.add(new LongTypeParameter((Long)invocationResponse));
			} else if (invocationResponse.getClass().equals(Integer.class)) {
				logger.info("... invocationResponse is Int, returning IntTypeParameter");
				responseParameters.add(new IntTypeParameter((Integer)invocationResponse));
			} else if (invocationResponse.getClass().equals(Boolean.class)) {
				logger.info("... invocationResponse is Boolean, returning BooleanTypeParameter");
				responseParameters.add(new BooleanTypeParameter((Boolean)invocationResponse));
		    } else if (invocationResponse.getClass().equals(String.class)) {
				logger.info("... invocationResponse is String, returning StringTypeRequestParameter");
				responseParameters.add(new StringTypeParameter((String)invocationResponse));
			} else {
				logger.info("... invocationResponse is " + invocationResponse.getClass().getName() + ", returning class without wrapping");
				responseParameters.add(invocationResponse);
			}
			phase2Response.setResponseParameters(responseParameters);
		}
		
		return phase2Response;
	}
	
	/**
	 * Return a Phase2Response that wraps a supplied exception
	 * @param e The exception
	 * @return The Phase2Response wrapping the exception
	 */
	
	public static Response getExceptionResponse(Exception e) {
		e.printStackTrace();
		Response phase2Response = new Response();
		
		if (e.getCause() == null) {
			phase2Response.setErrorMessage(e.getClass().getName() + ": " +e.getMessage());
		} else {
			Throwable t = e.getCause();
			phase2Response.setErrorMessage(t.getClass().getName() + ": " +t.getMessage());
		}
		
		return phase2Response;
	}
}
