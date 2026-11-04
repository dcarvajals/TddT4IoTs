package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.grpc.GrpcTrainingModelOpenAi;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.ModelUseToolMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ComparisonEvidenceService;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.ModelUseTool;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.ModelUseToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ComparisonGenerationTest {
    private final ObjectMapper json = new ObjectMapper();
    private final GrpcTrainingModelOpenAi provider = mock(GrpcTrainingModelOpenAi.class);
    private final ComparisonEvidenceService evidence = mock(ComparisonEvidenceService.class);
    private final UUID run = UUID.randomUUID();
    private MasterProjectBOImpl bo;
    private ModelUseToolDto selected;
    private GenericTddt4iotsReqDTO<GenerateCaseWorkspaceReqDTO> request;

    @BeforeEach
    void setup() throws Exception {
        bo = new MasterProjectBOImpl(null);
        Tddt4iotsGenericBO sessions = mock(Tddt4iotsGenericBO.class);
        ModelUseToolService selections = mock(ModelUseToolService.class);
        ModelUseToolMapper mapper = mock(ModelUseToolMapper.class);
        ModelCredentialsResolver credentials = mock(ModelCredentialsResolver.class);
        ReflectionTestUtils.setField(bo, "tddt4iotsGenericBO", sessions);
        ReflectionTestUtils.setField(bo, "modelUseToolService", selections);
        ReflectionTestUtils.setField(bo, "modelUseToolMapper", mapper);
        ReflectionTestUtils.setField(bo, "modelCredentialsResolver", credentials);
        ReflectionTestUtils.setField(bo, "comparisonEvidenceService", evidence);
        ReflectionTestUtils.setField(bo, "grpcTrainingModelOpenAi", provider);
        ReflectionTestUtils.setField(bo, "projectVersionService", mock(com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ProjectVersionService.class));
        when(sessions.validateSession("session")).thenReturn(PersonDTO.builder().id(1L).build());
        ModelUseTool entity = new ModelUseTool();
        when(selections.getModelUseToolByPerson(1L)).thenReturn(entity);
        selected = ModelUseToolDto.builder().modelTraining(TrainingHistoryDTO.builder()
                .id(7L).idPerson(PersonDTO.builder().id(10L).build())
                .resultTrining("{\"code\":0,\"data\":{\"model\":\"test-model\"}}").build()).build();
        when(mapper.modelUseToolDtoTo(entity)).thenReturn(selected);
        when(credentials.resolve(selected)).thenReturn("owner-test-key");
        when(evidence.begin(eq(1L), eq(10L), eq(7L), eq("TRAINED_MODEL"), any(), anyString(), any())).thenReturn(run);
        request = GenericTddt4iotsReqDTO.<GenerateCaseWorkspaceReqDTO>builder().userToken("session")
                .classDTO(GenerateCaseWorkspaceReqDTO.builder().projectId("opaque-project-ref").description("Natural language requirements")
                        .reasoningEffort("low").maxOutputTokens(24000)
                        .research(ComparisonRunContextDTO.builder().experimentCode("study").phase("EVALUATION")
                                .systemCode("S1").requirementsVersion("1.0").repetition(2).attempt(1).build()).build()).build();
    }

    private String envelope(String finish, String refusal) throws Exception {
        JsonNode workspace = json.readTree("""
                {"format":"TDDT4IOTS_CASE_WORKSPACE","schemaVersion":"1.0","diagrams":[
                {"id":"d1","name":"Area","actors":[{"id":"a1"}],"relations":[],"useCases":[
                {"id":"u1","mainFlow":[{"actor":"a1","action":"Request"}],"sequence":{
                "participants":[{"id":"p1"},{"id":"p2"}],"messages":[{"from":"p1","to":"p2"}]}}]}]}
                """);
        return "Processing result: " + json.createObjectNode().put("code", 0).put("model", "test-snapshot")
                .put("data", "  " + workspace + "  ").put("finish_reason", finish).put("refusal", refusal);
    }

    @Test
    void sendsOwnerCredentialsAndConfiguredParametersAndPreservesUnmodifiedOutput() throws Exception {
        when(provider.useModel(anyString())).thenReturn(envelope("stop", null));
        JsonNode result = bo.generateCaseWorkspace(request);
        assertEquals(run.toString(), result.path("_research").path("runId").asText());
        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(provider).useModel(sent.capture());
        JsonNode actual = json.readTree(sent.getValue());
        assertEquals("owner-test-key", actual.path("openAiSecretKey").asText());
        assertEquals("low", actual.path("reasoningEffort").asText());
        assertEquals(24000, actual.path("maxOutputTokens").asInt());
        String instruction = actual.path("messages").get(0).path("content").asText();
        assertTrue(instruction.contains("startStep <= returnStep <= N"));
        assertTrue(instruction.contains("For outcome=end, returnStep must be null"));
        assertTrue(actual.path("temperature").isNull());
        ArgumentCaptor<String> raw = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<JsonNode> parsed = ArgumentCaptor.forClass(JsonNode.class);
        verify(evidence).finish(eq(run), eq("SERVER_VALIDATED"), anyString(), raw.capture(), parsed.capture(),
                eq("test-snapshot"), any(), anyLong(), isNull());
        assertTrue(raw.getValue().startsWith("  {"));
        assertFalse(parsed.getValue().has("_research"));
    }

    @Test
    void projectModelOverridesGlobalSelectionAndArchivesTheActualModel() throws Exception {
        var catalog = mock(SharedModelCatalogService.class);
        var archive = mock(com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ProjectVersionService.class);
        var versionId = UUID.randomUUID();
        ReflectionTestUtils.setField(bo,"sharedModelCatalogService",catalog);
        ReflectionTestUtils.setField(bo,"projectVersionService",archive);
        var entity = new ModelUseTool();
        when(catalog.resolveForRequest(1L,8L,"TRAINED_MODEL")).thenReturn(entity);
        var projectSelection = ModelUseToolDto.builder().modelTraining(TrainingHistoryDTO.builder().id(8L)
                .idPerson(PersonDTO.builder().id(20L).build()).resultTrining("{\"code\":0,\"data\":{\"model\":\"project-model\"}}").build()).build();
        var mapper=(ModelUseToolMapper)ReflectionTestUtils.getField(bo,"modelUseToolMapper");
        when(mapper.modelUseToolDtoTo(entity)).thenReturn(projectSelection);
        var credentials=(ModelCredentialsResolver)ReflectionTestUtils.getField(bo,"modelCredentialsResolver");
        when(credentials.resolve(projectSelection)).thenReturn("project-owner-key");
        request.getClassDTO().setModelRegistrationId(8L);request.getClassDTO().setModelRegistrationKind("TRAINED_MODEL");
        when(evidence.begin(eq(1L),eq(20L),eq(8L),eq("TRAINED_MODEL"),any(),anyString(),any())).thenReturn(run);
        when(archive.recordGeneration(eq(1L),any(),eq(run),any(),any())).thenReturn(versionId);
        when(provider.useModel(anyString())).thenReturn(envelope("stop",null));
        JsonNode result=bo.generateCaseWorkspace(request);
        assertEquals(versionId.toString(),result.path("_version").path("id").asText());
        var sent=ArgumentCaptor.forClass(String.class);verify(provider).useModel(sent.capture());
        assertEquals("project-model",json.readTree(sent.getValue()).path("model").asText());
        assertEquals("project-owner-key",json.readTree(sent.getValue()).path("openAiSecretKey").asText());
        verify((ModelUseToolService)ReflectionTestUtils.getField(bo,"modelUseToolService"),never()).getModelUseToolByPerson(anyLong());
    }

    @Test
    void ordinaryGenerationIsArchivedWithoutInventingResearchEvidence() throws Exception {
        var archive=mock(com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ProjectVersionService.class);
        ReflectionTestUtils.setField(bo,"projectVersionService",archive);
        selected.setModelTraining(null);selected.setModel(ModelDTO.builder().id(3L).name("base-model").idPerson(PersonDTO.builder().id(10L).build()).build());
        request.getClassDTO().setResearch(null);
        UUID version=UUID.randomUUID();when(archive.recordGeneration(eq(1L),any(),isNull(),any(),any())).thenReturn(version);
        when(provider.useModel(anyString())).thenReturn(envelope("stop",null));
        JsonNode result=bo.generateCaseWorkspace(request);
        assertEquals(version.toString(),result.path("_version").path("id").asText());assertFalse(result.has("_research"));
        var original=ArgumentCaptor.forClass(JsonNode.class);var metadata=ArgumentCaptor.forClass(com.fasterxml.jackson.databind.node.ObjectNode.class);
        verify(archive).recordGeneration(eq(1L),any(),isNull(),original.capture(),metadata.capture());
        assertFalse(original.getValue().has("_version"));assertFalse(original.getValue().has("_research"));
        assertEquals("base-model",metadata.getValue().path("requestedModel").asText());
        assertFalse(metadata.getValue().toString().contains("owner-test-key"));
    }

    @Test
    void transportFailureIsRetainedWithoutAnotherProviderCall() throws Exception {
        when(provider.useModel(anyString())).thenThrow(new IllegalStateException("transport failure"));
        assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        verify(provider, times(1)).useModel(anyString());
        verify(evidence).finish(eq(run), eq("FAILED"), isNull(), isNull(), isNull(), eq("test-model"), any(), anyLong(), eq("transport failure"));
    }

    @Test
    void validLookingButTruncatedOutputIsStillAFailedObservation() throws Exception {
        when(provider.useModel(anyString())).thenReturn(envelope("length", null));
        assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        verify(evidence).finish(eq(run), eq("FAILED"), anyString(), anyString(), isNull(), eq("test-snapshot"), any(), anyLong(), contains("límite"));
    }

    @Test
    void invalidAlternativeReturnIsRejectedOnServerAndOriginalOutputIsRetained() throws Exception {
        String original = withAlternative("return", 2, json.getNodeFactory().numberNode(1));
        when(provider.useModel(anyString())).thenReturn(original);
        GenericException error = assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        assertTrue(error.getUserMessageError().contains("alternativeFlows[0].returnStep"));
        verify(evidence).finish(eq(run), eq("FAILED"), eq(original), anyString(), isNull(),
                eq("test-snapshot"), any(), anyLong(), contains("returnStep"));
        verify(provider, times(1)).useModel(anyString());
    }

    @Test
    void alternativeReturnBoundariesAndEndAreValidatedWithoutChangingModelOutput() throws Exception {
        for (JsonNode resume : new JsonNode[]{json.getNodeFactory().numberNode(2), json.getNodeFactory().numberNode(3)}) {
            when(provider.useModel(anyString())).thenReturn(withAlternative("return", 2, resume));
            assertDoesNotThrow(() -> bo.generateCaseWorkspace(request));
        }
        when(provider.useModel(anyString())).thenReturn(withAlternative("end", 2, json.nullNode()));
        assertDoesNotThrow(() -> bo.generateCaseWorkspace(request));
        for (JsonNode resume : new JsonNode[]{json.getNodeFactory().numberNode(0), json.getNodeFactory().numberNode(4),
                json.nullNode(), json.getNodeFactory().numberNode(2.5)}) {
            when(provider.useModel(anyString())).thenReturn(withAlternative("return", 2, resume));
            assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        }
        when(provider.useModel(anyString())).thenReturn(withAlternative("end", 2, json.getNodeFactory().numberNode(2)));
        assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        when(provider.useModel(anyString())).thenReturn(withAlternative("return", 4, json.getNodeFactory().numberNode(4)));
        assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
    }

    private String withAlternative(String outcome, int start, JsonNode resume) throws Exception {
        var envelope = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(envelope("stop", null).substring("Processing result: ".length()));
        var workspace = json.readTree(envelope.path("data").asText());
        var useCase = (com.fasterxml.jackson.databind.node.ObjectNode) workspace.path("diagrams").get(0).path("useCases").get(0);
        var steps = (com.fasterxml.jackson.databind.node.ArrayNode) useCase.path("mainFlow");
        steps.add(steps.get(0).deepCopy()); steps.add(steps.get(0).deepCopy());
        var flow = useCase.putArray("alternativeFlows").addObject().put("id", "af1").put("startStep", start)
                .put("actor", "System").put("condition", "Device offline").put("action", "Record incident")
                .put("outcome", outcome);
        flow.set("returnStep", resume);
        envelope.put("data", workspace.toString());
        return "Processing result: " + envelope;
    }

    @Test
    void providerRefusalIsDistinguishedFromMalformedUml() throws Exception {
        when(provider.useModel(anyString())).thenReturn(envelope("stop", "test refusal"));
        assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        verify(evidence).finish(eq(run), eq("REFUSED"), anyString(), anyString(), isNull(), eq("test-snapshot"), any(), anyLong(), contains("rechazó"));
    }

    @Test
    void requirementsStoredInDocxTablesAreExtracted() throws Exception {
        byte[] bytes;
        try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText("Project requirements");
            doc.createTable(1, 1).getRow(0).getCell(0).setText("REQ-01: Notify the caregiver");
            doc.write(out); bytes = out.toByteArray();
        }
        NaturalLanguageDocumentDTO source = NaturalLanguageDocumentDTO.builder().fileName("requirements.docx")
                .base64Content(java.util.Base64.getEncoder().encodeToString(bytes)).build();
        String extracted = ReflectionTestUtils.invokeMethod(bo, "extractNaturalLanguageDocument", source);
        assertNotNull(extracted);
        assertTrue(extracted.contains("REQ-01: Notify the caregiver"));
    }

    @Test
    void emptyDocumentsAreNotSilentlySkippedWhenDescriptionIsPresent() {
        request.getClassDTO().setDocuments(java.util.List.of(NaturalLanguageDocumentDTO.builder()
                .fileName("empty.txt").base64Content("").build()));
        assertThrows(GenericException.class, () -> bo.generateCaseWorkspace(request));
        verifyNoInteractions(provider, evidence);
    }

    @Test
    void directlyRegisteredBaseModelGeneratesWithoutCreatingATrainingJob() throws Exception {
        selected.setModelTraining(null);
        selected.setModel(ModelDTO.builder().id(3L).name("base-model-test").idPerson(PersonDTO.builder().id(10L).build()).build());
        when(evidence.begin(eq(1L), eq(10L), eq(3L), eq("BASE_MODEL"), any(), anyString(), any())).thenReturn(run);
        when(provider.useModel(anyString())).thenReturn(envelope("stop", null));
        assertEquals(run.toString(), bo.generateCaseWorkspace(request).path("_research").path("runId").asText());
        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(provider).useModel(sent.capture());
        assertEquals("base-model-test", json.readTree(sent.getValue()).path("model").asText());
    }
}
