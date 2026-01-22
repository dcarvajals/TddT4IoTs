package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ClassDTO {
    private String id;
    private String className;
    private String visibility;
    private String modifiers;
    private String action; // Requisito de NL asociado
    private List<ConstructorDTO> constructors;
    private List<AttributeDTO> attributes;
    private List<MethodDTO> methods;
}
