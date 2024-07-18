package dev.codescreen.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.codescreen.Service.BankLedgerService;
import dev.codescreen.controller.BankController;
import dev.codescreen.model.Amount;
import dev.codescreen.model.DebitCredit;
import dev.codescreen.model.request.AuthorizationRequest;
import dev.codescreen.model.request.LoadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankController.class)
@AutoConfigureMockMvc
public class BankControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankLedgerService ledgerService;

    @Test
    public void testAuthorizeTransaction() throws Exception {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest("user123", "msg123",
                new Amount("100", "USD", DebitCredit.DEBIT));

        when(ledgerService.authorizeTransaction(anyString(), anyString(), anyDouble()))
                .thenReturn(100.0);

        mockMvc.perform(MockMvcRequestBuilders.put("/authorization")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(authorizationRequest)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("user123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageId").value("msg123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("100.00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.debitOrCredit").value("DEBIT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("APPROVED"));
    }

    @Test
    public void testLoadFunds() throws Exception {
        LoadRequest loadRequest = new LoadRequest("user123", "msg123",
                new Amount("100", "USD", DebitCredit.CREDIT));

        when(ledgerService.loadFunds(anyString(), anyString(), anyDouble()))
                .thenReturn(100.0);

        mockMvc.perform(MockMvcRequestBuilders.put("/load")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loadRequest)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("user123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageId").value("msg123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.amount").value("100.00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.currency").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balance.debitOrCredit").value("CREDIT"));
    }

    // Utility method to convert object to JSON string
    private String asJsonString(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}