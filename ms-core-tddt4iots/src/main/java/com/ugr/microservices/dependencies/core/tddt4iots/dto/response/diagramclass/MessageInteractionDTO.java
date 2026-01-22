package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageInteractionDTO {
    private String from;
    private String to;
    private String messageName;
    private String type; // sync, async, return
    private String order; // Para asegurar la secuencia lógica
}
