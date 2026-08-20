package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

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
public class NaturalLanguageDocumentDTO {
    private String fileName;
    private String contentType;
    private String base64Content;
}
