package com.training.mts.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
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
    }
}
