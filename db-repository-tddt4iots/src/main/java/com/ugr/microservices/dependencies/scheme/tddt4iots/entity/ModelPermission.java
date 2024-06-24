package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "model_permission")
public class ModelPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_permission_id_gen")
    @SequenceGenerator(name = "model_permission_id_gen", sequenceName = "model_permission_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "can_use")
    private Boolean canUse;

    @Column(name = "can_train")
    private Boolean canTrain;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "primary_train")
    private Boolean primaryTrain;

    @OneToMany(mappedBy = "model")
    private List<TrainingHistory> trainingHistories = new ArrayList<>();

}