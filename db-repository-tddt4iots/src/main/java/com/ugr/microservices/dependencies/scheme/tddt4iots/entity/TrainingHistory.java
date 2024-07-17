package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "training_history")
public class TrainingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "training_history_id_gen")
    @SequenceGenerator(name = "training_history_id_gen", sequenceName = "training_history_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_person")
    private Person idPerson;

    @Size(max = 200)
    @Column(name = "commit", length = 200)
    private String commit;

    @Column(name = "date_creation")
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateCreation;

    @Column(name = "count_row_data_trining")
    private Long countRowDataTrining;

    @Column(name = "date_start_trining")
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateStartTrining;

    @Column(name = "date_end_trining")
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateEndTrining;

    @Size(max = 350)
    @Column(name = "path_file_json", length = 350)
    private String pathFileJson;

    @Size(max = 350)
    @Column(name = "path_file_csv", length = 350)
    private String pathFileCsv;

    @Column(name = "result_trining", length = Integer.MAX_VALUE)
    private String resultTrining;

    @Column(name = "status")
    private Character status;

    @OneToMany(mappedBy = "modelTraining")
    private List<ModelUseTool> modelUseTools = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_model", nullable = false)
    private ModelPermission model;

}