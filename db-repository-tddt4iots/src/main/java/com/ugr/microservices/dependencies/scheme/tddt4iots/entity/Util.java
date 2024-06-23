package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "utils")
public class Util {
    @Id
    @Size(max = 50)
    @SequenceGenerator(name = "utils_id_gen", sequenceName = "component_id_component_seq", allocationSize = 1)
    @Column(name = "key_u", nullable = false, length = 50)
    private String keyU;

    @Column(name = "value_u", length = Integer.MAX_VALUE)
    private String valueU;

}