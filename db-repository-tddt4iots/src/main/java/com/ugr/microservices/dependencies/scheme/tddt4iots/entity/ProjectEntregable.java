package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "project_entregable")
public class ProjectEntregable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_entregable_id_gen")
    @SequenceGenerator(name = "project_entregable_id_gen", sequenceName = "project_entregable_component_id_entregable_component_seq", allocationSize = 1)
    @Column(name = "id_entregable", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_masterproject", nullable = false)
    private MasterProject idMasterproject;

    @Size(max = 30)
    @NotNull
    @Column(name = "name_entregable", nullable = false, length = 30)
    private String nameEntregable;

    @NotNull
    @Column(name = "description_entregable", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionEntregable;

    @NotNull
    @Column(name = "status_entregable", nullable = false, length = Integer.MAX_VALUE)
    private String statusEntregable;

    @NotNull
    @Column(name = "prioritylevel_entregable", nullable = false)
    private Integer prioritylevelEntregable;

    @NotNull
    @Column(name = "path_entregable", nullable = false, length = Integer.MAX_VALUE)
    private String pathEntregable;

    @NotNull
    @Column(name = "creationdate_entregable", nullable = false)
    private Date creationdateEntregable;

    @NotNull
    @Column(name = "updatedate_entregable", nullable = false)
    private Date updatedateEntregable;

    @NotNull
    @Column(name = "stimateddate_entregable", nullable = false)
    private Date stimateddateEntregable;

    @Column(name = "finishdate_entregable")
    private Date finishdateEntregable;

    @NotNull
    @Column(name = "actual_percentage_entregable", nullable = false, precision = 18, scale = 2)
    private BigDecimal actualPercentageEntregable;

    @NotNull
    @Column(name = "base_percentage_entregable", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePercentageEntregable;

    @Size(max = 3)
    @NotNull
    @Column(name = "develop_status_entregable", nullable = false, length = 3)
    private String developStatusEntregable;

    @Column(name = "startdate_entregable")
    private Date startdateEntregable;

    @Column(name = "percentageupdate_entregable")
    private Date percentageupdateEntregable;

    @Column(name = "is_started")
    private Boolean isStarted;

    @OneToMany(mappedBy = "idEntregable")
    private Set<ProjectEntregableComponent> projectEntregableComponents = new LinkedHashSet<>();

}