package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCaseWorkspaceReqDTO {
    private String description;
    @Builder.Default
    private List<NaturalLanguageDocumentDTO> documents = new ArrayList<>();
}
