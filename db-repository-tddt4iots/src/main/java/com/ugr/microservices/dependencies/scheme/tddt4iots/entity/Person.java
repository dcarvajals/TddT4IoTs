package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_id_gen")
    @SequenceGenerator(name = "person_id_gen", sequenceName = "person_id_person_seq", allocationSize = 1)
    @Column(name = "id_person", nullable = false)
    private Long id;

    @Size(max = 30)
    @Column(name = "name_person", length = 30)
    private String namePerson;

    @Size(max = 30)
    @Column(name = "lastname_person", length = 30)
    private String lastnamePerson;

    @Size(max = 50)
    @Column(name = "pathimg_person", length = 50)
    private String pathimgPerson;

    @Size(max = 15)
    @Column(name = "codeverifitacion_person", length = 15)
    private String codeverifitacionPerson;

    @Column(name = "dateverification_person")
    private Date dateverificationPerson;

    @Column(name = "type_person", length = Integer.MAX_VALUE)
    private String typePerson;

    @Size(max = 75)
    @Column(name = "email_person", length = 75)
    private String emailPerson;

    @Size(max = 64)
    @Column(name = "password_person", length = 64)
    private String passwordPerson;

    @Column(name = "datereg_person")
    private Date dateregPerson;

    @Size(max = 25)
    @Column(name = "provider_person", length = 25)
    private String providerPerson;

    @Size(max = 20)
    @Column(name = "id_city", length = 20)
    private String idCity;

    @Column(name = "openai_secret_key", length = Integer.MAX_VALUE)
    private String openaiSecretKey;

    @OneToMany(mappedBy = "personIdPerson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Component> components = new ArrayList<>();

    @OneToMany(mappedBy = "idPerson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Model> models = new ArrayList<>();

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ModelPermission> modelPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ModelUseTool> modelUseTools = new ArrayList<>();

    @OneToMany(mappedBy = "personIdPerson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PermitMaster> permitMasters = new ArrayList<>();

    @OneToMany(mappedBy = "idPerson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TrainingHistory> trainingHistories = new ArrayList<>();

}