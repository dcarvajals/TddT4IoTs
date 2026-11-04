package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.MessageDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonEvidenceServiceTest {
    @Test
    void invalidContextAndParametersAreRejectedBeforeExecution() {
        GenerateCaseWorkspaceReqDTO input = GenerateCaseWorkspaceReqDTO.builder()
                .projectId("opaque-project-ref").research(ComparisonRunContextDTO.builder().experimentCode("study")
                        .phase("EVALUATION").systemCode("S1").requirementsVersion("1.0")
                        .repetition(1).attempt(1).build()).build();
        assertDoesNotThrow(() -> ComparisonEvidenceService.validate(input));
        input.getResearch().setRepetition(0);
        assertThrows(IllegalArgumentException.class, () -> ComparisonEvidenceService.validate(input));
        input.getResearch().setRepetition(1);
        input.getResearch().setSystemCode("../S1");
        assertThrows(IllegalArgumentException.class, () -> ComparisonEvidenceService.validate(input));
        input.setResearch(null);
        input.setTemperature(Double.NaN);
        assertThrows(IllegalArgumentException.class, () -> ComparisonEvidenceService.validate(input));
        input.setTemperature(null);
        input.setMaxOutputTokens(0);
        assertThrows(IllegalArgumentException.class, () -> ComparisonEvidenceService.validate(input));
    }

    @Test
    void evidenceIncludesTheExactInstructionsAndSettingsWithoutCredentials() {
        GrpcUseModelReqDTO request = GrpcUseModelReqDTO.builder().model("test-model")
                .openAiSecretKey("private-test-key").maxOutputTokens(16000).temperature(null).reasoningEffort("low")
                .responseSchema("case_workspace")
                .messages(List.of(MessageDTO.builder().role("system").content("Instruction").build(),
                        MessageDTO.builder().role("user").content("Requisitos íntegros").build())).build();
        ObjectNode evidence = ComparisonEvidenceService.requestEvidence(request);
        assertEquals(600, evidence.path("timeoutSeconds").asInt());
        assertFalse(evidence.toString().contains("private-test-key"));
        assertFalse(evidence.has("openAiSecretKey"));
        assertEquals("Requisitos íntegros", evidence.path("messages").get(1).path("content").asText());
        assertEquals("low", evidence.path("reasoningEffort").asText());
        assertTrue(evidence.path("temperature").isNull());
        assertEquals(64, evidence.path("messagesSha256").asText().length());
        assertNotEquals(ComparisonEvidenceService.sha256("A"), ComparisonEvidenceService.sha256("A\n"));
    }

    @Test
    void importedStateRequiresTheInitialUmlSnapshot() {
        ComparisonEvidenceService service = new ComparisonEvidenceService(null);
        assertThrows(IllegalArgumentException.class, () -> service.reportImport(1L, java.util.UUID.randomUUID(),
                new ObjectMapper().createObjectNode().put("status", "IMPORTED")));
    }
}
