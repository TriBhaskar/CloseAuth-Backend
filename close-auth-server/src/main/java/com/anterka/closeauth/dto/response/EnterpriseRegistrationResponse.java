package com.anterka.closeauth.dto.response;

import com.anterka.closeauth.constants.UserStatusEnum;
import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
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

    public static EnterpriseRegistrationResponse fromEntity(CloseAuthEnterpriseUser user, CloseAuthEnterpriseDetails enterpriseDetails) {
        return EnterpriseRegistrationResponse.builder()
                .status("SUCCESS")
                .message("Enterprise registered successfully")
                .user(EnterpriseUserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .userName(user.getUsername())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build())
                .enterprise(EnterpriseDetailsResponse.builder()
                        .id(enterpriseDetails.getId())
                        .name(enterpriseDetails.getName())
                        .email(enterpriseDetails.getEmail())
                        .createdAt(enterpriseDetails.getCreatedAt())
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