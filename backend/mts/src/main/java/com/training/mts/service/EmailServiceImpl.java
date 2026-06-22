package com.training.mts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;


@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Async
    public void sendEmail(String to, String subject, String htmlBody) {

        try {
            // 1. Create a MimeMessage
            MimeMessage message = mailSender.createMimeMessage();

            // 2. Use MimeMessageHelper to configure the message
            // The boolean 'true' indicates you want a multipart message (required for attachments/inline images)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            // 3. Set the body and set the second argument 'html' to true
            helper.setText(htmlBody, true);

            // 4. Send the email
            mailSender.send(message);
        } catch (Exception e) {
            if(logger.isErrorEnabled()){
                logger.error("Failed to send email to: {}" , e.getMessage());
            }

        }
    }
}
