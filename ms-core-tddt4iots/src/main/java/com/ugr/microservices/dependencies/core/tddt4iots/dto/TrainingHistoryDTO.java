package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainingHistoryDTO {
    private Long id;
    private PersonDTO idPerson;
    private String commit;
    private Date dateCreation;
    private Long countRowDataTrining;
    private Date dateStartTrining;
    private Date dateEndTrining;
    private String pathFileJson;
    private String pathFileCsv;
    private String resultTrining;
    private Character status;
    private ModelPermissionDTO model;
}
