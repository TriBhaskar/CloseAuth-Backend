package com.anterka.closeauth.dao;

import com.anterka.closeauth.entities.CloseAuthEnterpriseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloseAuthEnterpriseRepository extends JpaRepository<CloseAuthEnterpriseDetails, Long> {

}
