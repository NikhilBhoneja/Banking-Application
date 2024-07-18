package dev.codescreen.controller;

import dev.codescreen.Service.BankLedgerService;
import dev.codescreen.exceptions.BankLedgerInternalException;
import dev.codescreen.exceptions.InsufficientFunds;
import dev.codescreen.model.Amount;
import dev.codescreen.model.DebitCredit;
import dev.codescreen.model.request.AuthorizationRequest;
import dev.codescreen.model.request.LoadRequest;
import dev.codescreen.model.response.AuthorizationResponse;
import dev.codescreen.model.response.LoadResponse;
import dev.codescreen.model.response.ResponseCode;
import dev.codescreen.model.response.ServerError;
import dev.codescreen.utils.Utils;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BankController {
	@Autowired
	private BankLedgerService ledgerService;

	public BankController(BankLedgerService ledgerService) {
		this.ledgerService = ledgerService;
	}
	
	// This method handles "/authorization" end-point and it is used for DEBIT transaction.
	
	@PutMapping(path = "/authorization", consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> authorizeTransaction(@Valid @RequestBody AuthorizationRequest authRequest,
			BindingResult bindingResult) {
		log.info("A New Authorization Request received {}", authRequest);
		String errorCode = "";
		String errorMessage = "";

		if (bindingResult.hasErrors()) {
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				errorMessage += fieldError.getDefaultMessage() + "\n";
			}
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		}

		Amount transactionAmount = authRequest.getTransactionAmount();
		// Load API should always be used for DEBIT
		if (!transactionAmount.getDebitOrCredit().equals(DebitCredit.DEBIT)) {
			return new ResponseEntity<>("Authorization API should always have DEBIT Amount",
					HttpStatus.BAD_REQUEST);
		}
		
		try {
			
			double amountInUSD = Utils.convertAmountToUSD(Utils.parseAmount(transactionAmount.getAmount()),
					transactionAmount.getCurrency());
			double updatedBalance = ledgerService.authorizeTransaction(authRequest.getUserId(),
					authRequest.getMessageId(), amountInUSD);
			AuthorizationResponse response = AuthorizationResponse.builder().userId(authRequest.getUserId())
					.messageId(authRequest.getMessageId())
					.balance(Amount.builder().currency("USD").debitOrCredit(DebitCredit.DEBIT)
							.amount(Utils.formatAmount(updatedBalance)).build())
					.responseCode(ResponseCode.APPROVED).build();
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (InsufficientFunds ex) {
			AuthorizationResponse response = AuthorizationResponse.builder().userId(authRequest.getUserId())
					.messageId(authRequest.getMessageId())
					.balance(Amount.builder().currency("USD").debitOrCredit(DebitCredit.DEBIT)
							.amount(Utils.formatAmount(ex.getUserBalance())).build())
					.responseCode(ResponseCode.DECLINED).build();
			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (NumberFormatException ex) {
			errorMessage = "Please provide valid Input for amount";
			log.error(errorMessage);
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		} catch (BankLedgerInternalException ex) {
			errorMessage = "Failed to Load the Balance.";
			errorCode = "INTERNAL_EXCEPTION";
			log.error(errorMessage, ex);
		} catch (Exception ex) {
			errorMessage = "Unknown Exception occurred while loading the balance";
			errorCode = "UNKNOWN_EXCEPTION";
			log.error(errorMessage, ex);
		}
		ServerError serverError = ServerError.builder()
				.error(dev.codescreen.model.Error.builder().code(errorCode).message(errorMessage).build()).build();
		return new ResponseEntity<>(serverError, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// This method handles "/load" end-point and it is used for CREDIT transaction.
	
	@PutMapping(path = "/load", consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> loadFunds(@Valid @RequestBody LoadRequest loadRequest, BindingResult bindingResult) {
		log.info("A New Load Request received {}", loadRequest);
		String errorCode = "";
		String errorMessage = "";

		if (bindingResult.hasErrors()) {
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				errorMessage += fieldError.getDefaultMessage() + "\n";
			}
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		}

		Amount transactionAmount = loadRequest.getTransactionAmount();
		
		// Load API should always be used for Credit
		if (!transactionAmount.getDebitOrCredit().equals(DebitCredit.CREDIT)) {
			return new ResponseEntity<>("Load API should always have CREDIT Amount", HttpStatus.BAD_REQUEST);
		}

		try {
			double amountInUSD = Utils.convertAmountToUSD(Utils.parseAmount(transactionAmount.getAmount()),
					transactionAmount.getCurrency());
			double updatedBalance = ledgerService.loadFunds(loadRequest.getUserId(), loadRequest.getMessageId(),
					amountInUSD);
			LoadResponse loadResponse = LoadResponse.builder().userId(loadRequest.getUserId())
					.messageId(loadRequest.getMessageId()).balance(Amount.builder().currency("USD")
							.debitOrCredit(DebitCredit.CREDIT).amount(Utils.formatAmount(updatedBalance)).build())
					.build();
			return new ResponseEntity<>(loadResponse, HttpStatus.CREATED);
		} catch (NumberFormatException ex) {
			errorMessage = "Please provide valid Input for amount";
			log.error(errorMessage);
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		} catch (BankLedgerInternalException ex) {
			errorMessage = "Failed to Load the Balance.";
			errorCode = "INTERNAL_EXCEPTION";
			log.error(errorMessage, ex);
		} catch (Exception ex) {
			errorMessage = "Unknown Exception occurred while loading the balance";
			errorCode = "UNKNOWN_EXCEPTION";
			log.error(errorMessage, ex);
		}
		ServerError serverError = ServerError.builder()
				.error(dev.codescreen.model.Error.builder().code(errorCode).message(errorMessage).build()).build();
		return new ResponseEntity<>(serverError, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
