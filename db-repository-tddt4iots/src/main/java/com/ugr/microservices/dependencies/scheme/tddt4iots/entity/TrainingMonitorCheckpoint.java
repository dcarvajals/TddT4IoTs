package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter @Setter @Entity
@Table(name = "training_monitor_checkpoint", uniqueConstraints = @UniqueConstraint(columnNames = "provider_checkpoint_id"))
public class TrainingMonitorCheckpoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_history_id", nullable = false)
    private TrainingHistory trainingHistory;
    @Column(name = "provider_checkpoint_id", nullable = false, length = 200)
    private String providerCheckpointId;
    @Column(name = "fine_tuned_model", length = 250)
    private String fineTunedModel;
    @Column(name = "step_number")
    private Long stepNumber;
    @Column(name = "provider_created_at")
    private Long providerCreatedAt;
    @Column(name = "metrics_payload", columnDefinition = "TEXT")
    private String metricsPayload;
    @Column(name = "captured_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date capturedAt;
}
