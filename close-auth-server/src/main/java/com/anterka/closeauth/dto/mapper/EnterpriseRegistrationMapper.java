package com.anterka.closeauth.dto.mapper;

import com.anterka.closeauth.dto.request.register.EnterpriseRegistrationRequest;
import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class})
public interface EnterpriseRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "enterpriseDetails.enterpriseName")
    @Mapping(target = "email", source = "enterpriseDetails.enterpriseEmail")
    @Mapping(target = "contactNumber", source = "enterpriseDetails.enterpriseContactNumber")
    @Mapping(target = "country", source = "enterpriseDetails.enterpriseCountry")
    @Mapping(target = "state", source = "enterpriseDetails.enterpriseState")
    @Mapping(target = "address", source = "enterpriseDetails.enterpriseAddress")
    @Mapping(target = "pinCode", source = "enterpriseDetails.enterprisePinCode")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", source = "userName")
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "closeAuthEnterpriseUsers", ignore = true)
    CloseAuthEnterpriseDetails toEnterpriseDetails(EnterpriseRegistrationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", source = "userFirstName")
    @Mapping(target = "lastName", source = "userLastName")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "email", source = "userEmail")
    @Mapping(target = "password", source = "userPassword")
    @Mapping(target = "status", constant = "UNBLOCKED")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastPasswordChangedAt", expression = "java(Instant.now())")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", source = "userName")
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "role", ignore = true)
    CloseAuthEnterpriseUser toEnterpriseUser(EnterpriseRegistrationRequest request);

    default CloseAuthEnterpriseUser toEnterpriseUserWithDetails(EnterpriseRegistrationRequest request, CloseAuthEnterpriseDetails details) {
        CloseAuthEnterpriseUser user = toEnterpriseUser(request);
        user.setCloseAuthEnterpriseDetails(details);
        return user;
    }
}