package com.anterka.closeauth.config;

import com.anterka.closeauth.api.ApiPaths;
import com.anterka.closeauth.service.CloseAuthEnterpriseUserService;
import com.anterka.closeauth.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final CloseAuthEnterpriseUserService closeAuthEnterpriseUserService;
    public static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final String[] skipFilterForRequests = {
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.LOGIN,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.REGISTER_ENTERPRISE,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.VERIFY_OTP,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.FORGOT_PASSWORD,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.VALIDATE_TOKEN,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.RESET_PASSWORD,
            ApiPaths.API_CONTEXT_PATH+ApiPaths.API_PREFIX+ApiPaths.RESEND_OTP,
            ApiPaths.API_CONTEXT_PATH+"/api/v1/testredis"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return Arrays.asList(skipFilterForRequests).contains(requestUri);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;


        if (shouldNotFilter(request)) {
            log.info("Skipping JWT authentication for: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        log.trace("Checking the request header");
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            log.error("Either invalid or unable to find the token in the request : {}", request);
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authorizationHeader.substring(7);
        userEmail = jwtService.extractUserName(jwtToken);

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.closeAuthEnterpriseUserService.loadUserByUsername(userEmail);
            authenticateAndUpdateContext(request, jwtToken, userDetails);
        }
        filterChain.doFilter(request, response);
        log.info("User {} authenticated successfully",  userEmail);
    }

    private void authenticateAndUpdateContext(HttpServletRequest request, String jwtToken, UserDetails userDetails) {
        if (Boolean.TRUE.equals(jwtService.isTokenValid(jwtToken, userDetails))){
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }
}