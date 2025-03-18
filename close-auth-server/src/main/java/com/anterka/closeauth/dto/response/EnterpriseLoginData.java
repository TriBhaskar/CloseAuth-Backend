package com.anterka.closeauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseLoginData {
    private EnterpriseUser user;
    private EnterpriseAuth auth;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EnterpriseUser {
        private Long userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EnterpriseAuth {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
    }

}
