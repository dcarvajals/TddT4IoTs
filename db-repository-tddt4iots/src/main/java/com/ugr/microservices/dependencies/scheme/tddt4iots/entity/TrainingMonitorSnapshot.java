package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter @Setter @Entity
@Table(name = "training_monitor_snapshot")
public class TrainingMonitorSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_history_id", nullable = false)
    private TrainingHistory trainingHistory;
    @Column(name = "provider_job_id", nullable = false, length = 200)
    private String providerJobId;
    @Column(name = "provider_status", nullable = false, length = 40)
    private String providerStatus;
    @Column(name = "progress_percent")
    private Double progressPercent;
    @Column(name = "checked_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date checkedAt;
    @Column(name = "duration_ms")
    private Long durationMs;
    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
