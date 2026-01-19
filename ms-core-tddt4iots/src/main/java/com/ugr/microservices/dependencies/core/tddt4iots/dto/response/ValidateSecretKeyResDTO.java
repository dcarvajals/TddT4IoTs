package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidateSecretKeyResDTO {
    private String secretKey;
    private boolean hasSecretKey;
}
