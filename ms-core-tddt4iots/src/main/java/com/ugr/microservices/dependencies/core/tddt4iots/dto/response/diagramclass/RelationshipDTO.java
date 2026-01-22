package com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipDTO {
    private String from;
    private String to;
    private String typeRelatioship; // Respetando el nombre de tu prompt
    private String value;
    private String cardinalidate;   // Respetando el nombre de tu prompt
    private String from_fk;
    private String to_fk;
    private String simbol;
}
