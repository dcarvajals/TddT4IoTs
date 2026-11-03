package com.ugr.microservices.dependencies.core.tddt4iots.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String providerJobId;
    private String providerStatus;
    private Double progressPercent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date lastMonitoredAt;
    private Integer monitoringFailures;
    private ModelPermissionDTO model;
}
