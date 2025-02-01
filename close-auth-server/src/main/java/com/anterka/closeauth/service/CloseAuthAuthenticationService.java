package com.anterka.closeauth.service;

import com.anterka.closeauth.constants.UserRolesEnum;
import com.anterka.closeauth.dao.CloseAuthEnterpriseRepository;
import com.anterka.closeauth.dao.CloseAuthEnterpriseUserRepository;
import com.anterka.closeauth.dao.CloseAuthUserRoleRepository;
import com.anterka.closeauth.dto.mapper.EnterpriseRegistrationMapper;
import com.anterka.closeauth.dto.request.auth.CloseAuthAuthenticationRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseDetailsRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.dto.response.CloseAuthAuthenticationResponse;
import com.anterka.closeauth.dto.response.EnterpriseRegistrationResponse;
import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import com.anterka.closeauth.entities.CloseAuthUserRole;
import com.anterka.closeauth.exception.CredentialValidationException;
import com.anterka.closeauth.exception.DataAlreadyExistsException;
import com.anterka.closeauth.exception.EnterpriseRegistrationException;
import com.anterka.closeauth.exception.UserAuthenticationException;
import com.anterka.closeauth.exception.UserNotFoundException;
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
    public CloseAuthAuthenticationResponse authenticate(@NonNull CloseAuthAuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new CredentialValidationException("Invalid username or password for user: [" + request.getUsername() + "]");
        } catch (LockedException ex) {
            throw new UserAuthenticationException("User with username: [" + request.getUsername() + "] is blocked, please contact the clos-auth");
        } catch (Exception ex) {
            throw new EnterpriseRegistrationException("Exception occurred while authenticating the user: [" + request.getUsername() + "], Error: " + ex.getMessage());
        }
        log.info("User authenticated successfully!!");
        var user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User: [" + request.getUsername() + "] does not exist"));
        var jwtToken = jwtService.generateJwtToken(user);
        return CloseAuthAuthenticationResponse.builder().authenticationToken(jwtToken).build();
    }

    /**
     * Handles the registration of the enterprises on close-auth
     */

    @Transactional
    public EnterpriseRegistrationResponse registerEnterprise(EnterpriseRegistrationRequest request) {
        validateEnterpriseData(request.getEnterpriseDetails());
        // Send Email with OTP
        String otp = otpService.generateOtp();
        long otpValiditySeconds = otpService.saveOtp(request.getEnterpriseDetails().getEnterpriseEmail(), otp);
        emailService.sendOTPMail(request.getEnterpriseDetails().getEnterpriseEmail(), otp);
        registrationCacheService.saveRegistration(request.getEnterpriseDetails().getEnterpriseEmail(), request);
        return EnterpriseRegistrationResponse.builder().username(request.getUserName()).otpValiditySeconds(otpValiditySeconds).status("success").
                message("OTP sent to the email: [" + request.getEnterpriseDetails().getEnterpriseEmail() + "]").
                timestamp(LocalDateTime.now()).build();
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
     * Creates the [{@link EnterpriseRegistrationResponse}]
     *
     * @param user              which holds the [{@link CloseAuthEnterpriseUser}]
     */
    private EnterpriseRegistrationResponse createRegistrationResponse(CloseAuthEnterpriseUser user) {
        var jwtToken = jwtService.generateJwtToken(user);
        EnterpriseRegistrationResponse response = new EnterpriseRegistrationResponse();
        return response;
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
