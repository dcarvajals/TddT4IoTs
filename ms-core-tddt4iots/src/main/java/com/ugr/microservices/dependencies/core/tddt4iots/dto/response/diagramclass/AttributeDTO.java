package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AttributeDTO {
    private String name;
    private String type;
    private String visibility;
    private String cardinalidate;
    private String idToOrFrom;
}
