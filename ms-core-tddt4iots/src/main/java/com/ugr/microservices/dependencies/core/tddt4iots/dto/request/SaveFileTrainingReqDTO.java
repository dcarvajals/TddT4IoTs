package com.ugr.microservices.dependencies.core.tddt4iots.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class SaveFileTrainingReqDTO {
    private String filePath;
    private String fileContent;
}
