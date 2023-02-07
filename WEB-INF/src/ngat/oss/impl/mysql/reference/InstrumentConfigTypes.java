/**
 * 
 */
package ngat.oss.impl.mysql.reference;

/** Constants to determine instrument config types.
 * @author nrc
 *
 */
public interface InstrumentConfigTypes {

	/** Instrument config type represented by CCD attributes. */
	public static final int CCD = 1;
	
	/** Instrument config type represented by Spectrograph attributes. */
	public static final int IMAGING_SPECTROGRAPH = 2;

    /** Instrument config type represented Frodo-Spec attributes. */
	public static final int FRODO = 3;
	
	/** Instrument config type represented Polarimeter attributes. */
	public static final int POLAR = 4;
	
	/** Instrument config type represented Tip Tilt attributes. */
	public static final int TIP_TILT = 5;
	
	/** Instrument config type represented two-slit spectrograph attributes. */
	public static final int TWO_SLIT_SPECTROGRAPH = 6;
	
	/** Instrument config type representing the MOPTOP polarimeter. (Multi-colour optimised optical polarimeter). */
	public static final int MOPTOP = 7;
	
	/** Instrument config type representing the RAPTOR infra-red imager. */
	public static final int RAPTOR = 8;
}
