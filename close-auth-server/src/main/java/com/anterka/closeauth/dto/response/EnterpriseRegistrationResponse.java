package com.anterka.closeauth.dto.response;

import com.anterka.closeauth.constants.UserStatusEnum;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseRegistrationResponse {
    private String status;
    private String message;
    private String token;
    private EnterpriseUserResponse user;
    private EnterpriseDetailsResponse enterprise;

    public static EnterpriseRegistrationResponse fromEntity(CloseAuthEnterpriseUser user) {
        return EnterpriseRegistrationResponse.builder()
                .status("SUCCESS")
                .message("Enterprise registered successfully")
                .user(EnterpriseUserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .userName(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .enterprise(EnterpriseDetailsResponse.builder()
                        .id(user.getCloseAuthEnterpriseDetails().getId())
                        .name(user.getCloseAuthEnterpriseDetails().getName())
                        .email(user.getCloseAuthEnterpriseDetails().getEmail())
                        .createdAt(user.getCloseAuthEnterpriseDetails().getCreatedAt())
                        .build())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterpriseUserResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String userName;
        private String email;
        private UserStatusEnum status;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterpriseDetailsResponse {
        private Long id;
        private String name;
        private String email;
        private Instant createdAt;
    }
}