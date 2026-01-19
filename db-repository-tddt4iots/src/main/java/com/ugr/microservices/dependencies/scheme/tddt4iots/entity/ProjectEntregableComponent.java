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
@Table(name = "project_entregable_component")
public class ProjectEntregableComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_entregable_component_id_gen")
    @SequenceGenerator(name = "project_entregable_component_id_gen", sequenceName = "project_entregable_component_id_entregable_component_seq", allocationSize = 1)
    @Column(name = "id_entregable_component", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_entregable", nullable = false)
    private ProjectEntregable idEntregable;

    @Size(max = 30)
    @NotNull
    @Column(name = "name_component", nullable = false, length = 30)
    private String nameComponent;

    @NotNull
    @Column(name = "description_component", nullable = false, length = Integer.MAX_VALUE)
    private String descriptionComponent;

    @NotNull
    @Column(name = "status_component", nullable = false, length = Integer.MAX_VALUE)
    private String statusComponent;

    @NotNull
    @Column(name = "path_component", nullable = false, length = Integer.MAX_VALUE)
    private String pathComponent;

    @NotNull
    @Column(name = "base_percentage_component", nullable = false, precision = 18, scale = 2)
    private BigDecimal basePercentageComponent;

    @NotNull
    @Column(name = "actual_percentage_component", nullable = false, precision = 18, scale = 2)
    private BigDecimal actualPercentageComponent;

    @NotNull
    @Column(name = "creationdate_component", nullable = false)
    private Date creationdateComponent;

    @NotNull
    @Column(name = "updatedate_component", nullable = false)
    private Date updatedateComponent;

    @NotNull
    @Column(name = "stimateddate_component", nullable = false)
    private Date stimateddateComponent;

    @Column(name = "finishdate_component")
    private Date finishdateComponent;

    @Size(max = 3)
    @NotNull
    @Column(name = "develop_status_component", nullable = false, length = 3)
    private String developStatusComponent;

    @Column(name = "startdate_component")
    private Date startdateComponent;

    @Column(name = "percentageupdate_component")
    private Date percentageupdateComponent;

    @Column(name = "is_started")
    private Boolean isStarted;

    @OneToMany(mappedBy = "idComponent")
    private Set<EntregableComponentTask> entregableComponentTasks = new LinkedHashSet<>();

}