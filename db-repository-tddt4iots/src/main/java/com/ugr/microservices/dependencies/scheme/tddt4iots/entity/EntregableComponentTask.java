package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "entregable_component_task")
public class EntregableComponentTask {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entregable_component_task_id_gen")
    @SequenceGenerator(name = "entregable_component_task_id_gen", sequenceName = "entregable_component_task_id_task_seq", allocationSize = 1)
    @Column(name = "id_task", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_component", nullable = false)
    private ProjectEntregableComponent idComponent;

    @Size(max = 30)
    @NotNull
    @Column(name = "name_task", nullable = false, length = 30)
    private String nameTask;

    @NotNull
    @Column(name = "description_task", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionTask;

    @NotNull
    @Column(name = "status_task", nullable = false, length = Integer.MAX_VALUE)
    private String statusTask;

    @NotNull
    @Column(name = "path_task", nullable = false, length = Integer.MAX_VALUE)
    private String pathTask;

    @NotNull
    @Column(name = "base_percentage_task", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePercentageTask;

    @NotNull
    @Column(name = "actual_percentage_task", nullable = false, precision = 18, scale = 2)
    private BigDecimal actualPercentageTask;

    @NotNull
    @Column(name = "creationdate_task", nullable = false)
    private Date creationdateTask;

    @NotNull
    @Column(name = "updatedate_task", nullable = false)
    private Date updatedateTask;

    @NotNull
    @Column(name = "stimateddate_task", nullable = false)
    private Date stimateddateTask;

    @Column(name = "finishdate_task")
    private Date finishdateTask;

    @Size(max = 3)
    @NotNull
    @Column(name = "develop_status_task", nullable = false, length = 3)
    private String developStatusTask;

    @Column(name = "startdate_task")
    private Date startdateTask;

    @Column(name = "is_started")
    private Boolean isStarted;

    @Column(name = "percentageupdate_task")
    private Date percentageupdateTask;

}