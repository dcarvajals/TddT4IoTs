package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named="comparison.test.jdbcUrl",matches="jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/comparison_test")
class ProjectVersionPostgresTest {
 private final ObjectMapper j=new ObjectMapper();private JdbcTemplate jdbc;private ProjectVersionService versions;
 @BeforeEach void setup() throws Exception {
  jdbc=new JdbcTemplate(new DriverManagerDataSource(System.getProperty("comparison.test.jdbcUrl"),System.getProperty("user.name"),""));
  jdbc.execute("DROP SCHEMA IF EXISTS tddt4iots_research CASCADE; CREATE SCHEMA tddt4iots_research");
  for(String file:List.of("004_comparative_generations.sql","005_shared_model_catalog.sql","006_comparison_assessments.sql","007_project_model_versions.sql"))migration(file);
  versions=new ProjectVersionService(jdbc);
 }
 private void migration(String file)throws Exception{jdbc.execute(Files.readString(Path.of("../database/research/"+file)));}
 private ObjectNode snapshot(String name){ObjectNode s=j.createObjectNode();s.putArray("useCaseDiagrams").addObject().put("id","d1").put("name","Care").put("graph","<diagram/>").putObject("data");s.putObject("classModel").putArray("diagram");s.putArray("sequences");s.putArray("inventory").addObject().put("id","c1").put("kind","class").put("label",name);return s;}
 private UUID generated(long user,String project,String model){var input=GenerateCaseWorkspaceReqDTO.builder().projectId(project).build();ObjectNode meta=j.createObjectNode().put("requestedModel",model).put("returnedModel",model).put("inputSha256","same-input");return versions.recordGeneration(user,input,null,j.createObjectNode(),meta);}
 @Test void modelGenerationsAndCorrectionsKeepStableNumbersAndImmutableOrigins() throws Exception {
  UUID a=generated(1L,"p1","model-a");ObjectNode first=snapshot("Sensor");versions.imported(1L,a,j.createObjectNode().put("status","IMPORTED").set("workspace",first));
  ObjectNode body=j.createObjectNode().put("label","Expert correction").put("notes","REQ-02").put("parentId",a.toString());body.set("workspace",snapshot("Sensor corrected"));UUID c=versions.save(1L,"p1",body);
  UUID b=generated(1L,"p1","model-b");assertEquals(1,versions.detail(1L,a).path("version_number").asInt());assertEquals(2,versions.detail(1L,c).path("version_number").asInt());assertEquals(3,versions.detail(1L,b).path("version_number").asInt());
  assertEquals("CORRECTION",versions.detail(1L,c).path("kind").asText());assertEquals("model-a",versions.detail(1L,c).path("metadata").path("returnedModel").asText());assertEquals(first,versions.detail(1L,a).path("snapshot"));
  assertThrows(IllegalArgumentException.class,()->versions.imported(1L,a,j.createObjectNode().put("status","IMPORTED").set("workspace",snapshot("Overwrite"))));
  assertEquals(3,versions.list(1L,"p1").size());assertTrue(versions.list(2L,"p1").isEmpty());assertThrows(IllegalArgumentException.class,()->versions.detail(2L,a));assertThrows(IllegalArgumentException.class,()->versions.save(1L,"other-project",body));
  UUID own=generated(2L,"p1","other-owner");assertEquals(1,versions.detail(2L,own).path("version_number").asInt());
 }
 @Test void manualSnapshotsDoNotInventModelAttributionOrNewGenerations(){
  ObjectNode body=j.createObjectNode().put("label","Existing manual work");body.set("workspace",snapshot("Sensor"));UUID manual=versions.save(1L,"p1",body);
  ObjectNode row=versions.detail(1L,manual);assertEquals("MANUAL",row.path("kind").asText());assertTrue(row.path("metadata").isEmpty());assertTrue(row.path("run_id").isNull());
  body.put("parentId",manual.toString()).put("label","Manual refinement");UUID correction=versions.save(1L,"p1",body);assertEquals("CORRECTION",versions.detail(1L,correction).path("kind").asText());assertFalse(versions.detail(1L,correction).path("metadata").has("returnedModel"));
 }
 @Test void oldResearchRunsAreBackfilledOnlyOnceAndFailuresRemainVisible() throws Exception {
  jdbc.execute("DROP TABLE tddt4iots_research.ai_project_version");var ledger=new ComparisonEvidenceService(jdbc);
  var input=GenerateCaseWorkspaceReqDTO.builder().projectId("p1").research(ComparisonRunContextDTO.builder().experimentCode("study").phase("PILOT").systemCode("S1").requirementsVersion("1").repetition(1).attempt(1).build()).build();
  var request=GrpcUseModelReqDTO.builder().model("model-a").build();UUID success=ledger.begin(1L,2L,3L,input,"requirements",request);
  ledger.finish(success,"SERVER_VALIDATED","provider","{}",j.createObjectNode(),"model-a",null,42,null);
  ledger.reportImport(1L,success,j.createObjectNode().put("status","IMPORTED").set("workspace",snapshot("Sensor")));
  input.getResearch().setAttempt(2);UUID failed=ledger.begin(1L,2L,3L,input,"requirements",request);ledger.finish(failed,"FAILED",null,null,null,"model-a",null,100,"timeout");
  migration("007_project_model_versions.sql");migration("007_project_model_versions.sql");
  assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM tddt4iots_research.ai_project_version",Integer.class));assertEquals("model-a",versions.detail(1L,success).path("metadata").path("returnedModel").asText());
  assertEquals(2,versions.list(1L,"p1").size());assertTrue(versions.list(1L,"p1").toString().contains("ATTEMPT"));
 }
}
