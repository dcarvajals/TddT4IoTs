package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.GenerateCaseWorkspaceReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectVersionService {
 private static final ObjectMapper J=new ObjectMapper();
 @Qualifier("jdbcTddt4iots") private final JdbcTemplate jdbc;
 @Value("${tddt4iots.build.revision:local-development}") private String revision;
 private static JsonNode json(Object value){try{return value==null?J.nullNode():J.readTree(value.toString());}catch(Exception e){throw new IllegalStateException("Versión JSON dañada.",e);}}
 private static String project(String value){if(value==null||value.isBlank()||value.length()>512)throw new IllegalArgumentException("Identificador de proyecto no válido.");return value;}
 private static String text(JsonNode body,String key,int max){String s=body.path(key).asText("").trim();if(s.isEmpty()||s.length()>max)throw new IllegalArgumentException(key+" es obligatorio (máximo "+max+").");return s;}
 @Transactional
 public UUID recordGeneration(Long user,GenerateCaseWorkspaceReqDTO input,UUID run,JsonNode source,ObjectNode metadata){
   if(input.getProjectId()==null||input.getProjectId().isBlank())return null;
   UUID id=run==null?UUID.randomUUID():run;String model=metadata.path("returnedModel").asText(metadata.path("requestedModel").asText("Model"));
   metadata.put("sourceCommit",revision==null?"local-development":revision);
   jdbc.update("""
     INSERT INTO tddt4iots_research.ai_project_version(id,researcher_id,project_id,version_number,run_id,kind,label,source_workspace,metadata)
     VALUES(?,?,?,?,?,'GENERATED',?,?::jsonb,?::jsonb)
     """,id,user,project(input.getProjectId()),nextNumber(user,input.getProjectId()),run,model.length()>180?model.substring(0,180):model,source.toString(),metadata.toString());return id;
 }
 private Map<String,Object> row(Long user,UUID id){List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM tddt4iots_research.ai_project_version WHERE id=? AND researcher_id=?",id,user);if(rows.isEmpty())throw new IllegalArgumentException("Versión no disponible.");return rows.getFirst();}
 public ObjectNode detail(Long user,UUID id){Map<String,Object> row=row(user,id);ObjectNode result=J.createObjectNode();row.forEach((key,value)->{
   if(Set.of("snapshot","metadata","source_workspace").contains(key))result.set(key,json(value));else result.set(key,J.valueToTree(value instanceof UUID?value.toString():value instanceof java.sql.Timestamp t?t.toInstant().toString():value));});
   if(row.get("source_workspace")!=null||row.get("snapshot")!=null){ObjectNode metrics=ComparisonMetrics.analyze("GENERATED".equals(row.get("kind"))?json(row.get("source_workspace")):null,row.get("snapshot")==null?null:json(row.get("snapshot")));if(!"GENERATED".equals(row.get("kind")))metrics.put("artifactStage","MANUAL".equals(row.get("kind"))?"SAVED_MANUAL":"SAVED_CORRECTION");result.set("metrics",metrics);}return result;}
 public ArrayNode list(Long user,String project){ArrayNode result=J.createArrayNode();
   for(Map<String,Object> item:jdbc.queryForList("SELECT id FROM tddt4iots_research.ai_project_version WHERE researcher_id=? AND project_id=? ORDER BY created_at,id",user,project(project))){ObjectNode version=detail(user,(UUID)item.get("id"));version.remove(List.of("snapshot","source_workspace"));JsonNode metrics=version.get("metrics");if(metrics!=null&&metrics.isObject())((ObjectNode)metrics).remove("inventory");result.add(version);}
   // Retain failed recorded attempts in the history; they have no loadable artifact.
   for(Map<String,Object> r:jdbc.queryForList("SELECT r.id,r.status,r.error_message,r.started_at,r.requested_model,r.returned_model,r.repetition,r.attempt,c.input_sha256,c.requirements_version,c.phase FROM tddt4iots_research.ai_comparison_run r JOIN tddt4iots_research.ai_comparison_case c ON c.id=r.case_id WHERE r.researcher_id=? AND r.project_id=? AND NOT EXISTS(SELECT 1 FROM tddt4iots_research.ai_project_version v WHERE v.id=r.id)",user,project)){
     ObjectNode item=result.addObject().put("id",r.get("id").toString()).put("run_id",r.get("id").toString()).put("kind","ATTEMPT").put("label","Recorded attempt").put("import_status",r.get("status").toString()).put("error",Objects.toString(r.get("error_message"),"")).put("created_at",((java.sql.Timestamp)r.get("started_at")).toInstant().toString());
     item.putObject("metadata").put("requestedModel",r.get("requested_model").toString()).put("returnedModel",Objects.toString(r.get("returned_model"),"")).put("repetition",((Number)r.get("repetition")).intValue()).put("attempt",((Number)r.get("attempt")).intValue()).put("inputSha256",r.get("input_sha256").toString()).put("requirementsVersion",r.get("requirements_version").toString()).put("phase",r.get("phase").toString());
   }return result;}
 public void imported(Long user,UUID id,JsonNode report){row(user,id);String state=text(report,"status",24);
   if(!Set.of("IMPORTED","VALIDATION_FAILED","IMPORT_FAILED","CAPTURE_FAILED","DECLINED").contains(state))throw new IllegalArgumentException("Estado de importación no válido.");
   JsonNode snapshot=report.path("workspace");if(state.equals("IMPORTED"))validateSnapshot(snapshot);
   int updated=jdbc.update("UPDATE tddt4iots_research.ai_project_version SET snapshot=?::jsonb,import_status=? WHERE id=? AND researcher_id=? AND kind='GENERATED' AND import_status='PENDING'",state.equals("IMPORTED")?snapshot.toString():null,state,id,user);
   if(updated!=1)throw new IllegalArgumentException("La importación inicial de esta versión ya fue registrada.");
 }
 @Transactional
 public UUID save(Long user,String project,JsonNode body){project(project);String label=text(body,"label",180),notes=body.path("notes").asText("");if(notes.length()>10000)throw new IllegalArgumentException("Las notas superan 10000 caracteres.");JsonNode snapshot=body.path("workspace");validateSnapshot(snapshot);
   UUID parent=null,run=null;ObjectNode metadata=J.createObjectNode();String kind="MANUAL";
   if(body.hasNonNull("parentId")){try{parent=UUID.fromString(body.path("parentId").asText());}catch(Exception e){throw new IllegalArgumentException("parentId no válido.");}Map<String,Object> previous=row(user,parent);
     if(!project.equals(previous.get("project_id")))throw new IllegalArgumentException("La versión de origen pertenece a otro proyecto.");
     if(!Set.of("IMPORTED","SAVED").contains(previous.get("import_status"))&&previous.get("source_workspace")==null)throw new IllegalArgumentException("Abra una versión con un artefacto antes de guardar una corrección.");
     metadata=(ObjectNode)json(previous.get("metadata"));run=(UUID)previous.get("run_id");kind="CORRECTION";metadata.put("derivedFrom",parent.toString());
   }
   UUID id=UUID.randomUUID();jdbc.update("""
     INSERT INTO tddt4iots_research.ai_project_version(id,researcher_id,project_id,version_number,parent_id,run_id,kind,label,notes,snapshot,metadata,import_status)
     VALUES(?,?,?,?,?,?,?,?, ?,?::jsonb,?::jsonb,'SAVED')
     """,id,user,project,nextNumber(user,project),parent,run,kind,label,notes,snapshot.toString(),metadata.toString());return id;
 }
 private Long nextNumber(Long user,String project){
   jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",user+":"+project);
   return jdbc.queryForObject("SELECT COALESCE(MAX(version_number),0)+1 FROM tddt4iots_research.ai_project_version WHERE researcher_id=? AND project_id=?",Long.class,user,project);
 }
 static void validateSnapshot(JsonNode snapshot){if(!snapshot.isObject()||!snapshot.path("useCaseDiagrams").isArray()||snapshot.path("useCaseDiagrams").isEmpty()||!snapshot.path("classModel").isObject()||!snapshot.path("sequences").isArray())throw new IllegalArgumentException("La versión requiere diagramas, clases y secuencias del lienzo.");ComparisonMetrics.validateInventory(snapshot.path("inventory"));}
}
