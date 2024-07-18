package dev.codescreen.service.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import dev.codescreen.Repository.EventRepository;
import dev.codescreen.Repository.UserBalanceRepository;
import dev.codescreen.Service.BankLedgerService;
import dev.codescreen.exceptions.InsufficientFunds;
import dev.codescreen.model.entity.UserBalance;

@SpringBootTest
@Transactional
public class BankServiceIntegrationTest {

    @Autowired
    private BankLedgerService bankLedgerService;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private EventRepository eventRepository;
    
    @Test
    public void testLoadFunds() {
        // Given
        String userId = "user123";
        double initialBalance = 500.0;
        double amountToAdd = 100.0;

        // Create a user with initial balance
        userBalanceRepository.save(new UserBalance(userId, initialBalance));

        // When
        double updatedBalance = bankLedgerService.loadFunds(userId, "msg123", amountToAdd);

        // Then
        assertEquals(initialBalance + amountToAdd, updatedBalance);

        // Check if the event was recorded
        assertEquals(1, eventRepository.count());
    }

    @Test
    public void testAuthorizeTransaction() {
        // Given
        String userId = "user123";
        double initialBalance = 500.0;
        double transactionAmount = 100.0;

        // Create a user with initial balance
        userBalanceRepository.save(new UserBalance(userId, initialBalance));

        // When
        double updatedBalance = bankLedgerService.authorizeTransaction(userId, "msg123", transactionAmount);

        // Then
        assertEquals(initialBalance - transactionAmount, updatedBalance);

        // Check if the event was recorded
        assertEquals(1, eventRepository.count());
    }

    @Test
    public void testAuthorizeTransactionInsufficientFunds() {
        // Given
        String userId = "user123";
        double initialBalance = 50.0;
        double transactionAmount = 100.0;

        // Create a user with initial balance
        userBalanceRepository.save(new UserBalance(userId, initialBalance));

        // When
        // Then
        assertThrows(InsufficientFunds.class, () -> {
            bankLedgerService.authorizeTransaction(userId, "msg123", transactionAmount);
        });

        // Check if the event was recorded
        assertEquals(1, eventRepository.count());
    }
}
