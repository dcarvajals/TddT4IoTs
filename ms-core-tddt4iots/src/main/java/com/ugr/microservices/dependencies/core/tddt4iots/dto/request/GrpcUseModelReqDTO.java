package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.MessageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class GrpcUseModelReqDTO {
    private String openAiSecretKey;
    private String model;
    private List<MessageDTO> messages;
    /** Optional server-side schema identifier. Keeps ordinary JSON calls backwards compatible. */
    private String responseSchema;
    private Integer maxOutputTokens;
    @Builder.Default
    private Double temperature = 0.2;
    private String reasoningEffort;
}
