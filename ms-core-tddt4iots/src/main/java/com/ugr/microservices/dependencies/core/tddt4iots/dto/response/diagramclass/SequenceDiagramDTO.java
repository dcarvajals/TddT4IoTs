package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SequenceDiagramDTO {
    private String caseOfUse;
    private List<String> lifelines; // Objetos/Clases que participan
    private List<MessageInteractionDTO> messages;
}
