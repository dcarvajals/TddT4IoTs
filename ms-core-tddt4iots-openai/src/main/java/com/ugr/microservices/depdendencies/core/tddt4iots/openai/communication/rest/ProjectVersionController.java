package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ProjectVersionService;
import com.ugr.microservices.dependencies.core.tddt4iots.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins="*",methods={RequestMethod.GET,RequestMethod.POST})
@RequestMapping("/research/project-versions")
public class ProjectVersionController {
 private final ProjectVersionService versions;
 private final Tddt4iotsGenericBO sessions;
 @GetMapping public GenericBasicResponse<Object> list(@RequestHeader String userToken,@RequestParam String projectId)throws GenericException,IOException,InterruptedException{return result(versions.list(sessions.validateSession(userToken).getId(),projectId));}
 @GetMapping("/{id}") public GenericBasicResponse<Object> detail(@RequestHeader String userToken,@PathVariable UUID id)throws GenericException,IOException,InterruptedException{return result(versions.detail(sessions.validateSession(userToken).getId(),id));}
 @PostMapping public GenericBasicResponse<Object> save(@RequestHeader String userToken,@RequestBody JsonNode body)throws GenericException,IOException,InterruptedException{return result(versions.save(sessions.validateSession(userToken).getId(),body.path("projectId").asText(),body));}
 @PostMapping("/{id}/import") public GenericBasicResponse<Object> imported(@RequestHeader String userToken,@PathVariable UUID id,@RequestBody JsonNode body)throws GenericException,IOException,InterruptedException{versions.imported(sessions.validateSession(userToken).getId(),id,body);return result(true);}
 @PostMapping("/compare") public GenericBasicResponse<Object> compare(@RequestHeader String userToken,@RequestBody JsonNode body)throws GenericException,IOException,InterruptedException{
   Long user=sessions.validateSession(userToken).getId();JsonNode ids=body.path("ids");if(!ids.isArray()||ids.size()<2||ids.size()>4)throw new IllegalArgumentException("Seleccione entre 2 y 4 versiones.");
   ArrayNode rows=new ObjectMapper().createArrayNode();String project=null;Set<UUID> seen=new HashSet<>();
   for(JsonNode id:ids){UUID uuid=UUID.fromString(id.asText());if(!seen.add(uuid))throw new IllegalArgumentException("No repita versiones.");var row=versions.detail(user,uuid);if(project!=null&&!project.equals(row.path("project_id").asText()))throw new IllegalArgumentException("Las versiones deben pertenecer al mismo proyecto.");project=row.path("project_id").asText();row.remove(List.of("snapshot","source_workspace"));if(row.path("metrics").isObject())((com.fasterxml.jackson.databind.node.ObjectNode)row.path("metrics")).remove("inventory");rows.add(row);}return result(rows);
 }
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<GenericBasicResponse<Object>> invalid(IllegalArgumentException e){var r=result(null);r.setStatus("ERROR");r.setMessage(e.getMessage());r.setCode(1);return ResponseEntity.badRequest().body(r);}
 private static GenericBasicResponse<Object> result(Object value){var r=new GenericBasicResponse<Object>();r.setData(value);return r;}
}
