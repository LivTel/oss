/**
 * 
 */
package ngat.oss.impl.mysql.reference;

/** Constants to determine observing constraint types.
 * @author snf
 *
 */
public interface ObservingConstraintTypes {

	// Observing constraint type representing  the minimum acceptable seeing (arcsec).
	public static final int SEEING_CONSTRAINT = 1;
	
	// Observing constraint type representing the maximum acceptable extinction (mag/sqdeg).
	public static final int EXTINCTION_CONSTRAINT = 2;
	
	// Observing constraint type representing the maximum acceptable airmass (dimensionless).
	public static final int AIRMASS_CONSTRAINT = 3;
	
	// Observing constraint type representing the acceptable range of hour angles (rads).
	public static final int HOUR_ANGLE_CONSTRAINT = 4;
	
	// Observing constraint type representing the maximum acceptable sky brightness (units?).
	public static final int SKY_BRIGHTNESS_CONSTRAINT = 6;
	

	/***********************************************************************************************************************/
	// NO LONGER USED FROM JUNE 2012
	
	// Observing constraint type representing  the maximum acceptable solar elevation (rads below horizon).
	public static final int SOLAR_ELEVATION_CONSTRAINT = 5;
	
	// Observing constraint type representing  the minimum acceptable lunar-target distance (rads).
	public static final int LUNAR_DISTANCE_CONSTRAINT = 7;
	
	// Observing constraint type representing  the maximum acceptable lunar  elevation (rads above horizon).
	public static final int LUNAR_ELEVATION_CONSTRAINT = 8;
	
	// Observing constraint type representing  the maximum acceptable lunar phase (fullness of moon).
	public static final int LUNAR_PHASE_CONSTRAINT = 9;
	
}
