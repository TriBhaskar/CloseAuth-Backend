package com.anterka.closeauth.service.redis;

import com.anterka.closeauth.config.RedisConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

/**
 * This service will handle rate limiting for password reset-related operations.
 * It limits how many password reset attempts someone can make within a time window, making it much harder for attackers to guess or brute force tokens
 * */
@Service
@AllArgsConstructor
public class CloseAuthRateLimiterService {

    private final JedisPooled jedisPooled;
    private final RedisConfig redisConfig;

    public boolean isLimited(String action, String identifier) {
        String key = "rate_limit:" + action + ":" + identifier;

        String countStr = jedisPooled.get(key);
        int count = (countStr != null) ? Integer.parseInt(countStr) : 0;

        int limit = switch (action) {
            case "forgot_password" -> Integer.parseInt(redisConfig.getForgotPasswordRateLimit());
            case "validate_token" ->  Integer.parseInt(redisConfig.getValidateTokenRateLimit());
            case "reset_password" ->  Integer.parseInt(redisConfig.getResetPasswordRateLimit());
            default -> 5;
        };

        if (count >= limit) {
            return true;
        }

        if (count == 0) {
            jedisPooled.setex(key, Integer.parseInt(redisConfig.getWindowMinutesRateLimit()) * 60L, "1");
        } else {
            jedisPooled.incr(key);
        }

        return false;
    }
}