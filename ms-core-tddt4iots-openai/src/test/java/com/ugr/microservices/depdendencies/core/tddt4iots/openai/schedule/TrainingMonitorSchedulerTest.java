package com.ugr.microservices.depdendencies.core.tddt4iots.openai.schedule;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.grpc.GrpcTrainingModelOpenAi;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ResearchPersistenceService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.*;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingMonitorSchedulerTest {
    @Test
    void waitsForOwnerCredentialsAndResumesWithTheSameJob() {
        var histories = mock(TrainingHistoryRepository.class);
        var grpc = mock(GrpcTrainingModelOpenAi.class);
        var scheduler = new TrainingMonitorScheduler(histories, mock(TrainingMonitorSnapshotRepository.class),
                mock(TrainingMonitorEventRepository.class), mock(TrainingMonitorCheckpointRepository.class),
                grpc, mock(ResearchPersistenceService.class));
        var history = new TrainingHistory();
        var owner = new Person();
        owner.setOpenaiSecretKey(" ");
        history.setIdPerson(owner);
        history.setProviderJobId("ftjob-test");
        history.setProviderStatus("running");
        when(histories.findMonitorableTrainings()).thenReturn(List.of(history));
        scheduler.monitorActiveTrainings();
        verifyNoInteractions(grpc);
        verify(histories, never()).save(any());
        assertEquals("running", history.getProviderStatus());
        owner.setOpenaiSecretKey("test-owner-key");
        when(grpc.useModel(anyString())).thenReturn("Processing result: {\"status\":\"OK\",\"data\":{\"job\":{\"status\":\"succeeded\"}}}");
        scheduler.monitorActiveTrainings();
        verify(grpc).useModel(contains("test-owner-key"));
        assertEquals("succeeded", history.getProviderStatus());
    }
}
