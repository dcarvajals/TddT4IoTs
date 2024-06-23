package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "entregable_component_members")
public class EntregableComponentMember {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entregable_component_members_id_gen")
    @SequenceGenerator(name = "entregable_component_members_id_gen", sequenceName = "entregable_component_members_id_entregable_member_seq", allocationSize = 1)
    @Column(name = "id_entregable_member", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "status_member", nullable = false, length = Integer.MAX_VALUE)
    private String statusMember;

    @NotNull
    @Column(name = "creationdate_em", nullable = false)
    private Instant creationdateEm;

    @NotNull
    @Column(name = "updatedate_em", nullable = false)
    private Date updatedateEm;

    @Column(name = "id_component_task")
    private Integer idComponentTask;

    @Column(name = "id_permit_master")
    private Integer idPermitMaster;

    @Size(max = 1)
    @Column(name = "role_member", length = 1)
    private String roleMember;

}