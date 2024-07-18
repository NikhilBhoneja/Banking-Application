package dev.codescreen.model.response;

import dev.codescreen.model.Amount;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadResponse {
	private String userId;
	private String messageId;
	private Amount balance;
}
