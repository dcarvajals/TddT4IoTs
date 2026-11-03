package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.MasterProjectBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.ModelPermissionBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.Tddt4iotsGenericBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.grpc.GrpcTrainingModelOpenAi;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.config.ApplicationConfig;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.*;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.research.ResearchPersistenceService;
import com.ugr.microservices.dependencies.core.tddt4iots.cons.Tddt4iotsCons;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.diagramclass.DiagramDataResDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.core.tddt4iots.util.Tddt4iotsUtil;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MasterProjectBOImpl implements MasterProjectBO {

    private final ApplicationConfig applicationConfig;

    @Autowired
    Tddt4iotsGenericBO tddt4iotsGenericBO;

    @Autowired
    MasterProjectService masterProjectService;

    @Autowired
    ModelPermissionService modelPermissionService;

    @Autowired
    TrainingHistoryService trainingHistoryService;

    @Autowired
    PersonService personService;

    @Autowired
    ModelUseToolService modelUseToolService;

    @Autowired
    ModelPermissionBO modelPermissionBO;

    @Autowired
    MasterProjectMapper masterProjectMapper;

    @Autowired
    TrainingHistoryMapper trainingHistoryMapper;

    @Autowired
    ModelPermissionMapper modelPermissionMapper;

    @Autowired
    ModelUseToolMapper modelUseToolMapper;

    @Autowired
    PersonMapper personMapper;

    @Autowired
    private GrpcTrainingModelOpenAi grpcTrainingModelOpenAi;

    @Autowired
    private ResearchPersistenceService researchPersistenceService;

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public MasterProjectBOImpl(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public List<MasterProjectDTO> getAllProjectsActive() throws GenericException {
        return masterProjectMapper.masterProjectDTOListTo(masterProjectService.getAllProjectsActive());
    }

    @Override
    public List<MasterProjectDTO> getProjectFromToDate(GetProjectFromDateReq request) throws GenericException {
        return  masterProjectMapper.masterProjectDTOListTo(
                masterProjectService.getProjectFromToDate(request)
        );
    }

    @Override
    public CreateFileTrainingResDTO createFileTraining(CreateFileTrainingReq request) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());

        if (request.getSamples() != null && !request.getSamples().isEmpty()) {
            return createReviewedProjectDataset(request, personDTO);
        }

        CreateFileTrainingResDTO createFileTrainingResDTO = new CreateFileTrainingResDTO();
        GetProjectFromDateReq getProjectFromDateReq = GetProjectFromDateReq.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        List<MasterProjectDTO> masterProjectDTOList = masterProjectMapper.masterProjectDTOListTo(
                masterProjectService.getProjectFromToDate(getProjectFromDateReq));

        if (!masterProjectDTOList.isEmpty()) {
            String pathDescriptionUseCaseJson = "";
            List<TrainingDataDTO> trainingDataDTOListRoot = new ArrayList<>();
            List<TrainingDataDTO> trainingDataDTOList = new ArrayList<>();
            for (MasterProjectDTO masterProjectDTO : masterProjectDTOList) {
                pathDescriptionUseCaseJson =
                        applicationConfig.getTddt4iotsNfs() +
                        Tddt4iotsCons.PATH_PROJECTS +
                        masterProjectDTO.getPathMp() +
                        Tddt4iotsCons.UML_DIAGRAM +
                        Tddt4iotsCons.UML_PRO_JSON +
                        Tddt4iotsCons.JSON;
                log.info("pathDescriptionUseCaseJson: " + pathDescriptionUseCaseJson);
                UmlProDTO data = Tddt4iotsUtil.fetchDataFromUrl(pathDescriptionUseCaseJson, UmlProDTO.class);
                if(data.getData() != null) {
                    List<UseCaseDiagramDTO> useCaseDiagramDTOList = data.getData().getUseCase();
                    List<MessageDTO> messageDTOList = new ArrayList<>();
                    for (UseCaseDiagramDTO useCaseDiagramDTO : useCaseDiagramDTOList) {

                        List<ArmadilloApiResDTO> armadilloApiResDTOList = postMultipleRequests(
                                useCaseDiagramDTO.getName(),
                                useCaseDiagramDTO.getPorpuse(),
                                useCaseDiagramDTO.getPre_condition(),
                                useCaseDiagramDTO.getPost_condition(),
                                useCaseDiagramDTO.getDescription_interpret()
                        );

                        for(ArmadilloApiResDTO armadilloApiResDTO : armadilloApiResDTOList) {
                            messageDTOList = new ArrayList<>();
                            messageDTOList.add(new MessageDTO().builder()
                                    .role(Tddt4iotsCons.USER)
                                    .content(armadilloApiResDTO.getTextInterpret())
                                    .build());
                            // Agregar la posible respuesta que debería dar el modelo
                            messageDTOList.add(new MessageDTO().builder()
                                    .role(Tddt4iotsCons.ASSISTANT)
                                    .content(armadilloApiResDTO.getClassDiagram())
                                    .build());
                            TrainingDataDTO trainingDataDTO = TrainingDataDTO.builder()
                                    .messages(messageDTOList)
                                    .build();
                            trainingDataDTOList.add(trainingDataDTO);
                        }

                        for (MainStageDTO mainStageDTO : useCaseDiagramDTO.getMain_stage()) {
                            messageDTOList = new ArrayList<>();
                            ArmadilloApiReqDTO armadilloApiReqDTO = ArmadilloApiReqDTO.builder()
                                            .text(mainStageDTO.getAction_original()).build();
                            ArmadilloApiResDTO armadilloApiResDTO = Tddt4iotsUtil.postRequest(
                                    applicationConfig.getTddt4iotsArmadilloApi(),
                                    armadilloApiReqDTO,
                                    ArmadilloApiResDTO.class);
                            messageDTOList.add(new MessageDTO().builder()
                                    .role(Tddt4iotsCons.USER)
                                    .content(armadilloApiResDTO.getTextInterpret())
                                    .build());
                            // Agregar la posible respuesta que debería dar el modelo
                            messageDTOList.add(new MessageDTO().builder()
                                    .role(Tddt4iotsCons.ASSISTANT)
                                    .content(armadilloApiResDTO.getClassDiagram())
                                    .build());
                            TrainingDataDTO trainingDataDTO = TrainingDataDTO.builder()
                                    .messages(messageDTOList)
                                    .build();
                            trainingDataDTOList.add(trainingDataDTO);
                        }
                        for (AlternativeFlowDTO alternativeFlowDTO : useCaseDiagramDTO.getAlternative_flow()) {
                            messageDTOList = new ArrayList<>();
                            ArmadilloApiReqDTO armadilloApiReqDTO = ArmadilloApiReqDTO.builder()
                                    .text(alternativeFlowDTO.getAction_original()).build();
                            ArmadilloApiResDTO armadilloApiResDTO = Tddt4iotsUtil.postRequest(
                                    applicationConfig.getTddt4iotsArmadilloApi(),
                                    armadilloApiReqDTO,
                                    ArmadilloApiResDTO.class);
                            messageDTOList.add(new MessageDTO().builder()
                                    .role(Tddt4iotsCons.USER)
                                    .content(armadilloApiResDTO.getTextInterpret())
                                    .build());
                            // Agregar la posible respuesta que debería dar el modelo
                            messageDTOList.add(new MessageDTO().builder()
                                    .role(Tddt4iotsCons.ASSISTANT)
                                    .content(armadilloApiResDTO.getClassDiagram())
                                    .build());
                            TrainingDataDTO trainingDataDTO = TrainingDataDTO.builder()
                                    .messages(messageDTOList)
                                    .build();
                            trainingDataDTOList.add(trainingDataDTO);
                        }
                    }
                }
            }

            // Escribir en el archivo .jsonl
            StringBuilder jsonlContent = new StringBuilder();
            Integer rowCount = 0;
            for (TrainingDataDTO trainingDataDTO : trainingDataDTOList) {
                String json = JSON_MAPPER.writeValueAsString(trainingDataDTO);
                jsonlContent.append(json).append("\n");
                rowCount++;
            }

            // URL del servicio web
            String pathSaveFileTraining = applicationConfig.getTddt4iotsServer() + Tddt4iotsCons.URL_SAVE_FILE_TRAINING;

            // Escribir en el archivo .jsonl
            String dateFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String pathTrainingFileData = Tddt4iotsCons.TRAINING_DATA + dateFormat + Tddt4iotsCons.JSONL;

            SaveFileTrainingReqDTO saveFileTrainingReqDTO = SaveFileTrainingReqDTO.builder()
                    .filePath(pathTrainingFileData)
                    .fileContent(jsonlContent.toString())
                    .build();

            Tddt4iotsServerResDTO tddt4iotsServerResDTO = Tddt4iotsUtil.postRequest(
                    pathSaveFileTraining,
                    saveFileTrainingReqDTO,
                    Tddt4iotsServerResDTO.class);

            if (tddt4iotsServerResDTO.getStatus() == 2) {
                log.info("Solicitud POST exitosa");
                String pathFileJson = applicationConfig.getTddt4iotsNfs() +
                        Tddt4iotsCons.PATH_TRAINING_DATA +
                        pathTrainingFileData;
                // guardar el registro del archivo creado para el entrenamiento del modelo
                TrainingHistoryDTO trainingHistoryDTO = new TrainingHistoryDTO();
                trainingHistoryDTO.setIdPerson(personDTO);
                trainingHistoryDTO.setDateCreation(new Date());
                trainingHistoryDTO.setCommit(request.getCommit());
                trainingHistoryDTO.setDateStartTrining(request.getStartDate());
                trainingHistoryDTO.setDateEndTrining(request.getEndDate());
                trainingHistoryDTO.setPathFileJson(pathFileJson);
                trainingHistoryDTO.setCountRowDataTrining(Long.parseLong(rowCount.toString()));
                trainingHistoryDTO.setModel(modelPermissionBO.validatePermiss(request.getUserToken()));
                trainingHistoryDTO = trainingHistoryMapper.trainingHistoryDtoTo(trainingHistoryService.save(trainingHistoryMapper.trainingHistoryTo(trainingHistoryDTO)));
                createFileTrainingResDTO.setPathJsonFile(pathFileJson);
                createFileTrainingResDTO.setIdTrainingHistory(trainingHistoryDTO.getId());
            } else {
                log.error("Error en la solicitud POST. Código de respuesta: " + tddt4iotsServerResDTO.getInformation());
            }
        }
        return createFileTrainingResDTO;
    }

    private CreateFileTrainingResDTO createReviewedProjectDataset(CreateFileTrainingReq request, PersonDTO personDTO)
            throws GenericException, JsonProcessingException, IOException, InterruptedException {
        if (request.getDatasetName() == null || request.getDatasetName().isBlank()
                || request.getDatasetVersion() == null || request.getDatasetVersion().isBlank()) {
            throw new GenericException("El manifiesto requiere datasetName y datasetVersion.");
        }
        List<TrainingDataDTO> examples = new ArrayList<>();
        java.util.Set<String> projectIds = new java.util.HashSet<>();
        String systemInstruction = singleLineTrainingContent("""
                Convert the complete natural-language software requirements into a TDDT4IOTS_CASE_WORKSPACE 1.0 JSON object.
                Preserve requirement traceability, create all use-case diagrams, one global class model through the symbol language,
                and one consistent sequence per use case. Return only JSON that satisfies the supplied CASE contract.
                """);
        for (int index = 0; index < request.getSamples().size(); index++) {
            ResearchDatasetSampleDTO sample = request.getSamples().get(index);
            if (sample == null || sample.getInputRequirements() == null || sample.getGoldWorkspace() == null) {
                throw new GenericException("La muestra " + index + " requiere inputRequirements y goldWorkspace.");
            }
            String projectId = sample.getProjectId() == null ? "sample-" + index : sample.getProjectId().trim();
            if (projectId.isBlank() || !projectIds.add(projectId)) {
                throw new GenericException("projectId vacío o duplicado en el manifiesto: " + projectId);
            }
            String split = sample.getSplit() == null ? "TRAIN" : sample.getSplit().toUpperCase(Locale.ROOT);
            if (!java.util.Set.of("TRAIN", "VALIDATION", "TEST").contains(split)) {
                throw new GenericException("Split no compatible en " + projectId + ": " + split);
            }
            validateCaseWorkspace(sample.getGoldWorkspace());
            if (!"TRAIN".equals(split)) continue; // Validation and locked test projects must never enter training JSONL.
            List<MessageDTO> messages = new ArrayList<>();
            messages.add(MessageDTO.builder().role(Tddt4iotsCons.SYSTEM).content(systemInstruction).build());
            messages.add(MessageDTO.builder().role(Tddt4iotsCons.USER)
                    .content(singleLineTrainingContent(JSON_MAPPER.writeValueAsString(sample.getInputRequirements()))).build());
            messages.add(MessageDTO.builder().role(Tddt4iotsCons.ASSISTANT)
                    .content(singleLineTrainingContent(JSON_MAPPER.writeValueAsString(sample.getGoldWorkspace()))).build());
            examples.add(TrainingDataDTO.builder().messages(messages).build());
        }
        if (examples.isEmpty()) throw new GenericException("El manifiesto no contiene muestras con split TRAIN.");

        StringBuilder jsonlContent = new StringBuilder();
        for (TrainingDataDTO example : examples) jsonlContent.append(JSON_MAPPER.writeValueAsString(example)).append('\n');
        String fileName = sanitizeFilePart(request.getDatasetName()) + "-" + sanitizeFilePart(request.getDatasetVersion())
                + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + Tddt4iotsCons.JSONL;
        String pathTrainingFileData = Tddt4iotsCons.TRAINING_DATA + fileName;
        Tddt4iotsServerResDTO saveResponse = Tddt4iotsUtil.postRequest(
                applicationConfig.getTddt4iotsServer() + Tddt4iotsCons.URL_SAVE_FILE_TRAINING,
                SaveFileTrainingReqDTO.builder().filePath(pathTrainingFileData).fileContent(jsonlContent.toString()).build(),
                Tddt4iotsServerResDTO.class);
        if (saveResponse.getStatus() != 2) {
            throw new GenericException("No se pudo guardar el dataset revisado: " + saveResponse.getInformation());
        }

        String pathFileJson = applicationConfig.getTddt4iotsNfs() + Tddt4iotsCons.PATH_TRAINING_DATA + pathTrainingFileData;
        TrainingHistoryDTO history = new TrainingHistoryDTO();
        history.setIdPerson(personDTO);
        history.setDateCreation(new Date());
        history.setCommit(request.getDatasetName() + ":" + request.getDatasetVersion());
        history.setDateStartTrining(null);
        history.setDateEndTrining(null);
        history.setPathFileJson(pathFileJson);
        history.setCountRowDataTrining((long) examples.size());
        history.setModel(modelPermissionBO.validatePermiss(request.getUserToken()));
        history = trainingHistoryMapper.trainingHistoryDtoTo(trainingHistoryService.save(trainingHistoryMapper.trainingHistoryTo(history)));
        researchPersistenceService.registerDataset(history.getId(), request, personDTO.getId());
        return CreateFileTrainingResDTO.builder().pathJsonFile(pathFileJson).idTrainingHistory(history.getId()).build();
    }

    private String sanitizeFilePart(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-").replaceAll("^-|-$", "");
    }

    private String singleLineTrainingContent(String value) {
        if (value == null) return "";
        return value.replace("\\r\\n", " ")
                .replace("\\n", " ")
                .replace("\\r", " ")
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("[\\t ]+", " ")
                .trim();
    }

    @Override
    public List<TrainingModelOpenAiRes> trainingModelOpenAi(TrainingModelOpenAiReq request) throws GenericException {
        List<TrainingModelOpenAiRes> trainingModelOpenAiResList = new ArrayList<>();
        TrainingHistoryDTO trainingHistoryDTO = trainingHistoryMapper.trainingHistoryDtoTo(trainingHistoryService.findIdByModelPermission(request.getIdTrainingHistory()));
        // Every scientific run starts from the configured base model. Chaining fine-tunes
        // makes experiments incomparable and can accumulate drift between datasets.
        GrpcTrainModelReqDTO grpcTrainModelReqDTO = GrpcTrainModelReqDTO.builder()
                .openAiSecretKey(trainingHistoryDTO.getIdPerson().getOpenaiSecretKey())
                .model(trainingHistoryDTO.getModel().getModel().getName())
                .pathFileTrain(trainingHistoryDTO.getPathFileJson())
                .build();

        String requestGrpcTrainModel = Tddt4iotsUtil.convertObjectToJsonString(grpcTrainModelReqDTO);
        //log.info(requestGrpcTrainModel);
        // inicia el entrenamiento con el dataset versionado seleccionado
        String response = grpcTrainingModelOpenAi.trainModel(requestGrpcTrainModel);

        // Define the regex pattern to capture the JSON part of the string
        Pattern pattern = Pattern.compile("Processing result: (\\{.*\\})");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // Extract the JSON part
            String json = matcher.group(1);

            GrpcTrainModelResDTO grpcTrainModelResDTO =
                    Tddt4iotsUtil.fetchDataFromJsonString(json, GrpcTrainModelResDTO.class);

            // actualizar los resultados del entrenamiento
            trainingHistoryDTO.setResultTrining(json);
            String providerStatus = grpcTrainModelResDTO.getData() == null ? null : grpcTrainModelResDTO.getData().getStatus();
            trainingHistoryDTO.setProviderJobId(grpcTrainModelResDTO.getData() == null ? null : grpcTrainModelResDTO.getData().getId());
            trainingHistoryDTO.setProviderStatus(providerStatus);
            trainingHistoryDTO.setProgressPercent("succeeded".equals(providerStatus) ? 100D : null);
            trainingHistoryDTO.setLastMonitoredAt(new Date());
            trainingHistoryDTO.setMonitoringFailures(0);
            trainingHistoryDTO.setStatus(trainingStatusCode(grpcTrainModelResDTO.getStatus(), providerStatus));
            trainingHistoryService.save(trainingHistoryMapper.trainingHistoryTo(trainingHistoryDTO));
            try {
                JsonNode job = JSON_MAPPER.readTree(json).path("data");
                if (job.isObject()) researchPersistenceService.registerTrainingStart(trainingHistoryDTO.getId(), job);
            } catch (JsonProcessingException error) {
                log.warn("No se pudo registrar el inicio científico del entrenamiento {}: {}", trainingHistoryDTO.getId(), error.getMessage());
            }
            TrainingModelOpenAiRes trainingModelOpenAiRes = TrainingModelOpenAiRes.builder()
                    .result(response)
                    .pathFileJson(trainingHistoryDTO.getPathFileJson())
                    .build();
            trainingModelOpenAiResList.add(trainingModelOpenAiRes);
        } else {
            throw new GenericException("No JSON found in the response grpc training model.");
        }



        return trainingModelOpenAiResList;
    }

    private Character trainingStatusCode(String envelopeStatus, String providerStatus) {
        if ("ERROR".equals(envelopeStatus) || "failed".equals(providerStatus) || "cancelled".equals(providerStatus)) return 'E';
        if ("succeeded".equals(providerStatus)) return 'S';
        if ("running".equals(providerStatus)) return 'R';
        return 'Q';
    }

    @Override
    public DiagramDataResDTO useModelOpenAi(GenericTddt4iotsReqDTO<UseModelOpenaiRedDTO> genericTddt4iotsReqDTO) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(genericTddt4iotsReqDTO.getUserToken());
        // buscamos los datos del modelo a utilizar por la persona
        ModelUseToolDto modelUseToolDto = modelUseToolMapper.modelUseToolDtoTo(modelUseToolService.getModelUseToolByPerson(personDTO.getId()));
        GrpcTrainModelResDTO grpcTrainModelResDTO =
                Tddt4iotsUtil.fetchDataFromJsonString(modelUseToolDto.getModelTraining().getResultTrining(), GrpcTrainModelResDTO.class);
        List<MessageDTO> messages = new ArrayList<>();
        DiagramDataResDTO response = new DiagramDataResDTO();
        // leer el promp por defecto para cada peticion
        String contentDefault = Tddt4iotsUtil.fetchTextFromUrl(applicationConfig.getTddt4iotsNfs() + Tddt4iotsCons.PATH_TEMPLATE + "prompt.txt");
        MessageDTO messageDTOSystem = MessageDTO.builder()
                .role(Tddt4iotsCons.SYSTEM)
                .content(contentDefault)
                .build();
        MessageDTO messageDTOUser = MessageDTO.builder()
                .role(Tddt4iotsCons.USER)
                .content(genericTddt4iotsReqDTO.getClassDTO().getDescriptionUseCase())
                .build();
        messages.add(messageDTOSystem);
        messages.add(messageDTOUser);
        GrpcUseModelReqDTO grpcUseModelReqDTO = GrpcUseModelReqDTO.builder()
                .openAiSecretKey(personDTO.getOpenaiSecretKey())
                .model(grpcTrainModelResDTO.getData().getFineTunedModel() == null ? grpcTrainModelResDTO.getData().getModel() : grpcTrainModelResDTO.getData().getFineTunedModel())
                .messages(messages)
                .build();
        String grpcUseModelString = Tddt4iotsUtil.convertObjectToJsonString(grpcUseModelReqDTO);
        log.info(grpcUseModelString);
        String responseGrpc = grpcTrainingModelOpenAi.useModel(grpcUseModelString);
        log.info(responseGrpc);

        // 1. Extraer el JSON usando el Matcher que ya tienes
        Pattern pattern = Pattern.compile("Processing result:\\s*(\\{.*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseGrpc);

        if (!matcher.find()) {
            log.error("No se encontró un resultado JSON en la respuesta gRPC");
            throw new GenericException("El servicio de IA no devolvió una estructura de diagrama reconocible.");
        }

        String jsonExtraido = matcher.group(1);
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode rootNode = objectMapper.readTree(jsonExtraido);
            JsonNode dataNode = rootNode.get("data");

            if (dataNode == null || dataNode.isNull()) {
                throw new GenericException("La respuesta de la IA no contiene datos para generar el diagrama.");
            }

            String rawData = dataNode.isTextual() ? dataNode.asText() : dataNode.toString();
            rawData = rawData.trim()
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();

            if (!rawData.startsWith("{") || !rawData.endsWith("}")) {
                log.error("Detección de JSON truncado o inválido en el campo data");
                throw new GenericException("La respuesta de la IA llegó incompleta y no puede convertirse en un diagrama.");
            }

            response = objectMapper.readValue(rawData, DiagramDataResDTO.class);
            if (response.getDiagram() == null || response.getDiagram().isEmpty()) {
                throw new GenericException("La IA devolvió un modelo sin clases o paquetes para representar.");
            }
        } catch (JsonProcessingException e) {
            log.error("Error en la estructura del diagrama: {}", e.getMessage());
            throw new GenericException("La IA generó una estructura incompatible con el estándar UML.");
        }

        return response;
    }

    @Override
    public JsonNode generateCaseWorkspace(GenericTddt4iotsReqDTO<GenerateCaseWorkspaceReqDTO> request)
            throws GenericException, IOException, InterruptedException {
        GenerateCaseWorkspaceReqDTO input = request.getClassDTO();
        if (input == null) throw new GenericException("No se recibió información para analizar.");

        StringBuilder naturalLanguage = new StringBuilder();
        if (input.getDescription() != null && !input.getDescription().isBlank()) {
            naturalLanguage.append("DESCRIPCIÓN ADICIONAL:\n").append(input.getDescription().trim()).append("\n\n");
        }
        List<NaturalLanguageDocumentDTO> documents = input.getDocuments() == null ? List.of() : input.getDocuments();
        for (NaturalLanguageDocumentDTO document : documents) {
            String extracted = extractNaturalLanguageDocument(document);
            if (!extracted.isBlank()) {
                naturalLanguage.append("DOCUMENTO: ").append(document.getFileName()).append("\n")
                        .append(extracted).append("\n\n");
            }
        }
        if (naturalLanguage.toString().isBlank()) {
            throw new GenericException("Agregue una descripción o al menos un documento con texto.");
        }
        if (naturalLanguage.length() > 250_000) {
            throw new GenericException("El contenido combinado supera el límite de 250.000 caracteres.");
        }

        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(request.getUserToken());
        ModelUseToolDto modelUseToolDto = modelUseToolMapper.modelUseToolDtoTo(modelUseToolService.getModelUseToolByPerson(personDTO.getId()));
        GrpcTrainModelResDTO trainedModel = Tddt4iotsUtil.fetchDataFromJsonString(
                modelUseToolDto.getModelTraining().getResultTrining(), GrpcTrainModelResDTO.class);

        String workspacePrompt = """
                You are a senior CASE analyst. Convert the supplied natural-language requirements into one valid JSON object.
                Return JSON only. The root contract is:
                {"format":"TDDT4IOTS_CASE_WORKSPACE","schemaVersion":"1.0","project":{"name":"..."},"diagrams":[...]}
                Each diagram contains id, name, actors[{id,name,position:{x,y}}], useCases[] and relations[].
                Each use case contains id, name, position, purpose, precondition, postcondition, description,
                mainFlow[{actor,action}], alternativeFlows[{id,startStep,condition,actor,action,outcome,returnStep}],
                and sequence:{participants:[{id,name,type}],messages:[{from,to,message,type,response,responseType}]}.
                Relation types are association, include, extend or generalization. Actor references use actor ids; System is allowed.
                Sequence message type is sync or async. Every from/to references a participant id.
                Generate multiple use-case diagrams when requirements describe distinct functional areas and one sequence per use case.
                All purpose, condition, description and action texts must remain readable natural language AND include the modeling symbols
                required to derive the class diagram, such as (Class &-attribute=string [+operation=void]),
                *¡ClassA 1>[relation]<n ClassB! and *¿Enumeration »Value1 »Value2 ?.
                Use stable kebab-case ids. Do not include Markdown fences or explanations.
                """;
        List<MessageDTO> messages = new ArrayList<>();
        messages.add(MessageDTO.builder().role(Tddt4iotsCons.SYSTEM).content(workspacePrompt).build());
        messages.add(MessageDTO.builder().role(Tddt4iotsCons.USER).content(naturalLanguage.toString()).build());
        GrpcUseModelReqDTO grpcRequest = GrpcUseModelReqDTO.builder()
                .openAiSecretKey(personDTO.getOpenaiSecretKey())
                .model(trainedModel.getData().getFineTunedModel() == null
                        ? trainedModel.getData().getModel() : trainedModel.getData().getFineTunedModel())
                .messages(messages)
                .responseSchema("case_workspace")
                .maxOutputTokens(16000)
                .build();
        long inferenceStarted = System.currentTimeMillis();
        String responseGrpc = grpcTrainingModelOpenAi.useModel(Tddt4iotsUtil.convertObjectToJsonString(grpcRequest));
        Pattern pattern = Pattern.compile("Processing result:\\s*(\\{.*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseGrpc);
        if (!matcher.find()) {
            recordFailedResearchInference(modelUseToolDto.getModelTraining().getId(), naturalLanguage.toString(),
                    responseGrpc, System.currentTimeMillis() - inferenceStarted, grpcRequest.getModel(), null,
                    "El servicio de IA no devolvió un JSON de proyecto reconocible.");
            throw new GenericException("El servicio de IA no devolvió un JSON de proyecto reconocible.");
        }

        try {
            JsonNode envelope = JSON_MAPPER.readTree(matcher.group(1));
            JsonNode dataNode = envelope.get("data");
            if (dataNode == null || dataNode.isNull()) throw new GenericException("La IA devolvió una respuesta sin datos.");
            String rawData = dataNode.isTextual() ? dataNode.asText() : dataNode.toString();
            rawData = rawData.trim().replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
            JsonNode workspace = JSON_MAPPER.readTree(rawData);
            validateCaseWorkspace(workspace);
            researchPersistenceService.recordInference(
                    modelUseToolDto.getModelTraining().getId(),
                    JSON_MAPPER.createObjectNode().put("naturalLanguage", naturalLanguage.toString()),
                    rawData, workspace, JSON_MAPPER.createArrayNode(),
                    System.currentTimeMillis() - inferenceStarted,
                    envelope.path("model").asText(grpcRequest.getModel()), envelope.path("usage"), null);
            return workspace;
        } catch (GenericException exception) {
            JsonNode envelope = parseResearchEnvelope(matcher.group(1));
            recordFailedResearchInference(modelUseToolDto.getModelTraining().getId(), naturalLanguage.toString(),
                    matcher.group(1), System.currentTimeMillis() - inferenceStarted,
                    envelope.path("model").asText(grpcRequest.getModel()), envelope.path("usage"), exception.getMessage());
            throw exception;
        } catch (JsonProcessingException exception) {
            log.error("Invalid CASE workspace generated by AI: {}", exception.getMessage());
            JsonNode envelope = parseResearchEnvelope(matcher.group(1));
            recordFailedResearchInference(modelUseToolDto.getModelTraining().getId(), naturalLanguage.toString(),
                    matcher.group(1), System.currentTimeMillis() - inferenceStarted,
                    envelope.path("model").asText(grpcRequest.getModel()), envelope.path("usage"), exception.getMessage());
            throw new GenericException("La IA generó un JSON incompleto o inválido.");
        }
    }

    private JsonNode parseResearchEnvelope(String raw) {
        try { return JSON_MAPPER.readTree(raw); }
        catch (Exception ignored) { return JSON_MAPPER.createObjectNode(); }
    }

    private void recordFailedResearchInference(Long trainingHistoryId, String naturalLanguage, String raw,
                                               long latencyMs, String model, JsonNode usage, String error) {
        try {
            com.fasterxml.jackson.databind.node.ArrayNode errors = JSON_MAPPER.createArrayNode().add(error);
            researchPersistenceService.recordInference(trainingHistoryId,
                    JSON_MAPPER.createObjectNode().put("naturalLanguage", naturalLanguage),
                    raw, null, errors, latencyMs, model, usage, error);
        } catch (Exception persistenceError) {
            log.warn("No se pudo guardar la inferencia fallida para investigación: {}", persistenceError.getMessage());
        }
    }

    private void validateCaseWorkspace(JsonNode workspace) throws GenericException {
        if (!"TDDT4IOTS_CASE_WORKSPACE".equals(workspace.path("format").asText())
                || !"1.0".equals(workspace.path("schemaVersion").asText())) {
            throw new GenericException("La IA devolvió una versión no compatible del espacio CASE.");
        }
        JsonNode diagrams = workspace.path("diagrams");
        if (!diagrams.isArray() || diagrams.isEmpty()) {
            throw new GenericException("La IA no generó diagramas de casos de uso.");
        }
        java.util.Set<String> diagramIds = new java.util.HashSet<>();
        java.util.Set<String> globalUseCaseIds = new java.util.HashSet<>();
        for (int diagramIndex = 0; diagramIndex < diagrams.size(); diagramIndex++) {
            JsonNode diagram = diagrams.get(diagramIndex);
            String diagramPath = "diagrams[" + diagramIndex + "]";
            requireUniqueId(diagram.path("id").asText(), diagramIds, diagramPath + ".id");
            if (diagram.path("name").asText().isBlank()) throw workspaceError(diagramPath + ".name es obligatorio.");
            JsonNode actors = diagram.path("actors");
            JsonNode useCases = diagram.path("useCases");
            JsonNode relations = diagram.path("relations");
            if (!actors.isArray() || !useCases.isArray() || useCases.isEmpty() || !relations.isArray()) {
                throw workspaceError(diagramPath + " debe contener actors, useCases y relations válidos.");
            }
            java.util.Set<String> actorIds = new java.util.HashSet<>();
            for (JsonNode actor : actors) requireUniqueId(actor.path("id").asText(), actorIds, diagramPath + ".actors.id");
            java.util.Set<String> useCaseIds = new java.util.HashSet<>();
            for (int useCaseIndex = 0; useCaseIndex < useCases.size(); useCaseIndex++) {
                JsonNode useCase = useCases.get(useCaseIndex);
                String useCasePath = diagramPath + ".useCases[" + useCaseIndex + "]";
                String useCaseId = useCase.path("id").asText();
                requireUniqueId(useCaseId, useCaseIds, useCasePath + ".id");
                requireUniqueId(useCaseId, globalUseCaseIds, useCasePath + ".id global");
                if (!useCase.path("mainFlow").isArray() || useCase.path("mainFlow").isEmpty()) {
                    throw workspaceError(useCasePath + ".mainFlow debe contener al menos un paso.");
                }
                for (JsonNode step : useCase.path("mainFlow")) {
                    String actor = step.path("actor").asText();
                    if (!"System".equals(actor) && !actorIds.contains(actor)) {
                        throw workspaceError(useCasePath + ".mainFlow referencia un actor inexistente: " + actor);
                    }
                }
                validateSequence(useCase.path("sequence"), useCasePath + ".sequence");
            }
            for (JsonNode relation : relations) {
                String from = relation.path("from").asText();
                String to = relation.path("to").asText();
                if (!(actorIds.contains(from) || useCaseIds.contains(from))
                        || !(actorIds.contains(to) || useCaseIds.contains(to))) {
                    throw workspaceError(diagramPath + ".relations contiene referencias inexistentes.");
                }
            }
        }
    }

    private void validateSequence(JsonNode sequence, String path) throws GenericException {
        JsonNode participants = sequence.path("participants");
        JsonNode messages = sequence.path("messages");
        if (!participants.isArray() || participants.size() < 2 || !messages.isArray() || messages.isEmpty()) {
            throw workspaceError(path + " requiere al menos dos participantes y un mensaje.");
        }
        java.util.Set<String> participantIds = new java.util.HashSet<>();
        for (JsonNode participant : participants) requireUniqueId(participant.path("id").asText(), participantIds, path + ".participants.id");
        for (JsonNode message : messages) {
            if (!participantIds.contains(message.path("from").asText()) || !participantIds.contains(message.path("to").asText())) {
                throw workspaceError(path + " contiene un mensaje con participantes inexistentes.");
            }
        }
    }

    private void requireUniqueId(String id, java.util.Set<String> ids, String path) throws GenericException {
        if (id == null || id.isBlank()) throw workspaceError(path + " es obligatorio.");
        if (!ids.add(id)) throw workspaceError(path + " está duplicado: " + id);
    }

    private GenericException workspaceError(String detail) {
        return new GenericException("El espacio CASE generado no es consistente: " + detail);
    }

    private String extractNaturalLanguageDocument(NaturalLanguageDocumentDTO document) throws GenericException {
        if (document == null || document.getBase64Content() == null || document.getFileName() == null) return "";
        try {
            String encoded = document.getBase64Content().replaceFirst("^data:[^,]+,", "");
            byte[] bytes = Base64.getDecoder().decode(encoded);
            if (bytes.length > 10 * 1024 * 1024) throw new GenericException("El documento " + document.getFileName() + " supera 10 MB.");
            String fileName = document.getFileName().toLowerCase(Locale.ROOT);
            if (fileName.endsWith(".txt")) return new String(bytes, StandardCharsets.UTF_8);
            if (fileName.endsWith(".pdf")) {
                try (PDDocument pdf = Loader.loadPDF(bytes)) {
                    return new PDFTextStripper().getText(pdf);
                }
            }
            if (fileName.endsWith(".docx")) {
                try (XWPFDocument word = new XWPFDocument(new ByteArrayInputStream(bytes))) {
                    return word.getParagraphs().stream().map(XWPFParagraph::getText).reduce("", (left, right) -> left + right + "\n");
                }
            }
            if (fileName.endsWith(".doc")) {
                try (HWPFDocument word = new HWPFDocument(new ByteArrayInputStream(bytes));
                     WordExtractor extractor = new WordExtractor(word)) {
                    return extractor.getText();
                }
            }
            throw new GenericException("Formato no compatible: " + document.getFileName());
        } catch (IllegalArgumentException | IOException exception) {
            throw new GenericException("No se pudo leer " + document.getFileName() + ". Verifique que el archivo no esté dañado.");
        }
    }

    @Override
    public String deleteTrainingHistory(GenericTddt4iotsReqDTO<Long> request) throws GenericException {
        String response = "Record successfully deleted.";
        TrainingHistoryDTO trainingHistoryDTO = trainingHistoryMapper.trainingHistoryDtoTo(trainingHistoryService.findId(request.getClassDTO()));

        if(trainingHistoryDTO == null) {
            throw new GenericException("The record to be deleted does not exist in the database.");
        }

        trainingHistoryService.deleteTraining(trainingHistoryMapper.trainingHistoryTo(trainingHistoryDTO));
        return response;
    }

    private List<ArmadilloApiResDTO> postMultipleRequests(String... names) {
        return Tddt4iotsUtil.postMultipleRequests(applicationConfig.getTddt4iotsArmadilloApi(),
                ArmadilloApiResDTO.class, ArmadilloApiReqDTO.class, names);
    }


}
