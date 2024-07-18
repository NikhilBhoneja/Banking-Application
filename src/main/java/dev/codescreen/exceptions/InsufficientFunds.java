package dev.codescreen.exceptions;

import lombok.Getter;

@Getter
public class InsufficientFunds extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private double userBalance;

	public InsufficientFunds(String errorMessage, double userBalance) {
		super(errorMessage);
		this.userBalance = userBalance;
	}

}
