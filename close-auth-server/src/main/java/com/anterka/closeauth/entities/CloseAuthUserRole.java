package com.anterka.closeauth.entities;

import com.anterka.closeauth.constants.CloseAuthTables;
import com.anterka.closeauth.constants.UserRolesEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = CloseAuthTables.EnterpriseUserRoles.TABLE_NAME)
public class CloseAuthUserRole implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = CloseAuthTables.EnterpriseUserRoles.ID)
    private Long id;

    @Column(name = CloseAuthTables.EnterpriseUserRoles.ROLE)
    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserRolesEnum role;

    @Column(name = CloseAuthTables.EnterpriseUserRoles.DESCRIPTION)
    private String description;

}
