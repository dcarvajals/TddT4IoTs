package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearchDatasetSampleDTO {
    private String projectId;
    private String split;
    private JsonNode inputRequirements;
    private JsonNode goldWorkspace;
}
