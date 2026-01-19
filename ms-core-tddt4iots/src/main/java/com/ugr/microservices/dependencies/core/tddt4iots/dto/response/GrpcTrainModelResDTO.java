package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrpcTrainModelResDTO {
    @JsonProperty("code")
    private int code;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private FineTuningResDTO data;
}
