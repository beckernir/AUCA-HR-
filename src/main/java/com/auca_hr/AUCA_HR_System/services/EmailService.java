package com.auca_hr.AUCA_HR_System.services;

import com.auca_hr.AUCA_HR_System.dtos.SendEmailDto;
import com.auca_hr.AUCA_HR_System.exceptions.InvalidFormatException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Sends an email asynchronously with retry mechanism
    @Async
    public CompletableFuture<Void> sendEmail(SendEmailDto sendEmailDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = createEmailMessage(sendEmailDto);
                mailSender.send(message);

                logger.info("Email sent successfully to {}", sendEmailDto.getTo());
            } catch (MessagingException e) {
                logger.error("Failed to send email to {}", sendEmailDto.getTo(), e);
                // You can either throw a specific custom exception here or use fallback logic
                throw new InvalidFormatException("Failed to send email to " + sendEmailDto.getTo());
            }
        });
    }

    private MimeMessage createEmailMessage(SendEmailDto sendEmailDto) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(sendEmailDto.getTo());
        helper.setSubject(sendEmailDto.getSubject());
        helper.setText(sendEmailDto.getBody(), true);  // Set to true for HTML content
        return message;
    }

}
