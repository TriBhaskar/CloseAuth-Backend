package com.anterka.closeauth.service;

import com.anterka.closeauth.dao.CloseAuthEnterpriseUserRepository;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CloseAuthEnterpriseUserService implements UserDetailsService {
    private final CloseAuthEnterpriseUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CloseAuthEnterpriseUser> user = userRepository.findByEmail(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("User" + username + " does not exist"));
    }
}
