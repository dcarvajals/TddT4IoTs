package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UseCaseDiagramDTO {
    private String name;
    private String porpuse;
    private String pre_condition;
    private String post_condition;
    private String description_interpret;
    private List<MainStageDTO> main_stage;
    private List<AlternativeFlowDTO> alternative_flow;
}
