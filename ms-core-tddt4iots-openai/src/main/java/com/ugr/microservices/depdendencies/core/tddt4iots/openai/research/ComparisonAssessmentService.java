package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ComparisonAssessmentService {
    private static final ObjectMapper J=new ObjectMapper();
    @Qualifier("jdbcTddt4iots") private final JdbcTemplate jdbc;
    public static final List<String> CRITERIA=List.of("correctness","completeness","crossViewCoherence","traceability","readability");
    private Map<String,Object> run(Long user,UUID id,boolean ownerOnly) {
        List<Map<String,Object>> rows=jdbc.queryForList("""
            SELECT r.* FROM tddt4iots_research.ai_comparison_run r WHERE r.id=? AND
            (r.researcher_id=? OR (?=false AND EXISTS(SELECT 1 FROM tddt4iots_research.ai_comparison_assignment a WHERE a.run_id=r.id AND a.reviewer_id=?)))
            """,id,user,ownerOnly,user);
        if(rows.isEmpty())throw new IllegalArgumentException("Ejecución no disponible para este usuario.");return rows.getFirst();
    }
    private static JsonNode json(Object value){try{return value==null?J.nullNode():J.readTree(value.toString());}catch(Exception e){throw new IllegalStateException("Evidencia JSON dañada.",e);}}
    private ObjectNode automatic(Map<String,Object> r){
        if(r.get("parsed_workspace")==null){ObjectNode empty=J.createObjectNode().put("artifactStage","UNAVAILABLE").put("classMetricsAvailable",false);empty.putObject("counts");empty.putArray("inventory");empty.putArray("coherence");return empty;}
        return ComparisonMetrics.analyze(json(r.get("parsed_workspace")),r.get("original_imported_workspace")==null?null:json(r.get("original_imported_workspace")));
    }
    private static String required(JsonNode body,String key,int max){String value=body.path(key).asText("").trim();if(value.isEmpty()||value.length()>max)throw new IllegalArgumentException(key+" es obligatorio (máximo "+max+").");return value;}
    private static UUID uuid(JsonNode body,String key){try{return UUID.fromString(required(body,key,100));}catch(Exception e){throw new IllegalArgumentException(key+" debe ser UUID.");}}
    private static void imported(Map<String,Object> r){if(!"IMPORTED".equals(r.get("client_status"))||r.get("original_imported_workspace")==null)throw new IllegalArgumentException("Se requiere el UML inicial importado, sin correcciones.");}
    public ObjectNode detail(Long user,UUID id) {
        Map<String,Object> r=run(user,id,false);ObjectNode out=J.createObjectNode();
        out.put("runId",id.toString()).put("currentUserId",user).put("model",Objects.toString(r.get("returned_model"),Objects.toString(r.get("requested_model"),""))).put("owner",Objects.equals(((Number)r.get("researcher_id")).longValue(),user));
        out.put("imported","IMPORTED".equals(r.get("client_status")));out.set("automatic",automatic(r));out.set("usage",json(r.get("usage")));
        Map<String,Object> study=jdbc.queryForMap("SELECT experiment_code,phase,system_code,requirements_version,input_sha256,effective_input FROM tddt4iots_research.ai_comparison_case WHERE id=?",r.get("case_id"));
        out.set("study",J.valueToTree(study));
        for(String type:List.of("reference","evaluation","review","effort","cost")) {
            List<Map<String,Object>> records=type.equals("reference")?jdbc.queryForList("SELECT * FROM tddt4iots_research.ai_comparison_reference WHERE case_id=? ORDER BY created_at",r.get("case_id"))
                :jdbc.queryForList("SELECT * FROM tddt4iots_research.ai_comparison_"+type+" WHERE run_id=? ORDER BY "+(type.equals("effort")?"started_at":"created_at"),id);
            ArrayNode a=out.putArray(type+"s");for(Map<String,Object> row:records){ObjectNode item=a.addObject();row.forEach((k,v)->{if(Set.of("inventory","correspondence","results","scores","baseline","corrected_workspace","change_log","change_summary","tariff","result").contains(k))item.set(k,json(v));else item.set(k,J.valueToTree(v instanceof UUID?v.toString():v));});}
        }
        out.put("rubricVersion","expert-uml-v1");out.set("criteria",J.valueToTree(CRITERIA));return out;
    }
    public List<Map<String,Object>> assigned(Long user){return jdbc.queryForList("""
        SELECT r.id,r.project_id,r.requested_model,c.system_code,c.phase FROM tddt4iots_research.ai_comparison_assignment a
        JOIN tddt4iots_research.ai_comparison_run r ON r.id=a.run_id JOIN tddt4iots_research.ai_comparison_case c ON c.id=r.case_id
        WHERE a.reviewer_id=? ORDER BY a.created_at DESC
        """,user);}
    public void assign(Long user,UUID id,Long reviewer){run(user,id,true);if(reviewer==null||reviewer<1)throw new IllegalArgumentException("reviewerId debe ser un identificador de usuario válido.");
        Integer count=jdbc.queryForObject("SELECT count(*) FROM person WHERE id_person=?",Integer.class,reviewer);
        if(count==null||count!=1)throw new IllegalArgumentException("El usuario revisor no existe.");
        jdbc.update("INSERT INTO tddt4iots_research.ai_comparison_assignment(run_id,reviewer_id,assigned_by) VALUES(?,?,?) ON CONFLICT DO NOTHING",id,reviewer,user);}
    public UUID reference(Long user,UUID id,JsonNode body){Map<String,Object> r=run(user,id,true);if(!body.path("approved").asBoolean())throw new IllegalArgumentException("Debe confirmarse la revisión de la referencia.");
        String version=required(body,"version",100),provenance=required(body,"provenance",10000);ArrayNode inventory=ComparisonMetrics.validateInventory(body.path("inventory"));
        UUID ref=UUID.randomUUID();jdbc.update("""
          INSERT INTO tddt4iots_research.ai_comparison_reference(id,case_id,version,approved_by,provenance,inventory,sha256) VALUES(?,?,?,?,?,?::jsonb,?)
          """,ref,r.get("case_id"),version,user,provenance,inventory.toString(),ComparisonEvidenceService.sha256(inventory.toString()));return ref;}
    public UUID evaluate(Long user,UUID id,JsonNode body){Map<String,Object> r=run(user,id,false);imported(r);
        if(!body.path("reviewComplete").asBoolean())throw new IllegalArgumentException("Debe confirmarse la revisión completa de las correspondencias y elementos sin pareja.");
        UUID ref=uuid(body,"referenceId");List<Map<String,Object>> refs=jdbc.queryForList("SELECT inventory FROM tddt4iots_research.ai_comparison_reference WHERE id=? AND case_id=?",ref,r.get("case_id"));
        if(refs.isEmpty())throw new IllegalArgumentException("La referencia no corresponde a esta versión de requisitos.");
        JsonNode matches=body.path("matches");ObjectNode result=ComparisonMetrics.compare(automatic(r).path("inventory"),json(refs.getFirst().get("inventory")),matches);String rationale=required(body,"rationale",10000);
        UUID evaluation=UUID.randomUUID();jdbc.update("""
          INSERT INTO tddt4iots_research.ai_comparison_evaluation(id,run_id,reference_id,evaluator_id,evaluator_version,correspondence,results,rationale) VALUES(?,?,?,?,?,?::jsonb,?::jsonb,?)
          """,evaluation,id,ref,user,ComparisonMetrics.VERSION,matches.toString(),result.toString(),rationale);return evaluation;}
    public UUID review(Long user,UUID id,JsonNode body){Map<String,Object> r=run(user,id,false);imported(r);JsonNode scores=body.path("scores");ObjectNode clean=J.createObjectNode();
        for(String key:CRITERIA){JsonNode value=scores.path(key);if(!value.isIntegralNumber()||value.asLong()<1||value.asLong()>5)throw new IllegalArgumentException("Puntuación de "+key+": entero de 1 a 5.");clean.set(key,value);}
        int round=body.path("round").asInt(0);if(!body.path("round").isIntegralNumber()||!body.path("round").canConvertToInt()||round<1)throw new IllegalArgumentException("round debe ser un entero positivo.");
        UUID review=UUID.randomUUID();jdbc.update("INSERT INTO tddt4iots_research.ai_comparison_review(id,run_id,reviewer_id,round,rubric_version,scores,rationale) VALUES(?,?,?,?,?,?::jsonb,?)",review,id,user,round,"expert-uml-v1",clean.toString(),required(body,"rationale",10000));return review;}
    @Transactional
    public UUID effort(Long user,UUID id,JsonNode body){Map<String,Object> r=run(user,id,false);imported(r);String action=required(body,"action",20);
        if(action.equals("start")){
            JsonNode baseline=automatic(r).path("inventory");
            if(!baseline.equals(ComparisonMetrics.validateInventory(body.path("workspace").path("inventory"))))
                throw new IllegalArgumentException("El lienzo debe coincidir con el UML inicial antes de iniciar la medición. Restaure primero el resultado original.");
            UUID session=UUID.randomUUID();jdbc.update("""
          INSERT INTO tddt4iots_research.ai_comparison_effort(id,run_id,reviewer_id,status,active_since,baseline) VALUES(?,?,?,'ACTIVE',CURRENT_TIMESTAMP,?::jsonb)
          """,session,id,user,automatic(r).path("inventory").toString());return session;}
        UUID session=uuid(body,"sessionId");List<Map<String,Object>> sessions=jdbc.queryForList("SELECT * FROM tddt4iots_research.ai_comparison_effort WHERE id=? AND run_id=? AND reviewer_id=? FOR UPDATE",session,id,user);
        if(sessions.isEmpty())throw new IllegalArgumentException("Sesión de corrección no disponible.");Map<String,Object> s=sessions.getFirst();String status=s.get("status").toString();
        if("COMPLETE".equals(status))throw new IllegalArgumentException("La sesión ya fue completada.");
        if(action.equals("log") || action.equals("log_batch")){
            ArrayNode stored=s.get("change_log")==null?J.createArrayNode():(ArrayNode)json(s.get("change_log"));
            ArrayNode incoming=J.createArrayNode();
            if(action.equals("log_batch")) {
                if(!body.path("changes").isArray() || body.path("changes").isEmpty() || body.path("changes").size()>20)
                    throw new IllegalArgumentException("Se requieren entre 1 y 20 eventos por lote.");
                incoming.addAll((ArrayNode)body.path("changes"));
            } else incoming.add(body.path("change"));
            Set<String> seen=new HashSet<>(); for(JsonNode old:stored) if(old.hasNonNull("eventId"))seen.add(old.path("eventId").asText());
            for(JsonNode item:incoming) {
                ObjectNode change=item.isObject()?((ObjectNode)item).deepCopy():J.createObjectNode();
                if(!Set.of("add","remove","modify","layout").contains(change.path("action").asText()))throw new IllegalArgumentException("Acción de corrección no válida.");
                required(change,"description",2000);
                if(action.equals("log_batch")) {
                    String eventId=required(change,"eventId",100); UUID.fromString(eventId);
                    // A retry after a lost acknowledgement must never duplicate an editing event.
                    if(seen.contains(eventId))continue;
                    if(!"ACTIVE".equals(status))throw new IllegalArgumentException("La captura automática requiere una sesión activa.");
                    if(!change.path("patches").isArray() || change.path("patches").isEmpty() || change.path("patches").size()>1000 || change.toString().length()>2_000_000)
                        throw new IllegalArgumentException("Evento de corrección demasiado grande o sin diferencias.");
                    for(JsonNode patch:change.path("patches")) {
                        required(patch,"path",2000);
                        if(!Set.of("add","remove","modify").contains(patch.path("action").asText()) || !patch.has("before") || !patch.has("after"))
                            throw new IllegalArgumentException("Diferencia de corrección no válida.");
                    }
                    required(change,"capturedAt",100); required(change,"trigger",100);
                    change.put("source","automatic"); seen.add(eventId);
                } else change.put("source","manual");
                change.put("receivedAt",java.time.Instant.now().toString());
                if(stored.size()>=10000)throw new IllegalArgumentException("Se alcanzó el límite de 10000 eventos de la sesión.");
                stored.add(change);
            }
            jdbc.update("UPDATE tddt4iots_research.ai_comparison_effort SET change_log=?::jsonb WHERE id=?",stored.toString(),session);
        }
        else if(action.equals("resume")){if(!"PAUSED".equals(status))throw new IllegalArgumentException("La sesión no está pausada.");jdbc.update("UPDATE tddt4iots_research.ai_comparison_effort SET status='ACTIVE',active_since=CURRENT_TIMESTAMP WHERE id=?",session);}
        else if(action.equals("pause")||action.equals("complete")) {
            if(action.equals("pause")&&!"ACTIVE".equals(status))throw new IllegalArgumentException("La sesión no está activa.");
            JsonNode corrected=body.path("workspace"),changes=s.get("change_log")==null?J.createArrayNode():json(s.get("change_log"));ObjectNode summary=null;
            if(action.equals("complete")){ArrayNode inventory=ComparisonMetrics.validateInventory(corrected.path("inventory"));if(!changes.isArray()||changes.size()>10000)throw new IllegalArgumentException("changes debe ser un array de acciones documentadas.");for(JsonNode change:changes){if(!Set.of("add","remove","modify","layout").contains(change.path("action").asText()))throw new IllegalArgumentException("Acción de corrección no válida.");required(change,"description",2000);}summary=ComparisonMetrics.changes(json(s.get("baseline")),inventory);summary.put("documentedActions",changes.size());int automaticEvents=0,manualActions=0,fieldChanges=0;for(JsonNode change:changes){if("automatic".equals(change.path("source").asText())){automaticEvents++;fieldChanges+=change.path("patches").size();}else manualActions++;}summary.put("automaticEvents",automaticEvents);summary.put("manualAnnotations",manualActions);summary.put("observedFieldChanges",fieldChanges);}
            jdbc.update("""
              UPDATE tddt4iots_research.ai_comparison_effort SET
              active_ms=active_ms+CASE WHEN status='ACTIVE' THEN GREATEST(0,(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP-active_since))*1000)::bigint) ELSE 0 END,
              active_since=NULL,status=?,corrected_workspace=?::jsonb,change_log=?::jsonb,change_summary=?::jsonb,
              finished_at=CASE WHEN ?='COMPLETE' THEN CURRENT_TIMESTAMP ELSE NULL END WHERE id=?
              """,action.equals("complete")?"COMPLETE":"PAUSED",action.equals("complete")?corrected.toString():null,changes.toString(),summary==null?null:summary.toString(),action.equals("complete")?"COMPLETE":"PAUSED",session);
        }else throw new IllegalArgumentException("Acción no válida.");return session;
    }
    public ObjectNode workspace(Long user,UUID id){Map<String,Object> r=run(user,id,false);imported(r);ObjectNode out=J.createObjectNode();out.set("parsedWorkspace",json(r.get("parsed_workspace")));out.set("initialImportedWorkspace",json(r.get("original_imported_workspace")));return out;}
    public UUID cost(Long user,UUID id,JsonNode body){Map<String,Object> r=run(user,id,true);if(!body.path("confirmApplicable").asBoolean())throw new IllegalArgumentException("Confirme la aplicabilidad de la tarifa al modelo, nivel de servicio, contexto y región.");
        required(body,"source",2000);required(body,"effectiveDate",40);required(body,"serviceTier",40);required(body,"contextTier",40);required(body,"region",100);
        String actual=Objects.toString(r.get("returned_model"),Objects.toString(r.get("requested_model"),""));if(!actual.equals(required(body,"model",200)))throw new IllegalArgumentException("La tarifa debe identificar el modelo devuelto por el proveedor.");
        JsonNode envelope=J.nullNode();String provider=Objects.toString(r.get("provider_response"),"");try{int offset=provider.indexOf("Processing result:");if(offset>=0)envelope=J.readTree(provider.substring(offset+"Processing result:".length()));}catch(Exception ignored){}
        if(envelope.hasNonNull("service_tier")&&!envelope.path("service_tier").asText().equals(body.path("serviceTier").asText()))throw new IllegalArgumentException("El nivel de servicio no coincide con la respuesta del proveedor.");
        ObjectNode result=ComparisonMetrics.cost(json(r.get("usage")),body);result.put("model",actual).put("excluded","taxes, tools, infrastructure and unrelated requests");UUID cost=UUID.randomUUID();
        jdbc.update("INSERT INTO tddt4iots_research.ai_comparison_cost(id,run_id,calculator_id,tariff,result,amount_usd) VALUES(?,?,?,?::jsonb,?::jsonb,?)",cost,id,user,body.toString(),result.toString(),result.path("amountUsd").isNumber()?result.path("amountUsd").decimalValue():null);return cost;}
    public String csv(Long user,String project){StringBuilder csv=new StringBuilder("run_id,system,model,record_type,record_id,reviewer,metric,value,tp,fp,fn\n");
        for(Map<String,Object> row:jdbc.queryForList("SELECT r.id,c.system_code,COALESCE(r.returned_model,r.requested_model) model FROM tddt4iots_research.ai_comparison_run r JOIN tddt4iots_research.ai_comparison_case c ON c.id=r.case_id WHERE r.researcher_id=? AND (?::text IS NULL OR r.project_id=?)",user,project,project)) {
            UUID id=(UUID)row.get("id");ObjectNode d=detail(user,id);JsonNode automatic=d.path("automatic");
            automatic.path("counts").fields().forEachRemaining(e->{if(d.path("imported").asBoolean()||!Set.of("class","class_relation","attribute","operation").contains(e.getKey()))line(csv,row,"structure","","",e.getKey(),e.getValue().asText(),"","","");});
            int n=0;for(JsonNode c:automatic.path("coherence"))line(csv,row,"coherence",Integer.toString(n++),"",c.path("rule").asText(),c.path("status").asText(),"","","");
            for(JsonNode e:d.path("evaluations")){ArrayNode groups=(ArrayNode)e.path("results").path("groups").deepCopy();groups.add(e.path("results").path("micro"));for(JsonNode g:groups)for(String m:List.of("precision","recall","f1"))line(csv,row,"reference:"+e.path("reference_id").asText(),e.path("id").asText(),e.path("evaluator_id").asText(),g.path("kind").asText()+":"+m,g.path(m).isNull()?"":g.path(m).asText(),g.path("tp").asText(),g.path("fp").asText(),g.path("fn").asText());}
            for(JsonNode e:d.path("reviews"))e.path("scores").fields().forEachRemaining(s->line(csv,row,"review:round="+e.path("round").asText(),e.path("id").asText(),e.path("reviewer_id").asText(),s.getKey(),s.getValue().asText(),"","",""));
            for(JsonNode e:d.path("efforts"))if("COMPLETE".equals(e.path("status").asText())){line(csv,row,"effort",e.path("id").asText(),e.path("reviewer_id").asText(),"active_ms",e.path("active_ms").asText(),"","","");e.path("change_summary").fields().forEachRemaining(s->{if(s.getValue().isNumber())line(csv,row,"effort",e.path("id").asText(),e.path("reviewer_id").asText(),s.getKey(),s.getValue().asText(),"","","");});}
            for(JsonNode e:d.path("costs"))line(csv,row,"cost:"+e.path("result").path("status").asText(),e.path("id").asText(),e.path("calculator_id").asText(),"estimated_usd",e.path("amount_usd").isNull()?"":e.path("amount_usd").asText(),"","","");
        }return csv.toString();}
    private static void line(StringBuilder out,Map<String,Object> r,String... fields){List<String> cells=new ArrayList<>(List.of(r.get("id").toString(),r.get("system_code").toString(),r.get("model").toString()));cells.addAll(Arrays.asList(fields));for(int i=0;i<cells.size();i++){if(i>0)out.append(',');String v=cells.get(i);if(v.matches("^[=+@\\-].*"))v="'"+v;out.append('"').append(v.replace("\"","\"\"")).append('"');}out.append('\n');}
}
