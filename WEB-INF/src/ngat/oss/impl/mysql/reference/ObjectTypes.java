/**
 * 
 */
package ngat.oss.impl.mysql.reference;

/**Constants to determine lock object types.
 * @author snf
 *
 */
public class ObjectTypes {
	
	/** Lock type representing a TAG.*/
	public static final int TAG_TYPE = 1;
	
	/** Lock type representing a User.*/
	public static final int USER_TYPE= 2;
	
	/** Lock type representing a Proposal.*/
	public static final int PROPOSAL_TYPE = 3;
	
	/** Lock type representing a Group.*/
	public static final int GROUP_TYPE = 4;
	
	/** TYPE type representing an Observation.*/
	public static final int OBSERVATION_TYPE = 5;
	
	/** TYPE type representing a Target.*/
	public static final int TARGET_TYPE = 6;
	
	/** TYPE type representing a Config.*/
	public static final int CONFIG_TYPE = 7;
	
	public static String getObjectTypeAsName(int objectID) {
		String objectName = "UNKNOWN";
		
		switch (objectID) {
		case TAG_TYPE:
			objectName = "TAG";
			break;
		case USER_TYPE:
			objectName = "USER";
			break;
		case PROPOSAL_TYPE:
			objectName = "PROPOSAL";
			break;
		case GROUP_TYPE:
			objectName = "GROUP";
			break;
		case OBSERVATION_TYPE:
			objectName = "OBSERVATION_SEQUENCE";
			break;
		case TARGET_TYPE:
			objectName = "TARGET";
			break;
		case CONFIG_TYPE:
			objectName = "INSTRUMENT_CONFIG";
			break;
		}
		return objectName;
	}
	
}
