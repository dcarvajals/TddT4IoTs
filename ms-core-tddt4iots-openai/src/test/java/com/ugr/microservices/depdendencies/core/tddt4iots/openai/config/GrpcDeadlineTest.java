package com.ugr.microservices.depdendencies.core.tddt4iots.openai.config;

import com.ugr.microservices.dependencies.core.tddt4iots.config.GrpcClientConfigurationTddt4iots;
import grpc_service.OpenAIServiceGrpc;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcDeadlineTest {
    @Test
    void configuredDeadlineLetsThePythonProviderTimeoutReturnBeforeGrpcCloses() throws Exception {
        Properties properties = new Properties();
        try (var stream = getClass().getResourceAsStream("/application.properties")) {
            properties.load(stream);
        }
        int seconds = Integer.parseInt(properties.getProperty("grpc.deadline.after.timeout"));
        var config = mock(GrpcClientConfigurationTddt4iots.class);
        when(config.getManagedChannelMap()).thenReturn(Map.of("integrator", mock(ManagedChannel.class)));
        var manager = new GrpcConnectionManager(config);
        ReflectionTestUtils.setField(manager, "deadlineAfterTimeline", seconds);
        OpenAIServiceGrpc.OpenAIServiceBlockingStub stub = manager.getStubByChannel(
                "integrator", OpenAIServiceGrpc.class, "newBlockingStub");
        long remaining = stub.getCallOptions().getDeadline().timeRemaining(TimeUnit.SECONDS);
        assertTrue(remaining > 600, "gRPC must allow the Python 600-second timeout to return");
        assertTrue(remaining <= seconds);
    }
}
