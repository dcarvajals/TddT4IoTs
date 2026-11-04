package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: only an isolated database named comparison_test may be used. */
@EnabledIfSystemProperty(named = "comparison.test.jdbcUrl", matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/comparison_test")
class ComparisonEvidencePostgresTest {
    private JdbcTemplate jdbc;
    private ComparisonEvidenceService service;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setup() throws Exception {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(System.getProperty("comparison.test.jdbcUrl"),
                System.getProperty("user.name"), ""));
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS tddt4iots_research");
        jdbc.execute("DROP SCHEMA tddt4iots_research CASCADE; CREATE SCHEMA tddt4iots_research");
        String migration = Files.readString(Path.of("../database/research/004_comparative_generations.sql"));
        jdbc.execute(migration);
        jdbc.execute(Files.readString(Path.of("../database/research/005_shared_model_catalog.sql")));
        jdbc.execute(migration); // Reapplying the additive migration must be harmless.
        jdbc.execute(Files.readString(Path.of("../database/research/005_shared_model_catalog.sql")));
        jdbc.execute(Files.readString(Path.of("../database/research/006_comparison_assessments.sql")));
        jdbc.execute(Files.readString(Path.of("../database/research/006_comparison_assessments.sql")));
        service = new ComparisonEvidenceService(jdbc);
    }

    private GenerateCaseWorkspaceReqDTO input(long project, String phase, int repetition, int attempt) {
        return GenerateCaseWorkspaceReqDTO.builder().projectId("opaque-project-" + project).research(ComparisonRunContextDTO.builder()
                .experimentCode("study").phase(phase).systemCode("S1").requirementsVersion("1.0")
                .repetition(repetition).attempt(attempt).build()).build();
    }

    private GrpcUseModelReqDTO request(String model) {
        return GrpcUseModelReqDTO.builder().model(model).maxOutputTokens(16000).responseSchema("case_workspace").build();
    }

    @Test
    void fixesTheSameInputAcrossSeparateModelProjectsAndRejectsSilentChanges() {
        service.begin(1L, 10L, 100L, input(21, "EVALUATION", 1, 1), "requirements", request("model-a"));
        service.begin(1L, 11L, 101L, input(22, "EVALUATION", 1, 1), "requirements", request("model-b"));
        assertThrows(IllegalArgumentException.class, () -> service.begin(1L, 11L, 101L,
                input(22, "EVALUATION", 2, 1), "different requirements", request("model-b")));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM tddt4iots_research.ai_comparison_case", Integer.class));
        assertEquals(2, service.list(1L, null).size());
    }

    @Test
    void duplicateAttemptIsRejectedAndNewAttemptIsRetainedAsAnotherObservation() {
        GenerateCaseWorkspaceReqDTO input = input(21, "EVALUATION", 1, 1);
        service.begin(1L, 10L, 100L, input, "requirements", request("model-a"));
        assertThrows(IllegalArgumentException.class, () -> service.begin(1L, 10L, 100L, input, "requirements", request("model-a")));
        service.begin(1L, 10L, 100L, input(21, "EVALUATION", 1, 2), "requirements", request("model-a"));
        assertEquals(2, service.list(1L, "opaque-project-21").size());
    }

    @Test
    void pilotAndEvaluationInputsAreKeptSeparate() {
        service.begin(1L, 10L, 100L, input(21, "PILOT", 1, 1), "pilot", request("model-a"));
        service.begin(1L, 10L, 100L, input(21, "EVALUATION", 1, 1), "evaluation", request("model-a"));
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM tddt4iots_research.ai_comparison_case", Integer.class));
    }

    @Test
    void baseAndTrainedRegistrationsWithTheSameIdRemainDistinctConditions() {
        var input = input(21, "EVALUATION", 1, 1);
        service.begin(1L, 10L, 3L, "BASE_MODEL", input, "requirements", request("base"));
        service.begin(1L, 10L, 3L, "TRAINED_MODEL", input, "requirements", request("trained"));
        assertEquals(2, service.list(1L, null).size());
        assertTrue(service.csv(1L, null).contains("BASE_MODEL"));
    }

    @Test
    void missingProviderUsageDoesNotHideTheOriginalGenerationError() {
        for (String status : new String[]{"FAILED", "REFUSED", "SERVER_VALIDATED"}) {
            int repetition = status.equals("FAILED") ? 1 : status.equals("REFUSED") ? 2 : 3;
            UUID id = service.begin(1L, 10L, 100L, input(21, "EVALUATION", repetition, 1),
                    "requirements", request("model-a"));
            var envelope = json.createObjectNode();
            service.finish(id, status, envelope.toString(), null,
                    status.equals("SERVER_VALIDATED") ? json.createObjectNode() : envelope.path("workspace"),
                    "model-a", envelope.path("usage"), 10, "original provider error");
            var stored = service.detail(1L, id);
            assertEquals(status, stored.get("status"));
            assertNull(stored.get("usage"));
            assertEquals("original provider error", stored.get("error_message"));
            if (!status.equals("SERVER_VALIDATED")) assertNull(stored.get("parsed_workspace"));
        }
    }

    @Test
    void preservesFailuresAndProtectsEvidenceFromOtherUsersAndOverwrite() throws Exception {
        UUID id = service.begin(1L, 10L, 100L, input(21, "EVALUATION", 1, 1), "requirements", request("model-a"));
        service.finish(id, "SERVER_VALIDATED", "provider-envelope", "  {\"original\":true}  ",
                json.createObjectNode(), "model-snapshot", json.readTree("{\"prompt_tokens\":20,\"completion_tokens\":30,\"total_tokens\":50}"), 250, null);
        ObjectNode report = json.createObjectNode().put("status", "IMPORTED");
        ObjectNode snapshot = report.putObject("workspace");
        snapshot.putArray("useCaseDiagrams"); snapshot.putObject("classModel").put("original", true);
        assertThrows(IllegalArgumentException.class, () -> service.reportImport(2L, id, report));
        service.reportImport(1L, id, report);
        assertThrows(IllegalArgumentException.class, () -> service.reportImport(1L, id, report));
        assertThrows(IllegalArgumentException.class, () -> service.detail(2L, id));
        assertTrue(service.list(2L, "opaque-project-21").isEmpty());
        assertEquals("  {\"original\":true}  ", service.detail(1L, id).get("raw_output"));
        assertTrue(service.csv(1L, null).contains("\"50\""));
        UUID failure = service.begin(1L, 10L, 100L, input(21, "EVALUATION", 2, 1), "requirements", request("model-a"));
        service.finish(failure, "FAILED", "truncated-provider-response", "{", null, "model-a", null, 10, "incomplete");
        assertEquals("FAILED", service.detail(1L, failure).get("status"));
        assertEquals(2, service.list(1L, null).size());
    }
    @Test
    void assessmentsAreScopedVersionedAndDerivedFromConfirmedMatches() throws Exception {
        var assessments = new ComparisonAssessmentService(jdbc);
        UUID id = service.begin(1L, 10L, 100L, input(21, "PILOT", 1, 1), "requirements", request("model-a"));
        service.finish(id,"SERVER_VALIDATED","provider","{}",json.createObjectNode(),"model-a",json.readTree("{\"prompt_tokens\":1000,\"completion_tokens\":200}"),100,null);
        var inventory=json.createArrayNode();inventory.addObject().put("id","c1").put("kind","class").put("label","Sensor");
        var snapshot=json.createObjectNode();snapshot.set("inventory",inventory);snapshot.putArray("useCaseDiagrams");snapshot.putObject("classModel");
        service.reportImport(1L,id,json.createObjectNode().put("status","IMPORTED").set("workspace",snapshot));
        assertEquals(1, ((com.fasterxml.jackson.databind.JsonNode)service.detail(1L,id).get("automatic_metrics")).path("counts").path("class").asInt());
        assertThrows(IllegalArgumentException.class,()->assessments.detail(2L,id));
        var reference=json.createObjectNode().put("approved",true).put("version","ref-1").put("provenance","Reviewed against REQ-01");reference.set("inventory",inventory);
        UUID ref=assessments.reference(1L,id,reference);
        assertThrows(org.springframework.dao.DuplicateKeyException.class,()->assessments.reference(1L,id,reference));
        var comparison=json.createObjectNode().put("referenceId",ref.toString()).put("reviewComplete",true).put("rationale","REQ-01: same entity");
        comparison.putArray("matches").addObject().put("generatedId","c1").put("referenceId","c1");assessments.evaluate(1L,id,comparison);
        assertEquals(1.0,assessments.detail(1L,id).path("evaluations").get(0).path("results").path("micro").path("f1").asDouble());
        jdbc.execute("CREATE TABLE IF NOT EXISTS person(id_person BIGINT PRIMARY KEY)");jdbc.update("INSERT INTO person(id_person) VALUES(2) ON CONFLICT DO NOTHING");
        assessments.assign(1L,id,2L);assertEquals(1,assessments.assigned(2L).size());
        var review=json.createObjectNode().put("round",1).put("rationale","Checked REQ-01");var scores=review.putObject("scores");for(String criterion:ComparisonAssessmentService.CRITERIA)scores.put(criterion,4);
        assessments.review(2L,id,review);assertThrows(org.springframework.dao.DuplicateKeyException.class,()->assessments.review(2L,id,review));
        assertThrows(IllegalArgumentException.class,()->assessments.reference(2L,id,reference));
        assertThrows(IllegalArgumentException.class,()->assessments.review(3L,id,review));
        assertTrue(assessments.csv(1L,null).contains("crossViewCoherence"));assertFalse(assessments.csv(3L,null).contains("Sensor"));
        var tariff=json.createObjectNode().put("model","model-a").put("source","https://developers.openai.com/api/docs/pricing").put("effectiveDate","2026-11-03").put("serviceTier","default").put("contextTier","short").put("region","global").put("confirmApplicable",true)
            .put("inputPerMillion",10).put("cachedPerMillion",1).put("cacheWritePerMillion",12.5).put("outputPerMillion",50);
        assessments.cost(1L,id,tariff);assertEquals(.02,assessments.detail(1L,id).path("costs").get(0).path("amount_usd").asDouble(),.000001);
        assertThrows(IllegalArgumentException.class,()->assessments.cost(2L,id,tariff));
    }

    @Test
    void effortExcludesPausedIntervalsPersistsLogsAndProtectsOriginalUml() throws Exception {
        var assessments=new ComparisonAssessmentService(jdbc);
        UUID id=service.begin(1L,10L,100L,input(21,"PILOT",1,1),"requirements",request("model-a"));
        service.finish(id,"SERVER_VALIDATED","provider","{}",json.createObjectNode(),"model-a",null,100,null);
        var inventory=json.createArrayNode();inventory.addObject().put("id","c1").put("kind","class").put("label","Sensor");
        var snapshot=json.createObjectNode();snapshot.set("inventory",inventory);snapshot.putArray("useCaseDiagrams");snapshot.putObject("classModel");
        service.reportImport(1L,id,json.createObjectNode().put("status","IMPORTED").set("workspace",snapshot));
        var start=json.createObjectNode().put("action","start");start.set("workspace",snapshot);UUID session=assessments.effort(1L,id,start);
        jdbc.update("UPDATE tddt4iots_research.ai_comparison_effort SET active_since=CURRENT_TIMESTAMP-INTERVAL '2 seconds' WHERE id=?",session);
        var action=json.createObjectNode().put("sessionId",session.toString()).put("action","pause");assessments.effort(1L,id,action);
        long paused=jdbc.queryForObject("SELECT active_ms FROM tddt4iots_research.ai_comparison_effort WHERE id=?",Long.class,session);assertTrue(paused>=2000&&paused<5000);
        action.put("action","log");action.putObject("change").put("action","add").put("description","Added REQ-02 caregiver");assessments.effort(1L,id,action);
        action.put("action","resume");assessments.effort(1L,id,action);
        jdbc.update("UPDATE tddt4iots_research.ai_comparison_effort SET active_since=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE id=?",session);
        var corrected=snapshot.deepCopy();((com.fasterxml.jackson.databind.node.ArrayNode)corrected.path("inventory")).addObject().put("id","c2").put("kind","class").put("label","Caregiver");
        action.put("action","complete");action.set("workspace",corrected);assessments.effort(1L,id,action);
        var effort=assessments.detail(1L,id).path("efforts").get(0);assertTrue(effort.path("active_ms").asLong()>=paused+1000);assertEquals(1,effort.path("change_summary").path("added").asInt());assertEquals(1,effort.path("change_summary").path("documentedActions").asInt());
        assertEquals(snapshot,service.detail(1L,id).get("original_imported_workspace"));assertThrows(IllegalArgumentException.class,()->assessments.effort(1L,id,action));
    }

    @Test
    void automaticCorrectionEventsAreIdempotentScopedAndSurvivePause() throws Exception {
        var assessments=new ComparisonAssessmentService(jdbc);
        UUID id=service.begin(1L,10L,100L,input(21,"PILOT",1,1),"requirements",request("model-a"));
        service.finish(id,"SERVER_VALIDATED","provider","{}",json.createObjectNode(),"model-a",null,100,null);
        var snapshot=json.createObjectNode();snapshot.putArray("inventory").addObject().put("id","c1").put("kind","class").put("label","Sensor");snapshot.putArray("useCaseDiagrams");snapshot.putObject("classModel");
        service.reportImport(1L,id,json.createObjectNode().put("status","IMPORTED").set("workspace",snapshot));
        UUID session=assessments.effort(1L,id,json.createObjectNode().put("action","start").set("workspace",snapshot));
        var body=json.createObjectNode().put("action","log_batch").put("sessionId",session.toString());
        var event=body.putArray("changes").addObject().put("eventId",UUID.randomUUID().toString()).put("action","modify").put("description","Observed UML workspace change").put("capturedAt","2026-11-03T12:00:00Z").put("trigger","pointerup");
        event.putArray("patches").addObject().put("path","/classGraph").put("action","modify").put("before","x=1").put("after","x=2");
        assessments.effort(1L,id,body);assessments.effort(1L,id,body);
        var action=json.createObjectNode().put("sessionId",session.toString()).put("action","pause");assessments.effort(1L,id,action);
        var stored=assessments.detail(1L,id).path("efforts").get(0).path("change_log");
        assertEquals(1,stored.size());assertEquals("automatic",stored.get(0).path("source").asText());assertTrue(stored.get(0).hasNonNull("receivedAt"));
        assessments.effort(1L,id,body); // An acknowledged event can be retried while paused.
        event.put("eventId",UUID.randomUUID().toString());assertThrows(IllegalArgumentException.class,()->assessments.effort(1L,id,body));
        assertThrows(IllegalArgumentException.class,()->assessments.effort(2L,id,body));
        action.put("action","resume");assessments.effort(1L,id,action);assessments.effort(1L,id,body);
        action.put("action","complete");action.set("workspace",snapshot);assessments.effort(1L,id,action);
        var result=assessments.detail(1L,id).path("efforts").get(0);
        assertEquals(2,result.path("change_summary").path("automaticEvents").asInt());
        assertEquals(2,result.path("change_summary").path("observedFieldChanges").asInt());
        assertEquals(snapshot,service.detail(1L,id).get("original_imported_workspace"));
    }

}
