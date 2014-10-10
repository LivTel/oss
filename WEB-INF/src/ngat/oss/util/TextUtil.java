package ngat.oss.util;

public class TextUtil {
	
	/**
	 * converts an array of char into a String 
	 * @param chars
	 * @return String representation of the chars
	 */
	public static String convertToString(char[] chars) {
		String s="";
		for (int i=0; i< chars.length; i++ ) {
			s += Character.toString(chars[i]);
		}
		return s;
	}
	
	public static String convertBooleanToLetter(boolean b) {
		if (b) return "T";
		return "F";
	}
}
