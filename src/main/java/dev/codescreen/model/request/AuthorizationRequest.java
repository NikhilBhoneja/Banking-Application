package dev.codescreen.model.request;

import dev.codescreen.model.Amount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class AuthorizationRequest {
	@NotNull(message="UserId cannot be empty")
	@Pattern(regexp = ".*\\S.*", message = "UserId must not contain whitespace and should have at least one character")
    private String userId;
	
	@NotNull(message="MessageId cannot be empty")
	@Pattern(regexp = ".*\\S.*", message = "Message Id must not contain whitespace and should have at least one character")
	private String messageId;
	
	@NotNull(message="TransactionAmount is a mandatory parameter, cannot be empty.")
	@Valid
	private Amount transactionAmount;

}
