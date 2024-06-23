package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "permit_master")
public class PermitMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permit_master_id_gen")
    @SequenceGenerator(name = "permit_master_id_gen", sequenceName = "permit_master_id_permitmaster_seq", allocationSize = 1)
    @Column(name = "id_permitmaster", nullable = false)
    private Long id;

    @Column(name = "creationdate_pm")
    private Date creationdatePm;

    @Column(name = "updatedate_pm")
    private Date updatedatePm;

    @Column(name = "joinactive_pm", length = Integer.MAX_VALUE)
    private String joinactivePm;

    @Column(name = "permit_pm", length = Integer.MAX_VALUE)
    private String permitPm;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id_person", nullable = false)
    private Person personIdPerson;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_project_id_masterproject", nullable = false)
    private MasterProject masterProjectIdMasterproject;

    @Size(max = 3)
    @Column(name = "speciality", length = 3)
    private String speciality;

    @Size(max = 1)
    @Column(name = "role", length = 1)
    private String role;

    @Size(max = 1)
    @Column(name = "role_status", length = 1)
    private String roleStatus;

}