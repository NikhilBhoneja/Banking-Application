package dev.codescreen.utils;

public class Utils {
	
	public static double convertAmountToUSD(double amount, String sourceCurrency) {
		return amount;
	}
	
	public static double parseAmount(String amountInString) {
		return Double.parseDouble(amountInString);
	}
	public static String formatAmount(double amount) {
		return String.format("%,.2f", amount);
	}
	
}
