package com.anterka.closeauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

@Service
@RequiredArgsConstructor
public class TokenService {

    private JedisPooled client;

    private static final String TOKEN_PREFIX = "pwd_reset_";
    private static final long TOKEN_VALIDITY_SECONDS = 1 * 60 * 60;

    public void saveToken(String email, String token) {
        String key = TOKEN_PREFIX + token;
        // set the token in redis with expiry time
        client.setex(key, TOKEN_VALIDITY_SECONDS, email);
    }

    public String getEmailFromToken(String token) {
        String key = TOKEN_PREFIX + token;
        return client.get(key);
    }

    public void deleteToken(String token) {
        String key = TOKEN_PREFIX + token;
        client.del(key);
    }

    // Optional: Add rate limiting
    public boolean isRateLimited(String email) {
        String key = "rate_limit_" + email;
        String attempts = client.get(key);

        if (attempts == null) {
            client.setex(key, 3600, "1"); // 1 hour expiry
            return false;
        }

        int currentAttempts = Integer.parseInt(attempts);
        if (currentAttempts >= 5) { // Max 5 attempts per hour
            return true;
        }

        client.incr(key);
        return false;
    }
}

