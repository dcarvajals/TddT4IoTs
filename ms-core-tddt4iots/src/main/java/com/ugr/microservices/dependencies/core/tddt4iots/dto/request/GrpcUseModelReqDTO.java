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
}
