package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateTokenResDTO {
    private int status;
    private String information;
    private Data data;

    @Getter
    @Setter
    public class Data {
        private Long userId;
    }

}
