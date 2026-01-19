package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModelOpenAiRes {
    private String result;
    private String pathFileJson;
}
