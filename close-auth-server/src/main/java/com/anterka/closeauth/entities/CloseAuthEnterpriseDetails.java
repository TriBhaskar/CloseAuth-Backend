package com.anterka.closeauth.entities;

import com.anterka.closeauth.constants.CloseAuthTables;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = CloseAuthTables.EnterpriseDetails.TABLE_NAME)
public class CloseAuthEnterpriseDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "close_auth_ent_details_sequence_generator")
    @SequenceGenerator(name = "close_auth_ent_details_sequence_generator", sequenceName = CloseAuthTables.EnterpriseDetails.SEQUENCE_NAME, allocationSize = 1)
    @Column(name = CloseAuthTables.EnterpriseDetails.ID)
    private Long id;

    @Column(name = CloseAuthTables.EnterpriseDetails.NAME)
    private String name;

    @Column(name = CloseAuthTables.EnterpriseDetails.EMAIL)
    private String email;

    @Column(name = CloseAuthTables.EnterpriseDetails.CONTACT)
    private String contactNumber;

    @Column(name = CloseAuthTables.EnterpriseDetails.COUNTRY)
    private String country;

    @Column(name = CloseAuthTables.EnterpriseDetails.STATE)
    private String state;

    @Column(name = CloseAuthTables.EnterpriseDetails.CITY)
    private String city;

    @Column(name = CloseAuthTables.EnterpriseDetails.ADDRESS)
    private String address;

    @Column(name = CloseAuthTables.EnterpriseDetails.PIN_CODE)
    private String pinCode;

    @Column(name = CloseAuthTables.EnterpriseDetails.CREATED_BY)
    private String createdBy;

    @Column(name = CloseAuthTables.EnterpriseDetails.CREATED_AT)
    private Instant createdAt = Instant.now();

    @Column(name = CloseAuthTables.EnterpriseDetails.UPDATED_BY)
    private String updatedBy;

    @Column(name = CloseAuthTables.EnterpriseDetails.UPDATED_AT)
    private Instant updatedAt;

    @OneToMany(mappedBy = "closeAuthEnterpriseDetails", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CloseAuthEnterpriseUser> closeAuthEnterpriseUsers;
}
