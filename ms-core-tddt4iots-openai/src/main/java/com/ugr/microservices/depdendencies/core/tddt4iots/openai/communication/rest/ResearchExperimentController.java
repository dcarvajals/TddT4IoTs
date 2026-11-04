package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ResearchPersistenceService;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ComparisonEvidenceService;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericBasicResponse;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RequestMapping(path = "/research")
public class ResearchExperimentController {
    private final ResearchPersistenceService researchPersistenceService;
    private final Tddt4iotsGenericBO genericBO;
    private final ComparisonEvidenceService comparisonEvidenceService;

    @GetMapping(path = "/comparison-runs", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<List<Map<String, Object>>> comparisonRuns(@RequestHeader String userToken, @RequestParam String projectId)
            throws GenericException, IOException, InterruptedException {
        PersonDTO person = genericBO.validateSession(userToken);
        GenericBasicResponse<List<Map<String, Object>>> response = new GenericBasicResponse<>();
        response.setData(comparisonEvidenceService.list(person.getId(), projectId));
        return response;
    }

    @GetMapping(path = "/comparison-runs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<Map<String, Object>> comparisonDetail(@RequestHeader String userToken, @PathVariable java.util.UUID id)
            throws GenericException, IOException, InterruptedException {
        PersonDTO person = genericBO.validateSession(userToken);
        GenericBasicResponse<Map<String, Object>> response = new GenericBasicResponse<>();
        response.setData(comparisonEvidenceService.detail(person.getId(), id));
        return response;
    }

    @PostMapping(path = "/comparison-runs/{id}/import", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<Boolean> comparisonImport(@RequestHeader String userToken, @PathVariable java.util.UUID id, @RequestBody JsonNode report)
            throws GenericException, IOException, InterruptedException {
        PersonDTO person = genericBO.validateSession(userToken);
        comparisonEvidenceService.reportImport(person.getId(), id, report);
        GenericBasicResponse<Boolean> response = new GenericBasicResponse<>();
        response.setData(true);
        return response;
    }

    @GetMapping(path = "/comparison-export.csv", produces = "text/csv")
    public ResponseEntity<String> comparisonCsv(@RequestHeader String userToken, @RequestParam(required = false) String projectId)
            throws GenericException, IOException, InterruptedException {
        PersonDTO person = genericBO.validateSession(userToken);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comparison-runs.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(comparisonEvidenceService.csv(person.getId(), projectId));
    }

    @GetMapping(path = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<ObjectNode> summary(@RequestHeader String userToken)
            throws GenericException, IOException, InterruptedException {
        genericBO.validateSession(userToken);
        GenericBasicResponse<ObjectNode> response = new GenericBasicResponse<>();
        response.setData(researchPersistenceService.summary());
        return response;
    }

    @GetMapping(path = "/review-items", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<List<Map<String, Object>>> reviewItems(@RequestHeader String userToken)
            throws GenericException, IOException, InterruptedException {
        genericBO.validateSession(userToken);
        GenericBasicResponse<List<Map<String, Object>>> response = new GenericBasicResponse<>();
        response.setData(researchPersistenceService.reviewItems());
        return response;
    }

    @PostMapping(path = "/human-review", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<Long> humanReview(@RequestHeader String userToken, @RequestBody JsonNode request)
            throws GenericException, IOException, InterruptedException {
        PersonDTO person = genericBO.validateSession(userToken);
        Long datasetItemId = request.path("datasetItemId").isNumber() ? request.path("datasetItemId").asLong() : null;
        if (datasetItemId == null) throw new GenericException("datasetItemId es obligatorio.");
        GenericBasicResponse<Long> response = new GenericBasicResponse<>();
        response.setData(researchPersistenceService.recordHumanReview(datasetItemId, person.getId(), request));
        return response;
    }

    @PostMapping(path = "/backfill", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenericBasicResponse<Integer> backfill(@RequestHeader String userToken)
            throws GenericException, IOException, InterruptedException {
        genericBO.validateSession(userToken);
        GenericBasicResponse<Integer> response = new GenericBasicResponse<>();
        response.setData(researchPersistenceService.backfillExistingTrainings());
        return response;
    }

    @GetMapping(path = "/export-training.csv", produces = "text/csv")
    public ResponseEntity<String> exportTrainingCsv(@RequestHeader String userToken)
            throws GenericException, IOException, InterruptedException {
        genericBO.validateSession(userToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tddt4iots-research-training.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(researchPersistenceService.exportTrainingCsv());
    }
}
