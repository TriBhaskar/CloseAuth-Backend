package com.anterka.closeauth.dao;

import com.anterka.closeauth.constants.UserRolesEnum;
import com.anterka.closeauth.entities.CloseAuthUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CloseAuthUserRoleRepository extends JpaRepository<CloseAuthUserRole, Long> {
    Optional<CloseAuthUserRole> findByRole(UserRolesEnum role);
}
