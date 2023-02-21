package projects.exception;

@SuppressWarnings("serial")
public class DbException extends RuntimeException {

	/**
	 * Creating an exception class because JDBC API classes throw checked
	 * SQLException objects. Using this class we will turn those into
	 * unchecked exceptions to keep code clean and throw them to where we 
	 * handle them. 
	 */
	
	public DbException() {
		
	}
	
	public DbException(String message) {
		super(message);
		
	}

	public DbException(Throwable cause) {
		super(cause);
		
	}

	public DbException(String message, Throwable cause) {
		super(message, cause);
		
	}

	
}
