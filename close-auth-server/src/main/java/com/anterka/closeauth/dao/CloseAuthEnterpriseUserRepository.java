package com.anterka.closeauth.dao;

import com.anterka.closeauth.entities.CloseAuthEnterpriseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloseAuthEnterpriseUserRepository extends JpaRepository<CloseAuthEnterpriseUser, Long> {
    Optional<CloseAuthEnterpriseUser> findByEmail(String email);

}
