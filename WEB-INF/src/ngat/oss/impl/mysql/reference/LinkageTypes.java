package ngat.oss.impl.mysql.reference;

/** Constants to determine  linkage types.*/
public class LinkageTypes {
	/** Relationship where A can be done (now) if B can be done in the future within limits with confidence.*/
    public static final int A_ONLY_IF_B = 1;

    /** Relationship where If A was done within limits then B can be done (now).*/
    public static final int IF_A_THEN_B = 2;

}
