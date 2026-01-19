package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String role;
    private String content;
}
