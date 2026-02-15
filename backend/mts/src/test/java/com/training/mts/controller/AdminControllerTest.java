package com.training.mts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.mts.dto.ChangeStatusRequest;
import com.training.mts.enums.AppStatus;
import com.training.mts.model.Transaction;
import com.training.mts.model.User;
import com.training.mts.service.AdminServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AdminServiceImpl adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getTransLog_shouldReturnTransactions() throws Exception {

        Transaction transaction = new Transaction();
        transaction.setAmount(100.0);

        given(adminService.getTransactionHistory())
                .willReturn(List.of(transaction));

        mockMvc.perform(get("/admin/gettranslog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.0));

        verify(adminService).getTransactionHistory();
    }

    @Test
    void getUsers_shouldReturnUsers() throws Exception {

        User user = new User();
        user.setName("John");

        given(adminService.findAll())
                .willReturn(List.of(user));

        mockMvc.perform(get("/admin/getusers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"));

        verify(adminService).findAll();
    }

    @Test
    void changeStatus_shouldUpdateUser() throws Exception {

        ChangeStatusRequest request = new ChangeStatusRequest();
        request.setVpaId("test@upi");
        request.setAppStatus(AppStatus.ACTIVE);

        User updatedUser = new User();
        updatedUser.setAppStatus(AppStatus.ACTIVE);

        given(adminService.updateUser("test@upi", AppStatus.ACTIVE))
                .willReturn(updatedUser);

        mockMvc.perform(put("/admin/changeStatus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(adminService)
                .updateUser("test@upi", AppStatus.ACTIVE);
    }
}
