package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DiagramDataResDTO {
    private List<PackageDTO> diagram;
    private List<RelationshipDTO> relationships;
    private List<SequenceDiagramDTO> sequences; // Nueva lista para diagramas de secuencia
    private String xmldiagram;

    private List<Object> action;
    private List<Object> notifications;
    private boolean edition;
}
