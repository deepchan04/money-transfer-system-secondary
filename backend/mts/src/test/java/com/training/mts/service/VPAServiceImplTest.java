package com.training.mts.service;

import com.training.mts.exceptions.InsufficientUserDataException;
import com.training.mts.exceptions.VPAIdNotFoundException;
import com.training.mts.model.User;
import com.training.mts.model.VPA;
import com.training.mts.repository.VPARepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VPAServiceImplTest {

    @Mock
    private VPARepository vpaRepository;

    @InjectMocks
    private VPAServiceImpl vpaService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        user.setEmail("diya@gmail.com");
        user.setPhoneNumber("9999999999");
    }
    @Test
    void generateVPAId_success() {

        String vpa = vpaService.generateVPAId(user);

        assertEquals("diya999910@mts", vpa);
    }
    @Test
    void generateVPAId_nullEmail() {

        user.setEmail(null);

        assertThrows(InsufficientUserDataException.class, () ->
                vpaService.generateVPAId(user));
    }
    @Test
    void generateVPAId_emptyEmail() {

        user.setEmail("");

        assertThrows(InsufficientUserDataException.class, () ->
                vpaService.generateVPAId(user));
    }
    @Test
    void generateVPAId_nullPhone() {

        user.setPhoneNumber(null);

        assertThrows(InsufficientUserDataException.class, () ->
                vpaService.generateVPAId(user));
    }
    @Test
    void getVPA_success() {

        VPA vpa = new VPA();
        vpa.setVpaId("diya999910@mts");

        when(vpaRepository.findByVpaId("diya999910@mts"))
                .thenReturn(Optional.of(vpa));

        VPA result = vpaService.getVPA("diya999910@mts");

        assertEquals(vpa, result);
    }
    @Test
    void getVPA_notFound() {

        when(vpaRepository.findByVpaId("invalid@mts"))
                .thenReturn(Optional.empty());

        assertThrows(VPAIdNotFoundException.class, () ->
                vpaService.getVPA("invalid@mts"));
    }

}