package dev.codescreen.exceptions;

public class BankLedgerInternalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BankLedgerInternalException(String errorMessage) {
		super(errorMessage);
	}
	public BankLedgerInternalException(String errorMessage, Throwable exception) {
		super(errorMessage, exception);
	}
}
