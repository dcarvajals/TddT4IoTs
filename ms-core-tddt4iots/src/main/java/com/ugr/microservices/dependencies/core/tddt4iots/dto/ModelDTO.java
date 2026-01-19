package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelDTO {
    private Long id;

    @javax.validation.constraints.NotNull
    @javax.validation.constraints.Size(max = 200)
    private String name;

    @javax.validation.constraints.Size(max = 200)
    private String description;

    private Date createAt;
    private Boolean active;
    private PersonDTO idPerson;

}