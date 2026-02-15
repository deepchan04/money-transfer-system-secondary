package com.training.mts.controller;

import com.training.mts.dto.TransactionDTO;
import com.training.mts.dto.TransactionHistoryResponse;
import com.training.mts.dto.TransactionRequest;
import com.training.mts.service.AnalyticsService;
import com.training.mts.service.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionServiceImpl transactionService;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void testInitiatePayment_success() throws Exception {

        TransactionRequest request = new TransactionRequest();
        request.setAmount(1000.0);

        TransactionDTO dto = mock(TransactionDTO.class);

        when(transactionService.initiatePayment(request))
                .thenReturn(dto);

        ResponseEntity<TransactionDTO> response =
                transactionController.initiatePayment(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(transactionService).initiatePayment(request);
        verify(analyticsService).triggerSnowflakeSync();
    }

    @Test
    void testGetTransLog_success() {

        TransactionHistoryResponse history =
                new TransactionHistoryResponse(List.of(), List.of());

        when(transactionService.getTransactionHistory("abc@upi"))
                .thenReturn(history);

        ResponseEntity<TransactionHistoryResponse> response =
                transactionController.getTransLog("abc@upi");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(history, response.getBody());
    }
}
