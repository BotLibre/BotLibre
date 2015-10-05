package org.botlibre.api.sense;

/**
 * Thrown if the sense does not support output.
 * i.e. input only, such as hearing.
 */

public class OutputNotSupported extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public OutputNotSupported() {
		super();		
	}
	
	public OutputNotSupported(String message) {
		super(message);		
	}

}

