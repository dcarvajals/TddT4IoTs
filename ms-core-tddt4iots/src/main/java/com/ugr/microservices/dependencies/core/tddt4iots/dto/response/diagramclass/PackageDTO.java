package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDTO {
    private String id;
    private String packName; // Nombre del paquete (ej. AccessControlSystem)

    @JsonProperty("class")
    private List<ClassDTO> classList; // Lista de clases contenidas
    private List<EnumDTO> enums;
}
