package com.anterka.closeauth.service;

import com.anterka.closeauth.constants.UserRolesEnum;
import com.anterka.closeauth.dao.CloseAuthEnterpriseRepository;
import com.anterka.closeauth.dao.CloseAuthEnterpriseUserRepository;
import com.anterka.closeauth.dao.CloseAuthUserRoleRepository;
import com.anterka.closeauth.dto.mapper.EnterpriseRegistrationMapper;
import com.anterka.closeauth.dto.request.auth.CloseAuthAuthenticationRequest;
import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.dto.response.CloseAuthAuthenticationResponse;
import com.anterka.closeauth.dto.response.EnterpriseRegistrationResponse;
import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import com.anterka.closeauth.entities.CloseAuthUserRole;
import com.anterka.closeauth.exception.EnterpriseRegistrationException;
import com.anterka.closeauth.exception.UserAuthenticationException;
import com.anterka.closeauth.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional
    public EnterpriseRegistrationResponse registerEnterprise(EnterpriseRegistrationRequest request) {
        CloseAuthEnterpriseDetails enterpriseDetails = mapper.toEnterpriseDetails(request);
        if (enterpriseDetails.getEmail() == null || enterpriseDetails.getEmail().isEmpty()) {
            enterpriseDetails.setEmail(request.getEnterpriseDetails().getEnterpriseEmail());
        }

        CloseAuthEnterpriseDetails savedDetails = detailsRepository.save(enterpriseDetails);
        CloseAuthEnterpriseUser user = mapper.toEnterpriseUserWithDetails(request, savedDetails);
        user.setPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setEmail(request.getEnterpriseDetails().getEnterpriseEmail()); //TODO: needs discussion

        Optional<CloseAuthUserRole> role = userRoleRepository.findByRole(UserRolesEnum.ORGANIZATION);
        if (role.isEmpty()) {
            throw new EnterpriseRegistrationException("Unable to register the enterprise user, roles might not be assigned to the user");
        } else {
            user.setRole(role.get());
        }
        userRepository.save(user);
        var jwtToken = jwtService.generateJwtToken(user);
        EnterpriseRegistrationResponse response = EnterpriseRegistrationResponse.fromEntity(user, enterpriseDetails);
        response.setToken(jwtToken);
        return response;
    }

    public CloseAuthAuthenticationResponse authenticate(CloseAuthAuthenticationRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        }catch (Exception ex){
            if (ex.getCause() instanceof LockedException){
                throw new UserAuthenticationException("User with username: [{"+request.getUsername()+"}] is blocked, please contact the clos-auth");
            }
            //TODO - handle more exceptions
        }
        log.info("User authenticated successfully!!");
        var user = userRepository.findByEmail(request.getUsername()).orElseThrow(() -> new UserNotFoundException("User: [{"+request.getUsername()+"}] does not exist"));
        var jwtToken = jwtService.generateJwtToken(user);
        return CloseAuthAuthenticationResponse.builder().authenticationToken(jwtToken).build();
    }
}
