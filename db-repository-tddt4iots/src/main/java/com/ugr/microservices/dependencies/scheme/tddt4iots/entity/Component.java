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
@Table(name = "component")
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "component_id_gen")
    @SequenceGenerator(name = "component_id_gen", sequenceName = "component_id_component_seq", allocationSize = 1)
    @Column(name = "id_component", nullable = false)
    private Long id;

    @Size(max = 50)
    @Column(name = "name_component", length = 50)
    private String nameComponent;

    @Size(max = 200)
    @Column(name = "description_component", length = 200)
    private String descriptionComponent;

    @Size(max = 30)
    @Column(name = "type_component", length = 30)
    private String typeComponent;

    @Column(name = "active_component", length = Integer.MAX_VALUE)
    private String activeComponent;

    @Column(name = "pathimg_component", length = Integer.MAX_VALUE)
    private String pathimgComponent;

    @Column(name = "uploaddate_component")
    private Date uploaddateComponent;

    @Column(name = "pathports_component", length = Integer.MAX_VALUE)
    private String pathportsComponent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id_person", nullable = false)
    private Person personIdPerson;

}