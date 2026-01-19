package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelTrainingByPersonModelUseToolReqDTO {
    private Long idPersonModelUseTool;
    private Long idModelPermission;
    private Long idPersonModelTraining;
}
