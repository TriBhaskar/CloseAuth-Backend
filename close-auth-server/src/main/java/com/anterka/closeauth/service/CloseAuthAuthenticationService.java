package com.anterka.closeauth.service;

import com.anterka.closeauth.api.constants.ResponseStatus;
import com.anterka.closeauth.constants.UserRolesEnum;
import com.anterka.closeauth.dao.CloseAuthEnterpriseRepository;
import com.anterka.closeauth.dao.CloseAuthEnterpriseUserRepository;
import com.anterka.closeauth.dao.CloseAuthUserRoleRepository;
import com.anterka.closeauth.dto.mapper.EnterpriseRegistrationMapper;
import com.anterka.closeauth.dto.request.login.EnterpriseLoginRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseDetailsRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.dto.request.verifyotp.EnterpriseResendOtpRequest;
import com.anterka.closeauth.dto.request.verifyotp.EnterpriseVerifyOtpRequest;
import com.anterka.closeauth.dto.response.EnterpriseLoginData;
import com.anterka.closeauth.dto.response.EnterpriseLoginResponse;
import com.anterka.closeauth.dto.response.CustomApiResponse;
import com.anterka.closeauth.dto.response.EnterpriseRegistrationResponse;
import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import com.anterka.closeauth.entities.CloseAuthUserRole;
import com.anterka.closeauth.exception.*;
import jakarta.mail.MessagingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * This service handles the authentication of the user for close-auth
 * Handles the registration of new enterprises in the close-auth
 */
