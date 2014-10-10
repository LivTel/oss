package ngat.oss.exception;


/**
 * @author snf
 *
 */
public class NoSuchObjectException extends Phase2Exception {

	/** The iD for which no such object exists.*/
	private long id;
	
	/**
	 * @param id
	 */
	public NoSuchObjectException(long id) {
		super();
		this.id = id;
	}

	/**
	 * @param message
	 */
	public NoSuchObjectException(String message, long id) {
		super(message);
		this.id = id;
	}


	/**
	 * @return Returns the id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(long id) {
		this.id = id;
	}

	public String toString() {
			return super.toString()+" : ID="+id;
	}
}
