package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenReqDTO {
    private String user_token;
}
