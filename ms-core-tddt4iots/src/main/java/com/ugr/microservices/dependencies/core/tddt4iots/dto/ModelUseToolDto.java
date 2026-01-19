package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelUseToolDto {
    private Long id;
    private ModelDTO model;
    private TrainingHistoryDTO modelTraining;
    private PersonDTO person;
    private Boolean active;
}