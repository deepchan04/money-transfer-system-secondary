package com.training.mts.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendEmail_shouldSendSuccessfully() {
        MimeMessage mimeMessage =
                new MimeMessage((Session) null);

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        emailService.sendEmail(
                "test@test.com",
                "Subject",
                "<h1>Hello</h1>"
        );

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_shouldHandleException() {
        when(mailSender.createMimeMessage())
                .thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() ->
                emailService.sendEmail(
                        "test@test.com",
                        "Subject",
                        "Body"
                )
        );
    }
}
