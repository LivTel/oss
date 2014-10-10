package ngat.oss.impl.mysql.reference;

public interface ExecutiveActionElementTypes {
    
	//denoted by 'eaType' in SEQUENCE_COMPONENT table
	
    public static final int EXECUTIVE_TYPE_TARGET_SELECTOR = 1;
    
    public static final int EXECUTIVE_TYPE_INSTRUMENT_CONFIG_SELECTOR = 2;
    
    public static final int EXECUTIVE_TYPE_MOSAIC_OFFSET = 3;
    
    public static final int EXECUTIVE_TYPE_FOCUS_OFFSET = 4;
    
    public static final int EXECUTIVE_TYPE_ROTATOR_CONFIG = 5;
    
    public static final int EXECUTIVE_TYPE_AUTOGUIDER_CONFIG = 6;
    
    public static final int EXECUTIVE_TYPE_ACQUISITION_CONFIG = 7;
    
    public static final int EXECUTIVE_TYPE_EXPOSURE = 8;
    
    public static final int EXECUTIVE_TYPE_APERTURE_OFFSET = 9;
    
    public static final int EXECUTIVE_TYPE_CALIBRATION = 10;
    
    public static final int EXECUTIVE_TYPE_SLEW = 11;
    
    public static final int EXECUTIVE_TYPE_FOCUSCONTROL = 12;
    
    public static final int EXECUTIVE_TYPE_BEAMSTEERINGCONFIG = 13;
    
    public static final int EXECUTIVE_TYPE_OPTICALSLIDECONFIG = 14;
    
    public static final int EXECUTIVE_TYPE_TIPTILTABSOLUTEOFFSET = 15;
    
}

