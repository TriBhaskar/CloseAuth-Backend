package com.anterka.closeauth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private final Environment env;

    public JWTService(Environment env) {
        this.env = env;
    }

    /**
     * Extracts the user email from the JWT token in the request
     */
    public String extractUserName(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    /**
     * Validates the token w.r.t the [{@link UserDetails}] along with the expiration date
     */
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * generates the token just with the userDetails if there are no Claims
     */
    public String generateJwtToken(UserDetails userDetails) {
        return generateJwtToken(new HashMap<>(), userDetails);
    }

    /**
     * generates the token including the claims [RegisterClaims, PublicClaims, PrivateClaims]
     * Fetches the expiration time in hours from the properties
     * Default expiration time is [2] hours
     */
    public String generateJwtToken(Map<String, Object> claims, @NonNull UserDetails userDetails) {
        int expirationTimeInHours = Integer.parseInt(env.getProperty("jwt.security.secret-key.expiration.time.in-hours", "2"));
        long expirationTimeInMillis = 1000L * 60 * 60 * expirationTimeInHours;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeInMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * @return true if the token is not expired by reading the expiration from the claims
     * */
    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    /**
     * extracts the expiration from the claims with the token
     */
    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * extract single claim passed with the token
     */
    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaimsForToken(jwtToken);
        return claimsResolver.apply(claims);
    }

    /**
     * extract all claims associated with the token
     */
    private Claims extractAllClaimsForToken(String jwtToken) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(jwtToken).getBody();
    }

    /**
     * reads the secret_key from the properties
     * @return Key using [SH256 signature Algorithm]
     * */
    private Key getSignInKey() {
        final String secret_key = env.getProperty("jwt.security.secret-key");
        byte[] signInKeyBytes = Decoders.BASE64.decode(secret_key);
        return Keys.hmacShaKeyFor(signInKeyBytes);
    }

}
