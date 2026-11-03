package com.ugr.microservices.dependencies.scheme.tddt4iots.repository;

import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingMonitorSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingMonitorSnapshotRepository extends JpaRepository<TrainingMonitorSnapshot, Long> {
    List<TrainingMonitorSnapshot> findTop100ByTrainingHistoryIdOrderByCheckedAtDesc(Long trainingHistoryId);
}
