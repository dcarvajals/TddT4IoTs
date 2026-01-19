package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainingDataDTO {
    private List<MessageDTO> messages;
}


