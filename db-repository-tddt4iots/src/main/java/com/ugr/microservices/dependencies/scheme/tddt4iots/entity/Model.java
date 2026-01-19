package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "model")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_gen")
    @SequenceGenerator(name = "model_id_gen", sequenceName = "model_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 200)
    @NotNull
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_person", nullable = false)
    private Person idPerson;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "active")
    private Boolean active;

    @OneToMany(mappedBy = "model")
    private List<ModelPermission> modelPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "model")
    private List<ModelUseTool> modelUseTools = new ArrayList<>();

}