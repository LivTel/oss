package ngat.oss.exception;

import ngat.phase2.ILock;

public class ObjectLockedException extends Phase2Exception {

	private ILock lock;
	
	public ObjectLockedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ObjectLockedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ObjectLockedException(int code, Exception e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

	public ObjectLockedException(ILock lock) {
	super();
	this.lock = lock;
	}
	
	public String toString() {
		return "ObjectLockedException: "+lock+": "+getMessage();
	}
}
