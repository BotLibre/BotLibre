package org.botlibre.api.sense;

/**
 * Thrown if the sense does not support input.
 * i.e. output only, such as voice.
 */

public class InputNotSupported extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public InputNotSupported() {
		super();		
	}
	
	public InputNotSupported(String message) {
		super(message);		
	}

}

