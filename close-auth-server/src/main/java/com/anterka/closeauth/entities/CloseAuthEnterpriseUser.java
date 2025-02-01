package com.anterka.closeauth.entities;

import com.anterka.closeauth.constants.CloseAuthTables;
import com.anterka.closeauth.constants.UserStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = CloseAuthTables.EnterpriseUsers.TABLE_NAME)
public class CloseAuthEnterpriseUser implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "close_auth_ent_user_sequence_generator")
    @Column(name = CloseAuthTables.EnterpriseUsers.ID)
    @SequenceGenerator(name = "close_auth_ent_user_sequence_generator", sequenceName = CloseAuthTables.EnterpriseUsers.SEQUENCE_NAME, allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = CloseAuthTables.EnterpriseUsers.ENT_ID, referencedColumnName = CloseAuthTables.EnterpriseDetails.ID)
    private CloseAuthEnterpriseDetails closeAuthEnterpriseDetails;

    @Column(name = CloseAuthTables.EnterpriseUsers.FIRST_NAME)
    private String firstName;

    @Column(name = CloseAuthTables.EnterpriseUsers.LAST_NAME)
    private String lastName;

    @Column(name = CloseAuthTables.EnterpriseUsers.USER_NAME)
    private String userName;

    @Column(name = CloseAuthTables.EnterpriseUsers.EMAIL)
    private String email;

    @Column(name = CloseAuthTables.EnterpriseUsers.PASSWORD)
    private String password;

    @ManyToOne
    @JoinColumn(name = CloseAuthTables.EnterpriseUsers.ROLE, referencedColumnName = CloseAuthTables.EnterpriseUserRoles.ID)
    private CloseAuthUserRole role;

    @Column(name = CloseAuthTables.EnterpriseUsers.STATUS)
    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserStatusEnum status;

    @Column(name = CloseAuthTables.EnterpriseUsers.LAST_LOGIN)
    private Instant lastLoginAt;

    @Column(name = CloseAuthTables.EnterpriseUsers.FAILED_LOGIN_ATTEMPTS)
    private int failedLoginAttempts;

    @Column(name = CloseAuthTables.EnterpriseUsers.LAST_PASSWORD_CHANGED_AT)
    private Instant lastPasswordChangedAt;

    @Column(name = CloseAuthTables.EnterpriseUsers.CREATED_BY)
    private String createdBy;

    @Column(name = CloseAuthTables.EnterpriseUsers.CREATED_AT)
    private Instant createdAt;

    @Column(name = CloseAuthTables.EnterpriseUsers.UPDATED_BY)
    private String updatedBy;

    @Column(name = CloseAuthTables.EnterpriseUsers.UPDATED_AT)
    private Instant updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getRole().name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return getStatus() == UserStatusEnum.UNBLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
