package com.mattkantor.usernamer;

/**
 * Structured result for a user submission.
 * @author mkantor
 */
public class UserSubmissionResult {
	public enum Status {
		SUCCESS, CONFLICT, ERROR
	}
	public Status status;
	public String message;

	public UserSubmissionResult(Status status, String message) {
		this.status = status;
		this.message = message;
	}
}