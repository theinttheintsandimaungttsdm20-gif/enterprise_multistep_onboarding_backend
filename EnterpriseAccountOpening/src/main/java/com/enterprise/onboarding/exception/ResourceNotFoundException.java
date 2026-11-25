package com.enterprise.onboarding.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 858783297657147711L;

	public ResourceNotFoundException(String message) { super(message); }
}
