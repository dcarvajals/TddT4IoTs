package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WriteReadFileReq {
    private String pathAbsolute;
    private String content;
}
