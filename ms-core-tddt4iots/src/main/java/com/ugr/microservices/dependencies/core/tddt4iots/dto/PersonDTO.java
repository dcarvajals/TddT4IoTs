package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
    private Long id;
    private String namePerson;
    private String lastnamePerson;
    private String pathimgPerson;
    private String codeverifitacionPerson;
    private Date dateverificationPerson;
    private String typePerson;
    private String emailPerson;
    private String passwordPerson;
    private Instant dateregPerson;
    private String providerPerson;
    private String idCity;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String openaiSecretKey;
}
