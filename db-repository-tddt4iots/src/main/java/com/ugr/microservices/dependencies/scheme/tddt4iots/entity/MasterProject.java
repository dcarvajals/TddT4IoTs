package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
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
@Table(name = "master_project")
public class MasterProject {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "master_project_id_gen")
    @SequenceGenerator(name = "master_project_id_gen", sequenceName = "master_project_id_masterproject_seq", allocationSize = 1)
    @Column(name = "id_masterproject", nullable = false)
    private Long id;

    @Column(name = "creationdate_mp")
    private Date creationdateMp;

    @Column(name = "updatedate_mp")
    private Date updatedateMp;

    @Size(max = 30)
    @Column(name = "name_mp", length = 30)
    private String nameMp;

    @Size(max = 15)
    @Column(name = "code_mp", length = 15)
    private String codeMp;

    @Column(name = "description_mp", length = Integer.MAX_VALUE)
    private String descriptionMp;

    @Column(name = "path_mp", length = Integer.MAX_VALUE)
    private String pathMp;

    @Column(name = "status_uml", length = Integer.MAX_VALUE)
    private String statusUml;

    @Column(name = "status_iot", length = Integer.MAX_VALUE)
    private String statusIot;

    @Column(name = "download", length = Integer.MAX_VALUE)
    private String download;

    @Column(name = "download_ang", length = Integer.MAX_VALUE)
    private String downloadAng;

    @Column(name = "download_all", length = Integer.MAX_VALUE)
    private String downloadAll;

    @Size(max = 3)
    @Column(name = "develop_status", length = 3)
    private String developStatus;

    @Column(name = "actual_percentage", precision = 5, scale = 2)
    private BigDecimal actualPercentage;

    @Column(name = "percentageupdate_project")
    private Date percentageupdateProject;

    @Column(name = "startplan_date")
    private Date startplanDate;

    @Column(name = "endplan_date")
    private Date endplanDate;

    @OneToMany(mappedBy = "masterProjectIdMasterproject")
    private Set<PermitMaster> permitMasters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idMasterproject")
    private Set<ProjectEntregable> projectEntregables = new LinkedHashSet<>();

}