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
public class DataDTO {
    List<UseCaseDiagramDTO>  useCase;
}
