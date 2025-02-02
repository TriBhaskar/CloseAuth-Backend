package com.anterka.closeauth.service;

import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RegistrationCacheService {

    private final JedisPooled client;
    private final Gson gson;

    private static final String REGISTRATION_PREFIX = "registration_";
    private static final long REGISTRATION_VALIDITY_SECONDS = TimeUnit.HOURS.toSeconds(2);

    public void saveRegistration(String email, EnterpriseRegistrationRequest registrationRequest) {
        String key = REGISTRATION_PREFIX + email;
        client.jsonSet(key, gson.toJson(registrationRequest));
        client.expire(key, REGISTRATION_VALIDITY_SECONDS);
    }

    public EnterpriseRegistrationRequest getRegistration(String email) {
        String key = REGISTRATION_PREFIX + email;
        return client.jsonGet(key, EnterpriseRegistrationRequest.class);
    }

    public void deleteRegistration(String email) {
        String key = REGISTRATION_PREFIX + email;
        client.del(key);
    }



}
