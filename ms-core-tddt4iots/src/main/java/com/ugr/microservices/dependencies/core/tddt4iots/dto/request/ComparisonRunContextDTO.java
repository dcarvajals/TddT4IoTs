package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonRunContextDTO {
    private String experimentCode;
    private String phase;
    private String systemCode;
    private String requirementsVersion;
    private Integer repetition;
    private Integer attempt;
}
