/**
 * 
 */
package ngat.oss.impl.mysql.reference;

/** Constants to determine target types.
 * @author snf
 *
 */
public interface TargetTypes {

	/** Target type represented by a set of orbital elements of SLA Jform = 1 (major planet).*/
	public static final int SLA_MAJOR_PLANET_ELEMENTS = 1;
	
	/** Target type represented by a set of orbital elements of SLA Jform = 2 (minor planet).*/
	public static final int SLA_MINOR_PLANET_ELEMENTS = 2;
	
	/** Target type represented by a set of orbital elements of SLA Jform = 3 (comet)*/
	public static final int SLA_COMET_ELEMENTS = 3;
	
	/** Target type representing a stellar or galactic target.*/
	public static final int EXTRA_SOLAR_TARGET  = 4;
	
	/** Target type represented by a SLA catalog index.*/
	public static final int SLA_CATALOG_TARGET = 5;
	
	/** Target type represented by an ephemeris table*/
	public static final int EPHEMERIS_TARGET = 6;
	
	/** Target type representing Selenographic coordinates.*/
	public static final int SELENOGRAPHIC_TARGET = 7;
	
	/** Target type representing Planetocentric coordinates.*/
	public static final int PLANETOCENTRIC_TARGET = 8;
	
}
