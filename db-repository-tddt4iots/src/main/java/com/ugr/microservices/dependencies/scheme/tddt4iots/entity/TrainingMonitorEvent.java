package com.ugr.microservices.dependencies.scheme.tddt4iots.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter @Setter @Entity
@Table(name = "training_monitor_event", uniqueConstraints = @UniqueConstraint(columnNames = "provider_event_id"))
public class TrainingMonitorEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_history_id", nullable = false)
    private TrainingHistory trainingHistory;
    @Column(name = "provider_event_id", nullable = false, length = 200)
    private String providerEventId;
    @Column(name = "level", length = 30)
    private String level;
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    @Column(name = "provider_created_at")
    private Long providerCreatedAt;
    @Column(name = "data_payload", columnDefinition = "TEXT")
    private String dataPayload;
    @Column(name = "captured_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date capturedAt;
}
