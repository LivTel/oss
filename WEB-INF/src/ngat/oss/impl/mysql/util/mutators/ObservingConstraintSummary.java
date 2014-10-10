package ngat.oss.impl.mysql.util.mutators;

import ngat.oss.impl.mysql.reference.ObservingConstraintTypes;
import ngat.phase2.IObservingConstraint;

public class ObservingConstraintSummary {
	
	//field values of the Observing_Constraint table in the database
	private long groupId; 
	private int id, type, category; //field values of the Observing_Constraint table in the database
	private double min, max;
	
	public ObservingConstraintSummary(long groupId, int id, int type, int category, double min, double max) {	
		this.groupId = groupId;
		this.id = id;
		this.type = type;
		this.category = category;
		this.min = min;
		this.max = max;
	}
	public int getCategory() {
		return category;
	}
	public long getGroupId() {
		return groupId;
	}
	public int getId() {
		return id;
	}
	public double getMax() {
		return max;
	}
	public double getMin() {
		return min;
	}
	public int getType() {
		return type;
	}
	
	private String getTypeDescription(int type) {
		switch(type) {
			case ObservingConstraintTypes.SEEING_CONSTRAINT:
				return "SEEING_CONSTRAINT";

			case ObservingConstraintTypes.EXTINCTION_CONSTRAINT:
				return "EXTINCTION_CONSTRAINT";
				
			case ObservingConstraintTypes.AIRMASS_CONSTRAINT:
				return "AIRMASS_CONSTRAINT";
				
			case ObservingConstraintTypes.HOUR_ANGLE_CONSTRAINT:
				return "HOUR_ANGLE_CONSTRAINT";
				
			case ObservingConstraintTypes.SKY_BRIGHTNESS_CONSTRAINT:
				return "SKY_BRIGHTNESS_CONSTRAINT";
				
			case ObservingConstraintTypes.SOLAR_ELEVATION_CONSTRAINT:
				return "SOLAR_ELEVATION_CONSTRAINT";
				
			case ObservingConstraintTypes.LUNAR_DISTANCE_CONSTRAINT:
				return "LUNAR_DISTANCE_CONSTRAINT";
				
			case ObservingConstraintTypes.LUNAR_ELEVATION_CONSTRAINT:
				return "LUNAR_ELEVATION_CONSTRAINT";
				
			case ObservingConstraintTypes.LUNAR_PHASE_CONSTRAINT:
				return "LUNAR_PHASE_CONSTRAINT";
			
			default:
				return "UNKNOWN TYPE";
		}
	}
	
	/*
	public static final int CIVIL_TWILIGHT = 0;
	public static final int NAUTICAL_TWILIGHT = 1;
	public static final int ASTRONOMICAL_TWILIGHT = 2;
	public static final int NIGHT_TIME = 3;
	*/
	private String getCategoryDescription(int category) {
		switch(type) {
			case 0:
				return "CIVIL TWILIGHT";

			case 1:
				return "NAUTICAL TWILIGHT";
				
			case 2:
				return "ASTRONOMICAL TWILIGHT";
				
			case 3:
				return "NIGHT TIME";
			
			default:
				return "UNKNOWN CATEGORY";
		}
	}
	
	public String toString() {
		String s = "";
		s += this.getClass().getName() + "[groupId=" +groupId + ","; 
		s += " id=" +id + ","; 
		s += " type=" + getTypeDescription(this.type) + ","; 
		s += " category=" + getCategoryDescription(this.category) + ",";
		s += " min=" + min + ",";
		s += " max=" + max + "]";
		return s;
	}
}