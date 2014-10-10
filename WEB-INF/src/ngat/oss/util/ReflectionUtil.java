package ngat.oss.util;

import java.lang.reflect.Method;

import ngat.oss.exception.Phase2Exception;

import org.apache.log4j.Logger;

public class ReflectionUtil {
	
	static Logger logger = Logger.getLogger(ReflectionUtil.class);
	
	/**
	 * 
	 * @param hostObject
	 * @param methodName
	 * @return
	 * @throws Phase2Exception 
	 */
	public static Method findMethodFromName(Object hostObject, String methodName) throws Phase2Exception {
		logger.info("findMethodFromName(" + hostObject.getClass().getName() + "," + methodName + ")");
		if (getNumOfMethodsWithName(hostObject, methodName) > 1) {
			throw new Phase2Exception("too many methods on object " + hostObject.getClass().getName() + " have name " + methodName);
		}
		Method[] methods = hostObject.getClass().getMethods();
		for (int i=0; i< methods.length; i++) {
			Method method = methods[i];
			if (method.getName().equals(methodName)) {
				logger.info("... found method with same name");
				return method;
			}
		}
		logger.info("... FAILED to find method with same name");
		return null;
	}
	
	private static  int getNumOfMethodsWithName(Object hostObject, String methodName) {
		int numMethodsOfName = 0;
		Method[] methods = hostObject.getClass().getMethods();
		for (int i=0; i< methods.length; i++) {
			Method method = methods[i];
			if (method.getName().equals(methodName)) {
				numMethodsOfName ++;
			}
		}
		return numMethodsOfName;
	}
	
	/**
	 * Returns whether the supplied method is extant
	 * @param hostObject hosting object
	 * @param methodName method name
	 * @param methodParameterDefinition definition of the method
	 * @return 
	 */
	public static boolean methodExists(Object hostObject, String methodName, Class[] methodParameterDefinition) {
		logger.info("methodExists(" + hostObject.getClass().getName() + "," +methodName + "," +toString(methodParameterDefinition) + ")" );
		boolean methodExists = false;
		try {
			hostObject.getClass().getMethod(methodName, methodParameterDefinition);
			methodExists = true;
		} catch (Exception e) {
			e.printStackTrace();
			methodExists = false;
		}
		logger.info("... returns " + methodExists);
		return methodExists;
	}
	
	/**
	 * return a String representation of an array of classes
	 * @param classes
	 * @return
	 */
	public static String toString(Class[] classes) {
		String s = "[";
		boolean hadMembers = false;
		for (int i=0; i< classes.length; i++) {
			hadMembers = true;
			Class classFound = classes[i];
			if (classFound != null) {
				s += classes[i].getName() + ",";
			} else {
				s += "null,";
			}
		}
		if (hadMembers) {
			s = s.substring(0, s.length() - 1);
		}
		s += "]";
		return s;
	}
	
	public static String toString(Method method) {
		return "[" +method.getName() +" (" + toString(method.getParameterTypes()) + ")]";	
	}
	
	public static String toString(Object[] params) {
		String s = "[";
		boolean hadMembers = false;
		for (int i=0; i< params.length; i++) {
			hadMembers = true;
			Object objFound = params[i];
			if (objFound != null) {
				s += objFound.getClass().getName()  + ",";
			} else {
				s += "null,";
			}
		}
		if (hadMembers) {
			s = s.substring(0, s.length() - 1);
		}
		s += "]";
		return s;
	}
}
