package com.anterka.closeauth.service;

import com.anterka.closeauth.config.CloseAuthAppConfig;
import com.anterka.closeauth.dao.CloseAuthEnterpriseUserRepository;
import com.anterka.closeauth.dto.request.password.CloseAuthForgotPasswordRequest;
import com.anterka.closeauth.dto.request.password.CloseAuthResetPasswordRequest;
import com.anterka.closeauth.dto.response.CloseAuthTokenValidationResponse;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import com.anterka.closeauth.exception.InvalidTokenException;
import com.anterka.closeauth.exception.PasswordMismatchedException;
import com.anterka.closeauth.exception.PasswordReusedException;
import com.anterka.closeauth.exception.UserNotFoundException;
import com.anterka.closeauth.exception.WeakPasswordException;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class CloseAuthPasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(CloseAuthPasswordResetService.class);
    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private final CloseAuthEnterpriseUserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloseAuthAppConfig appConfig;
    private final EmailService emailService;

    public void processForgotPassword(CloseAuthForgotPasswordRequest request) {
        // Find user by email
        String email = request.getUserEmail();
        long tokenExpiryMinutes = Long.parseLong(appConfig.getTokenExpiryMinutes());
        String resetPasswordUrl = appConfig.getResetPasswordUrl();
        CloseAuthEnterpriseUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("No user found with the requested email : [%s]", email)));

        String token = UUID.randomUUID().toString();

        String redisKey = PASSWORD_RESET_PREFIX + token;
        redisTemplate.opsForValue().set(redisKey, user.getId().toString(), tokenExpiryMinutes, TimeUnit.MINUTES);

        // Create password reset link
        String resetLink = resetPasswordUrl + "?token=" + token;
        try {
            emailService.sendForgotPasswordLinkMail(user.getEmail(), resetLink, tokenExpiryMinutes);
        }catch (MessagingException exception){
            log.error(String.format("Exception occurred while sending the forgot password email to the user : [%s]", user.getEmail()));
        }
    }

    public void resetPassword(CloseAuthResetPasswordRequest request) {
        // Validate token exists and isn't expired first
        String redisKey = PASSWORD_RESET_PREFIX + request.getToken();
        String userId = redisTemplate.opsForValue().get(redisKey);

        if (userId == null) {
            // To prevent timing attacks, still perform password validation
            // but throw the same exception regardless
            validatePasswordStrength(request.getNewPassword());
            throw new InvalidTokenException("Invalid or expired token");
        }

        // Validate password strength and match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchedException("Passwords entered do not match");
        }

        validatePasswordStrength(request.getNewPassword());

        // Find user
        CloseAuthEnterpriseUser user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new PasswordReusedException("New password must be different from the current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete token from Redis immediately after successful use
        redisTemplate.delete(redisKey);
    }

    public CloseAuthTokenValidationResponse validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return new CloseAuthTokenValidationResponse(false, "Token is required");
        }

        String redisKey = PASSWORD_RESET_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(redisKey);

        if (userId == null) {
            return new CloseAuthTokenValidationResponse(false, "Invalid or expired token");
        }
        //TODO: Add the token validation rate limiting logic w.r.t an IP or user
        return new CloseAuthTokenValidationResponse(true, "Token is valid");
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters long");
        }

        // Check for password complexity
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (!(hasLetter && hasDigit && hasSpecial)) {
            throw new WeakPasswordException("Password must contain at least one letter, one number, and one special character");
        }
    }
}
