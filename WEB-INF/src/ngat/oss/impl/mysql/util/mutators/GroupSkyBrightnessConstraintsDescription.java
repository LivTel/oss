package ngat.oss.impl.mysql.util.mutators;

/*
 * class to represent the old set of sky brightness constraints specified in the database
 */
public class GroupSkyBrightnessConstraintsDescription {
	
	private int twilightValue; 						//0 = CIVIL; 1 = NAUTICAL; 2 = ASTRONOMICAL; 3 = NIGHT_TIME
	private double minimumLunarDistance; 	// radians
	private int moonBrightness; 					// 0 = dark (MOON_DOWN); 1 = bright (MOON_UP)
	
	public GroupSkyBrightnessConstraintsDescription() {
	}
	public double getMinimumLunarDistance() {
		return minimumLunarDistance;
	}
	public void setMinimumLunarDistance(double minimumLunarDistance) {
		this.minimumLunarDistance = minimumLunarDistance;
	}
	public int getMoonBrightness() {
		return moonBrightness;
	}
	public void setMoonBrightness(int moonBrightness) {
		this.moonBrightness = moonBrightness;
	}
	public int getTwilightValue() {
		return twilightValue;
	}
	public void setTwilightValue(int twilightValue) {
		this.twilightValue = twilightValue;
	}
	
	public String getTwilightName() {
		switch (twilightValue) {
			case 0:
				return "CIVIL";
			case 1:
				return "NAUTICAL";
			case 2:
				return "ASTRONOMICAL";
			case 3:
				return "NIGHT_TIME";
			default:
				return "UNKNOWN";
		}
	}
	
	public String getMoonDescription() {
		switch (moonBrightness) {
			case 0:
				return "MOON_DOWN (dark)";
			case 1:
				return "MOON_UP (bright)";
			default:
				return "UNKNOWN";
		}
	}
	
	public String toString() {
		String s="";
		s += this.getClass().getName() + "[";
		s += getTwilightName() + " (twilight_value=" + getTwilightValue() + "), ";
		s += "min_lunar_dist=" + minimumLunarDistance + " radians, ";
		s += "moon_brightness=" + getMoonDescription() + "]";
		return s;
	}
}