package ngat.oss.impl.mysql.reference;

/** Constants to determine orbital elements types.*/
public interface OrbitalElementsTypes {
	
	/** Element type representing a major planet in SLALIB.*/
    public static final int SLA_MAJOR_PLANET_ELEMENTS = 1;
    
    /** Element type representing a minor planet in SLALIB.*/
    public static final int SLA_MINOR_PLANET_ELEMENTS = 2;
    
    /** Element type representing a comet in SLALIB.*/
    public static final int SLA_COMET_ELEMENTS = 3;
}
