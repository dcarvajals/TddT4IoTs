package com.ugr.microservices.depdendencies.core.tddt4iots.openai.schedule;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ResearchPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResearchBackfillScheduler {
    private final ResearchPersistenceService researchPersistenceService;

    @Scheduled(initialDelayString = "${tddt4iots.research.backfill-initial-delay-ms:15000}",
            fixedDelayString = "${tddt4iots.research.backfill-delay-ms:3600000}")
    public void importMissingOperationalHistory() {
        try {
            int imported = researchPersistenceService.backfillExistingTrainings();
            if (imported > 0) log.info("Scientific backfill imported {} training histories", imported);
        } catch (Exception error) {
            log.warn("Scientific backfill is not available yet: {}", error.getMessage());
        }
    }
}
