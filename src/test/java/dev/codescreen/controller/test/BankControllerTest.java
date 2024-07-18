package dev.codescreen.controller.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import dev.codescreen.Service.BankLedgerService;
import dev.codescreen.controller.BankController;
import dev.codescreen.exceptions.BankLedgerInternalException;
import dev.codescreen.exceptions.InsufficientFunds;
import dev.codescreen.model.Amount;
import dev.codescreen.model.DebitCredit;
import dev.codescreen.model.request.AuthorizationRequest;
import dev.codescreen.model.request.LoadRequest;
import dev.codescreen.model.response.AuthorizationResponse;
import dev.codescreen.model.response.LoadResponse;
import dev.codescreen.model.response.ServerError;

class BankControllerTest {

    @Mock
    private BankLedgerService ledgerService;

    @InjectMocks
    private BankController bankController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

  @Test
    void authorizeTransaction_Success() {
        AuthorizationRequest authRequest = new AuthorizationRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.DEBIT));
        BindingResult bindingResult = mock(BindingResult.class);
        double updatedBalance = 500;
        when(ledgerService.authorizeTransaction(anyString(), anyString(), anyDouble())).thenReturn(updatedBalance);
        
        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest,bindingResult);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        AuthorizationResponse responseBody = (AuthorizationResponse) responseEntity.getBody();
        assertEquals("userId", responseBody.getUserId());
        assertEquals("messageId", responseBody.getMessageId());
        assertEquals(DebitCredit.DEBIT, responseBody.getBalance().getDebitOrCredit());
        assertEquals("USD", responseBody.getBalance().getCurrency());
        assertEquals("500.00", responseBody.getBalance().getAmount());
    }
  	
    @Test
    void authorizeTransaction_InsufficientFunds() {
    	BindingResult bindingResult = mock(BindingResult.class);
        AuthorizationRequest authRequest = new AuthorizationRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.DEBIT));
        when(ledgerService.authorizeTransaction(anyString(), anyString(), anyDouble())).thenThrow(new InsufficientFunds("Insufficient funds",100.00));
        
        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest,bindingResult);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        AuthorizationResponse responseBody = (AuthorizationResponse) responseEntity.getBody();
        assertEquals("userId", responseBody.getUserId());
        assertEquals("messageId", responseBody.getMessageId());
        assertEquals(DebitCredit.DEBIT, responseBody.getBalance().getDebitOrCredit());
        assertEquals("USD", responseBody.getBalance().getCurrency());
    }

    @Test
    void authorizeTransaction_InvalidInput() {
    	BindingResult bindingResult = mock(BindingResult.class);
        AuthorizationRequest authRequest = new AuthorizationRequest("userId", "messageId", new Amount("somestring", "USD", DebitCredit.DEBIT));
        when(ledgerService.authorizeTransaction(anyString(), anyString(), anyDouble())).thenThrow(new NumberFormatException("Invalid input"));

        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest,bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Please provide valid Input for amount", responseEntity.getBody());
        }
    @Test
    void authorizeTransaction_InvalidTransactionType() {
    	BindingResult bindingResult = mock(BindingResult.class);
    	AuthorizationRequest authRequest = new AuthorizationRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.CREDIT));
    	
    	BankController bankController = mock(BankController.class);
    	when(bankController.authorizeTransaction(authRequest,bindingResult)).thenCallRealMethod();
        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest,bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Authorization API should always have DEBIT Amount", responseEntity.getBody());
    }

    @Test
    void authorizeTransaction_InternalException() {
    	BindingResult bindingResult = mock(BindingResult.class);
        AuthorizationRequest authRequest = new AuthorizationRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.DEBIT));
        when(ledgerService.authorizeTransaction(anyString(), anyString(), anyDouble())).thenThrow(new BankLedgerInternalException("Internal error"));

        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest,bindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
 
    }
    @Test
    void authorizeTransaction_WithInvalidRequest_ReturnsBadRequestError() {
    
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("authRequest", "userId", "User ID is required")));

        AuthorizationRequest authRequest = new AuthorizationRequest("", "", new Amount("", "USD", DebitCredit.CREDIT));

        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("User ID is required\n", responseEntity.getBody());
    }
    
    @Test
    void authorizeTransaction_NullRequest() throws Exception  { 
    	BindingResult bindingResult = mock(BindingResult.class);
    	AuthorizationRequest authRequest = new AuthorizationRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.DEBIT));

        when(bankController.authorizeTransaction(authRequest,bindingResult)).thenThrow(new RuntimeException("Simulated RuntimeException"));

        ResponseEntity<?> responseEntity = bankController.authorizeTransaction(authRequest,bindingResult);

        ServerError serverError = ServerError.builder().error(
    			dev.codescreen.model.Error.builder()
    			.code("UNKNOWN_EXCEPTION")
    			.message("Unknown Exception occurred while loading the balance")
    			.build()
    			).build();
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(serverError, responseEntity.getBody());
    }
    
    @Test
    void loadFunds_WithInvalidRequest_ReturnsBadRequestError() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("loadRequest", "userId", "User ID is required")));

        LoadRequest loadRequest = new LoadRequest("", "", new Amount("", "USD", DebitCredit.CREDIT));

        ResponseEntity<?> responseEntity = bankController.loadFunds(loadRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("User ID is required\n", responseEntity.getBody());
    }
 

    @Test
    void loadFunds_Success() {
    	BindingResult bindingResult = mock(BindingResult.class);
        LoadRequest loadRequest = new LoadRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.CREDIT));
        double updatedBalance = 500;
        when(ledgerService.loadFunds(anyString(), anyString(), anyDouble())).thenReturn(updatedBalance);

        ResponseEntity<?> responseEntity = bankController.loadFunds(loadRequest,bindingResult);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        LoadResponse responseBody = (LoadResponse) responseEntity.getBody();
        assertEquals("userId", responseBody.getUserId());
        assertEquals("messageId", responseBody.getMessageId());
        assertEquals(DebitCredit.CREDIT, responseBody.getBalance().getDebitOrCredit());
        assertEquals("USD", responseBody.getBalance().getCurrency());
        assertEquals("500.00", responseBody.getBalance().getAmount());
    }

    @Test
    void loadFunds_InvalidInput() {
    	BindingResult bindingResult = mock(BindingResult.class);
        LoadRequest loadRequest = new LoadRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.CREDIT));
        when(ledgerService.loadFunds(anyString(), anyString(), anyDouble())).thenThrow(new NumberFormatException("Invalid input"));

        ResponseEntity<?> responseEntity = bankController.loadFunds(loadRequest,bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Please provide valid Input for amount", responseEntity.getBody());
    }
    
    @Test
    void loadFunds_InvalidTransactionType() {
    	BindingResult bindingResult = mock(BindingResult.class);
    	LoadRequest loadRequest = new LoadRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.DEBIT));
       
    	BankController bankController = mock(BankController.class);
    	when(bankController.loadFunds(loadRequest,bindingResult)).thenCallRealMethod();
        ResponseEntity<?> responseEntity = bankController.loadFunds(loadRequest,bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Load API should always have CREDIT Amount", responseEntity.getBody());
    }

    @Test
    void loadFunds_InternalException() {
    	BindingResult bindingResult = mock(BindingResult.class);
        LoadRequest loadRequest = new LoadRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.CREDIT));
        when(ledgerService.loadFunds(anyString(), anyString(), anyDouble())).thenThrow(new BankLedgerInternalException("Internal error"));

        ResponseEntity<?> responseEntity = bankController.loadFunds(loadRequest,bindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void loadFunds_NullRequest() throws Exception  { 
    	BindingResult bindingResult = mock(BindingResult.class);
    	LoadRequest loadRequest = new LoadRequest("userId", "messageId", new Amount("100", "USD", DebitCredit.CREDIT));

        when(bankController.loadFunds(loadRequest,bindingResult)).thenThrow(new RuntimeException("Simulated RuntimeException"));

        ResponseEntity<?> responseEntity = bankController.loadFunds(loadRequest,bindingResult);

        ServerError serverError = ServerError.builder().error(
    			dev.codescreen.model.Error.builder()
    			.code("UNKNOWN_EXCEPTION")
    			.message("Unknown Exception occurred while loading the balance")
    			.build()
    			).build();
       
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(serverError, responseEntity.getBody());
    }

}