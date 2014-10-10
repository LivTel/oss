package ngat.oss.impl.mysql.reference;

/** Constants to determine group timing constraint types.*/
public interface GroupTypes {
	
	/** Constant representing flexible timing constraint.*/
    public static final int FLEXIBLE_GROUP = 1;
    
    /** Constant representing monitoring timing constraint.*/
    public static final int MONITOR_GROUP = 2;
    
    /** Constant representing minimum-interval monitoring timing constraint.*/
    public static final int INTERVAL_GROUP = 3;
    
    /** Constant representing ephemeris timing constraint.*/
    public static final int EPHEMERIS_GROUP = 4;
    
    /** Constant representing a fixed timing constraint.*/
    public static final int FIXED_GROUP = 5;

}
