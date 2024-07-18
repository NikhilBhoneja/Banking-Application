package dev.codescreen.model;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "BANK_EVENTS")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
	@Id
	@Column(name = "EVENT_ID")
	private UUID eventId;
	
	@Column(name = "AUDIT_TIMESTAMP")
	private LocalDateTime auditTimestamp;
	
	@Column(name = "USER_ID")
	private String userId;
	
	@Column(name = "AMOUNT")
	private double amount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TRANSACTION_TYPE")
	private DebitCredit transactionType;
	
	@Column(name = "TRANSACTION_AMOUNT")
	private double transactionAmount;
	
	@Column(name = "IS_SUCCESS_INDICATOR")
	private boolean isSuccessIndicator;
}
