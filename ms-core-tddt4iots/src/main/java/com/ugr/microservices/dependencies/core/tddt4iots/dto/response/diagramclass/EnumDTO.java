package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EnumDTO {
    private String name;
    private List<String> values; // Lista de las constantes del enumerado
    private String visibility;
}
