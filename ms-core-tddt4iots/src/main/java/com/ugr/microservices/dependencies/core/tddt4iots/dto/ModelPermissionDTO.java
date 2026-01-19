package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelPermissionDTO {
    private Long id;
    private PersonDTO person;
    private Boolean canUse;
    private Boolean canTrain;
    private Boolean active;
    private Boolean primaryTrain;
    private ModelDTO model;
}