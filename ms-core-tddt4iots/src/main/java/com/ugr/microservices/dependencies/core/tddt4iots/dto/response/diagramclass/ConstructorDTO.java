package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ConstructorDTO {
    private String visibility;
    private List<ParameterDTO> parameters;
}
