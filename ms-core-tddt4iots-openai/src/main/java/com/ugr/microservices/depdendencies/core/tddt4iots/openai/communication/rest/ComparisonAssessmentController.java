package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ComparisonAssessmentService;
import com.ugr.microservices.dependencies.core.tddt4iots.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins="*",methods={RequestMethod.GET,RequestMethod.POST})
@RequestMapping("/research")
public class ComparisonAssessmentController {
    private final Tddt4iotsGenericBO sessions;
    private final ComparisonAssessmentService assessments;
    @GetMapping("/comparison-runs/{id}/assessments")
    public GenericBasicResponse<Object> detail(@RequestHeader String userToken,@PathVariable UUID id)throws GenericException,IOException,InterruptedException {
        return response(assessments.detail(sessions.validateSession(userToken).getId(),id));
    }
    @GetMapping("/assigned-comparison-runs")
    public GenericBasicResponse<Object> assigned(@RequestHeader String userToken)throws GenericException,IOException,InterruptedException {
        return response(assessments.assigned(sessions.validateSession(userToken).getId()));
    }
    @GetMapping("/comparison-runs/{id}/review-workspace")
    public GenericBasicResponse<Object> workspace(@RequestHeader String userToken,@PathVariable UUID id)throws GenericException,IOException,InterruptedException {
        return response(assessments.workspace(sessions.validateSession(userToken).getId(),id));
    }
    @PostMapping("/comparison-runs/{id}/assessments/{action}")
    public GenericBasicResponse<Object> save(@RequestHeader String userToken,@PathVariable UUID id,@PathVariable String action,@RequestBody JsonNode body)throws GenericException,IOException,InterruptedException {
        Long user=sessions.validateSession(userToken).getId();
        try {
            Object result=switch(action){
                case "reference" -> assessments.reference(user,id,body);
                case "evaluate" -> assessments.evaluate(user,id,body);
                case "review" -> assessments.review(user,id,body);
                case "effort" -> assessments.effort(user,id,body);
                case "cost" -> assessments.cost(user,id,body);
                case "assign" -> { assessments.assign(user,id,body.path("reviewerId").isIntegralNumber()?body.path("reviewerId").asLong():null);yield true; }
                default -> throw new IllegalArgumentException("Operación de evaluación no válida.");
            };return response(result);
        }catch(IllegalArgumentException e){throw new GenericException(e.getMessage());}
        catch(org.springframework.dao.DuplicateKeyException e){throw new GenericException("Este registro ya existe. Use una nueva versión o ronda; las evaluaciones anteriores se conservan.");}
    }
    @GetMapping(value="/comparison-assessments.csv",produces="text/csv")
    public ResponseEntity<String> csv(@RequestHeader String userToken,@RequestParam(required=false)String projectId)throws GenericException,IOException,InterruptedException {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=comparison-assessments.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(assessments.csv(sessions.validateSession(userToken).getId(),projectId));
    }
    private static GenericBasicResponse<Object> response(Object value){var r=new GenericBasicResponse<Object>();r.setData(value);return r;}
}
