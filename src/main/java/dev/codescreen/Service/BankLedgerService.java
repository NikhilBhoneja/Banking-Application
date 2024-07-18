package dev.codescreen.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.codescreen.Repository.EventRepository;
import dev.codescreen.Repository.UserBalanceRepository;
import dev.codescreen.exceptions.BankLedgerInternalException;
import dev.codescreen.exceptions.InsufficientFunds;
import dev.codescreen.model.DebitCredit;
import dev.codescreen.model.Event;
import dev.codescreen.model.entity.UserBalance;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BankLedgerService {
	@Autowired
	private UserBalanceRepository userBalanceRepository;
	@Autowired
	private EventRepository eventRepository;

	/**
	 * Add the Amount to User in Database Add the Event to EventsTable in Database
	 * 
	 * @param userId:    Unique Identifier of the Account
	 * @param messageId: MessageID for tracking the transaction/event
	 * @param amount:    Transaction Amount
	 * @return the updated balance
	 */
	
	
	@Transactional
	public double loadFunds(String userId, String messageId, double amount) {
		try {
			Optional<UserBalance> userBalanceIfPresent = userBalanceRepository.findById(userId);
			double updatedBalance = amount;
			if (userBalanceIfPresent.isPresent()) {
				UserBalance userBalance = userBalanceIfPresent.get();
				updatedBalance = userBalance.getBalance() + amount;
				userBalance.setBalance(updatedBalance);
				userBalance = userBalanceRepository.save(userBalance);
			} else {
				UserBalance userBalance = new UserBalance(userId, amount);
				userBalanceRepository.save(userBalance);
			}

			Event event = Event.builder().eventId(UUID.randomUUID()).auditTimestamp(LocalDateTime.now()).userId(userId)
					.amount(updatedBalance).transactionType(DebitCredit.CREDIT).transactionAmount(amount)
					.isSuccessIndicator(true).build();
			eventRepository.save(event);

			return updatedBalance;
		} catch (Exception ex) {
			String errorMessage = String.format("Unknown Exception Occurred while loading the balance %s for Userid %s",
					amount, userId);
			log.error(errorMessage, ex);
			throw new BankLedgerInternalException(errorMessage, ex);
		}
	}
	
	/**
	 * Deduct the Amount from User in Database and Add the Event to EventsTable in Database
	 * 
	 * @param userId:    Unique Identifier of the Account
	 * @param messageId: MessageID for tracking the transaction/event
	 * @param amount:    Transaction Amount
	 * @return the updated balance
	 */
	
	@Transactional(noRollbackFor = InsufficientFunds.class)
	public double authorizeTransaction(String userId, String messageId, double amount) {
		try {
			Optional<UserBalance> userBalanceIfPresent = userBalanceRepository.findById(userId);
			double updatedBalance = amount;
			if (userBalanceIfPresent.isPresent()) {
				UserBalance userBalance = userBalanceIfPresent.get();
				if (userBalance.getBalance() < amount) {
					Event failedEvent = Event.builder().eventId(UUID.randomUUID()).auditTimestamp(LocalDateTime.now())
							.userId(userId).amount(userBalance.getBalance()).transactionType(DebitCredit.DEBIT)
							.transactionAmount(amount).isSuccessIndicator(false).build();
					eventRepository.saveAndFlush(failedEvent);
					String errorMessage = String.format("Current Balance: %,.2f, Requested Funds: %,.2f",
							userBalance.getBalance(), amount);
					log.error(errorMessage);
					throw new InsufficientFunds(errorMessage, userBalance.getBalance());
				} else {
					updatedBalance = userBalance.getBalance() - amount;
					userBalance.setBalance(updatedBalance);
					userBalance = userBalanceRepository.save(userBalance);
					Event event = Event.builder().eventId(UUID.randomUUID()).auditTimestamp(LocalDateTime.now())
							.userId(userId).amount(updatedBalance).transactionType(DebitCredit.DEBIT)
							.transactionAmount(amount).isSuccessIndicator(true).build();
					eventRepository.save(event);
				}
			} else {
				String errorMessage = String.format("User %s is not found in the system", userId);
				log.error(errorMessage);
				throw new BankLedgerInternalException(errorMessage);
			}
			return updatedBalance;
		} catch (InsufficientFunds ex) {
			throw ex;
		} catch (Exception ex) {
			String errorMessage = String.format("Unknown Exception Occurred while loading the balance %s for Userid %s",
					amount, userId);
			log.error(errorMessage, ex);
			throw new BankLedgerInternalException(errorMessage, ex);
		}
	}

}
