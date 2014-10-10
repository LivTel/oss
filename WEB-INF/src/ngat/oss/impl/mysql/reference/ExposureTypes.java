package  ngat.oss.impl.mysql.reference;

public interface ExposureTypes {

    public static final int MULT_RUN = 1;				//standard mult-run
    public static final int PERIOD = 2;					//period exposure (no repeat count)
    public static final int PERIOD_RUN_AT = 3;		//period run-at exposure (i.e. expose for a specified period at a specified time)
}
