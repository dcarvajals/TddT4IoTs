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
    /** Opaque project reference supplied by the existing CASE workspace. */
    private String projectId;
    /** Explicit project choice; when absent the user's global selection applies. */
    private Long modelRegistrationId;
    private String modelRegistrationKind;
    /** Optional: ordinary generation remains available without comparison metadata. */
    private ComparisonRunContextDTO research;
    private Integer maxOutputTokens;
    private Double temperature;
    private String reasoningEffort;
    private String description;
    @Builder.Default
    private List<NaturalLanguageDocumentDTO> documents = new ArrayList<>();
}
