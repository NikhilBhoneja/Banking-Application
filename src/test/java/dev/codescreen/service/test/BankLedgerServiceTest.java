package dev.codescreen.service.test;

import dev.codescreen.Repository.EventRepository;
import dev.codescreen.Repository.UserBalanceRepository;
import dev.codescreen.Service.BankLedgerService;
import dev.codescreen.exceptions.BankLedgerInternalException;
import dev.codescreen.exceptions.InsufficientFunds;
import dev.codescreen.model.entity.UserBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BankLedgerServiceTest {

    @Mock
    private UserBalanceRepository userBalanceRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BankLedgerService bankLedgerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadFunds_Success() {
        double amount = 100;
        String userId = "userId";
        UserBalance userBalance = new UserBalance(userId, 200);
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.of(userBalance));
        when(userBalanceRepository.save(any(UserBalance.class))).thenReturn(userBalance);

        double updatedBalance = bankLedgerService.loadFunds(userId, "messageId", amount);

        assertEquals(300, updatedBalance);
    }

    @Test
    void loadFunds_NewUser() {
        double amount = 100;
        String userId = "userId";
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.empty());

        double updatedBalance = bankLedgerService.loadFunds(userId, "messageId", amount);

        assertEquals(100, updatedBalance);
    }

    @Test
    void loadFunds_Exception() {
        double amount = 100;
        String userId = "userId";
        when(userBalanceRepository.findById(userId)).thenThrow(new RuntimeException("DB error"));

        assertThrows(BankLedgerInternalException.class, () -> bankLedgerService.loadFunds(userId, "messageId", amount));
    }

    @Test
    void authorizeTransaction_Success() {
        double amount = 50;
        String userId = "userId";
        UserBalance userBalance = new UserBalance(userId, 100);
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.of(userBalance));
        when(userBalanceRepository.save(any(UserBalance.class))).thenReturn(userBalance);

        double updatedBalance = bankLedgerService.authorizeTransaction(userId, "messageId", amount);

        assertEquals(50, updatedBalance);
    }

    @Test
    void authorizeTransaction_InsufficientFunds() {
        double amount = 200;
        String userId = "userId";
        UserBalance userBalance = new UserBalance(userId, 100);
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.of(userBalance));

        assertThrows(InsufficientFunds.class, () -> bankLedgerService.authorizeTransaction(userId, "messageId", amount));
    }

    @Test
    void authorizeTransaction_UserNotFound() {
        double amount = 100;
        String userId = "userId";
        when(userBalanceRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BankLedgerInternalException.class, () -> bankLedgerService.authorizeTransaction(userId, "messageId", amount));
    }

    @Test
    void authorizeTransaction_Exception() {
        double amount = 100;
        String userId = "userId";
        when(userBalanceRepository.findById(userId)).thenThrow(new RuntimeException("DB error"));

        assertThrows(BankLedgerInternalException.class, () -> bankLedgerService.authorizeTransaction(userId, "messageId", amount));
    }

}