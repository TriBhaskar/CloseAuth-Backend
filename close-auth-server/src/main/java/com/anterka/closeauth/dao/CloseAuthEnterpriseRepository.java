package com.anterka.closeauth.dao;

import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CloseAuthEnterpriseRepository extends JpaRepository<CloseAuthEnterpriseDetails, Long> {
    Optional<CloseAuthEnterpriseDetails> findByEmail(String email);

    Optional<CloseAuthEnterpriseDetails> findByName(String name);

    Optional<CloseAuthEnterpriseDetails> findByContactNumber(String contactNumber);
}