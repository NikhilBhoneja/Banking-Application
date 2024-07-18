package dev.codescreen.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amount {
    @NotNull(message="Amount cannot be empty")
    @Pattern(regexp = ".*\\S.*", message = "Amount must not contain whitespace and should have at least one character")
	private String amount;
	
	@NotNull(message="Currency cannot be empty")
	@Pattern(regexp = ".*\\S.*", message = "Currency must not contain whitespace and should have at least one character")
	private String currency;
	
	@NotNull(message="Transation type cannot be empty")
	private DebitCredit debitOrCredit;
}
