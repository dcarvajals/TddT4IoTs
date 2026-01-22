package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodDTO {
    private String name;
    private String visibility; // public, private, protected
    private String returnType; // int, void, String, etc.
    private List<ParameterDTO> parameters; // Lista de argumentos del método
    private String modifiers;
    private String type;
}
