package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFileTrainingResDTO {
    private String pathCsvFile;
    private String pathJsonFile;
    private Long idTrainingHistory;
}
