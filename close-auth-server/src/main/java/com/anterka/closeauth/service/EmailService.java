package com.anterka.closeauth.service;

import com.anterka.closeauth.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendOTPMail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("OTP for Email Verification");
        message.setText("Your OTP for email verification is: " + otp);

        try {
            mailSender.send(message);
            log.info("Email sent successfully");
        } catch (MailException e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new EmailSendingException("Failed to process email verification request");
        }

    }
}