@Service
@RequiredArgsConstructor
public class CloseAuthAuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(CloseAuthAuthenticationService.class);
    private final EnterpriseRegistrationMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final CloseAuthEnterpriseUserRepository userRepository;
    private final CloseAuthEnterpriseRepository detailsRepository;
    private final CloseAuthUserRoleRepository userRoleRepository;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;
    private final RegistrationCacheService registrationCacheService;

    /**
     * Handles the login of the enterprise user on the close-auth dashboard
     */
    public EnterpriseLoginResponse authenticate(@NonNull EnterpriseLoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new CredentialValidationException("Invalid email or password for user: [" + request.getEmail() + "]");
        } catch (LockedException ex) {
            throw new UserAuthenticationException("User with email: [" + request.getEmail() + "] is blocked, please contact the clos-auth");
        } catch (Exception ex) {
            throw new EnterpriseRegistrationException("Exception occurred while authenticating the user: [" + request.getEmail() + "], Error: " + ex.getMessage());
        }
        log.info("User authenticated successfully!!");
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User-email: [" + request.getEmail() + "] does not exist"));

        var jwtToken = jwtService.generateJwtToken(user);

        // TODO: Implement the logic to save the refresh token in the database

        EnterpriseLoginData loginData  = EnterpriseLoginData.builder().user(EnterpriseLoginData.EnterpriseUser.builder().userId(user.getId())
                        .firstName(user.getFirstName()).lastName(user.getLastName()).username(user.getUsername())
                        .email(user.getEmail()).role(user.getRole().getRole().name()).build())
                .auth(EnterpriseLoginData.EnterpriseAuth.builder().accessToken(jwtToken).refreshToken(jwtToken).expiresIn(jwtService.getJwtExpirationTimeInMillis()).build()).build();
        return EnterpriseLoginResponse.builder().status(ResponseStatus.SUCCESS).message("Login Successful").data(loginData).build();
    }

    /**
     * Handles the registration of the enterprises on close-auth
     */

    @Transactional
    public EnterpriseRegistrationResponse registerEnterprise(EnterpriseRegistrationRequest request) throws MessagingException {
        validateEnterpriseData(request.getEnterpriseDetails());
        // Send Email with OTP
        String otp = otpService.generateOtp();
        long otpValiditySeconds = otpService.saveOtp(request.getEnterpriseDetails().getEnterpriseEmail(), otp);
        //TODO: handle the messagingException
        emailService.sendOTPMail(request.getEnterpriseDetails().getEnterpriseEmail(), otp);
        registrationCacheService.saveRegistration(request.getEnterpriseDetails().getEnterpriseEmail(), request);
        return EnterpriseRegistrationResponse.builder().username(request.getUserName()).otpValiditySeconds(otpValiditySeconds).status(ResponseStatus.SUCCESS).
                message("OTP sent to the email: [" + request.getEnterpriseDetails().getEnterpriseEmail() + "]").
                timestamp(LocalDateTime.now()).build();
    }

    /**
     * Verifies the enterprise email using the provided OTP.
     *
     * @param request the request containing the email and OTP to be verified
     * @return a CustomApiResponse indicating the status of the email verification
     * @throws EmailVerificationException if the registration request is not found or OTP is invalid
     */
    @Transactional
    public CustomApiResponse verifyEnterpriseEmail(EnterpriseVerifyOtpRequest request) {
        EnterpriseRegistrationRequest registrationRequest = registrationCacheService.getRegistration(request.getEmail());
        validateEmail(request, registrationRequest);
        CloseAuthEnterpriseDetails enterpriseDetails = mapper.toEnterpriseDetails(registrationRequest);
        if (enterpriseDetails.getEmail() == null || enterpriseDetails.getEmail().isEmpty()) {
            enterpriseDetails.setEmail(registrationRequest.getEnterpriseDetails().getEnterpriseEmail());
        }
        CloseAuthEnterpriseDetails savedDetails = detailsRepository.save(enterpriseDetails);
        CloseAuthEnterpriseUser user = saveAndReturnUser(registrationRequest, savedDetails);
        otpService.deleteOtp(request.getEmail());
        registrationCacheService.deleteRegistration(request.getEmail());
        return CustomApiResponse.builder().status(ResponseStatus.SUCCESS).message("Enterprise registered successfully user:"+user.getUsername()).timestamp(LocalDateTime.now()).build();
    }

    /**
     * Resends the OTP for enterprise email verification.
     *
     * @param request the request containing the email to which the OTP should be resent
     * @return a CustomApiResponse indicating the status of the OTP resend operation
     * @throws EmailVerificationException if the registration request is not found for the provided email
     */
    @Transactional
    public CustomApiResponse resendEnterpriseOTP(EnterpriseResendOtpRequest request) {
        EnterpriseRegistrationRequest registrationRequest = registrationCacheService.getRegistration(request.getEmail());
        if (registrationRequest == null) {
            throw new EmailVerificationException("Registration request not found for email: [" + request.getEmail() + "]");
        }
        String otp = otpService.generateOtp();
        otpService.saveOtp(request.getEmail(), otp);
        try {
            emailService.sendOTPMail(request.getEmail(), otp);
        }catch (MessagingException e){
            log.error(String.format("Exception occurred while sending the otp verification mail : [%s]", e.getMessage()));
            return CustomApiResponse.builder().status(ResponseStatus.FAILED).message("Error while sending the OTP verification email").timestamp(LocalDateTime.now()).build();
        }
        registrationCacheService.saveRegistration(request.getEmail(), registrationRequest);
        return CustomApiResponse.builder().status(ResponseStatus.SUCCESS).message("OTP sent to the email: [" + request.getEmail() + "]").timestamp(LocalDateTime.now()).build();
    }

    /**
     * Validates the OTP for the enterprise email verification.
     *
     * @param request the request containing the email and OTP to be verified
     * @param registrationRequest the registration request associated with the email
     * @throws EmailVerificationException if the registration request is not found, OTP is expired, or OTP is invalid
     */

    private void validateEmail(EnterpriseVerifyOtpRequest request, EnterpriseRegistrationRequest registrationRequest) {
        if (registrationRequest == null) {
            throw new EmailVerificationException("Registration request not found for email: [" + request.getEmail() + "]");
        }
        String otp = otpService.getOtp(request.getEmail());
        if (otp == null) {
            throw new EmailVerificationException("OTP Already Expired: [" + request.getEmail() + "]");
        }
        if (!otp.equals(request.getOtp())) {
            throw new EmailVerificationException("Invalid OTP for email: [" + request.getEmail() + "]");
        }
    }

    /**
     * Saves the user w.r.t the enterprise saved above
     * @return [CloseAuthEnterpriseUser] for creating the [{@link EnterpriseRegistrationResponse}]
     */
    private CloseAuthEnterpriseUser saveAndReturnUser(EnterpriseRegistrationRequest request, CloseAuthEnterpriseDetails savedDetails) {
        CloseAuthEnterpriseUser user = mapper.toEnterpriseUserWithDetails(request, savedDetails);
        user.setPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setEmail(savedDetails.getEmail()); //TODO : Email to be saved for the ORGANIZATION user for login

        Optional<CloseAuthUserRole> role = userRoleRepository.findByRole(UserRolesEnum.ORGANIZATION);
        if (role.isEmpty()) {
            throw new EnterpriseRegistrationException("Unable to register the enterprise user, roles might not be assigned to the user, Please contact close-auth");
        } else {
            user.setRole(role.get());
        }
        try {
            userRepository.save(user);
        }catch(Exception exception){
            throw new EnterpriseRegistrationException("Exception: [{"+exception.getCause()+"}] occurred while associating the user: ["+user+"] with the organization : ["+savedDetails.getName()+"]");
        }
        return userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new UserNotFoundException("Unable to find the user " + user.getEmail() + " in close-auth"));
    }

    /**
     * Accepts {@link EnterpriseDetailsRequest}
     * - validates the email, name, contact number
     */
    private void validateEnterpriseData(EnterpriseDetailsRequest request) {
        checkIfEnterpriseByEmailIsNew(request.getEnterpriseEmail());
        checkIfEnterpriseByNameIsNew(request.getEnterpriseName());
        checkIfContactNumberAlreadyExists(request.getEnterpriseContactNumber());
    }

    /**
     * validates the enterprise email
     *
     * @throws DataAlreadyExistsException if email already exists with some other enterprise
     */
    private void checkIfEnterpriseByEmailIsNew(String email) {
        Optional<CloseAuthEnterpriseDetails> enterpriseByEmailAlreadyRegistered = detailsRepository.findByEmail(email);
        if (enterpriseByEmailAlreadyRegistered.isPresent()) {
            throw new DataAlreadyExistsException("Unable to register the enterprise as email : [" + email + "] already exists");
        }
    }

    /**
     * validates the enterprise name
     *
     * @throws DataAlreadyExistsException if name already exists
     */
    private void checkIfEnterpriseByNameIsNew(String name) {
        Optional<CloseAuthEnterpriseDetails> enterpriseByNameAlreadyRegistered = detailsRepository.findByName(name);
        if (enterpriseByNameAlreadyRegistered.isPresent()) {
            throw new DataAlreadyExistsException("Unable to register the enterprise as the name: [" + name + "] already registered with close-auth");
        }
    }

    /**
     * validates the enterprise contact number
     *
     * @throws DataAlreadyExistsException if contact number already associated with some other enterprise
     */
    private void checkIfContactNumberAlreadyExists(String contactNumber) {
        Optional<CloseAuthEnterpriseDetails> contactNumberAlreadyRegistered = detailsRepository.findByContactNumber(contactNumber);
        if (contactNumberAlreadyRegistered.isPresent()) {
            throw new DataAlreadyExistsException("Unable to register the enterprise as the contact number: [" + contactNumber + "] already registered with close-auth");
        }
    }

}
