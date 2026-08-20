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
                rowCount += rowCount;
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

    @Override
    public List<TrainingModelOpenAiRes> trainingModelOpenAi(TrainingModelOpenAiReq request) throws GenericException {
        List<TrainingModelOpenAiRes> trainingModelOpenAiResList = new ArrayList<>();
        // buscar los entrenamientos disponibles por el rango de fecha
        GetProjectFromDateReq getProjectFromDateReq = GetProjectFromDateReq.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        TrainingHistoryDTO trainingHistoryDTO = trainingHistoryMapper.trainingHistoryDtoTo(trainingHistoryService.findIdByModelPermission(request.getIdTrainingHistory()));
        GrpcTrainModelReqDTO grpcTrainModelReqDTO;

        if(trainingHistoryDTO.getModel().getPrimaryTrain()) {
            log.info("******* PRIMER ENTRENAMIENTO ********");
             grpcTrainModelReqDTO = GrpcTrainModelReqDTO.builder()
                    .openAiSecretKey(trainingHistoryDTO.getIdPerson().getOpenaiSecretKey())
                    .model((trainingHistoryDTO.getModel().getModel().getName()))
                    .pathFileTrain(trainingHistoryDTO.getPathFileJson())
                    .build();
        } else {
            log.info("******* NO ES SU PRIMER ENTRENAMIENTO ********");
            // buscamos el ultimo entrenamiento realizado por ese modelo base y los permisos asignados
            TrainingHistoryDTO trainingHistoryLastestDTO = trainingHistoryMapper
                    .trainingHistoryDtoTo(trainingHistoryService.getLastestTrainingByNotId(trainingHistoryDTO.getModel().getId(), request.getIdTrainingHistory()));

            GrpcTrainModelResDTO grpcTrainModelResDTO =
                    Tddt4iotsUtil.fetchDataFromJsonString(trainingHistoryLastestDTO.getResultTrining(), GrpcTrainModelResDTO.class);

            grpcTrainModelReqDTO = GrpcTrainModelReqDTO.builder()
                    .openAiSecretKey(trainingHistoryDTO.getIdPerson().getOpenaiSecretKey())
                    .model(grpcTrainModelResDTO.getData().getFineTunedModel())
                    .pathFileTrain(trainingHistoryDTO.getPathFileJson())
                    .build();
        }

        String requestGrpcTrainModel = Tddt4iotsUtil.convertObjectToJsonString(grpcTrainModelReqDTO);
        //log.info(requestGrpcTrainModel);
        // entrenamos todos los archivos que se han encontrado con la fecha indicada
        String response = grpcTrainingModelOpenAi.trainModel(requestGrpcTrainModel);

        // Define the regex pattern to capture the JSON part of the string
        Pattern pattern = Pattern.compile("Processing result: (\\{.*\\})");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // Extract the JSON part
            String json = matcher.group(1);

            GrpcTrainModelResDTO grpcTrainModelResDTO =
                    Tddt4iotsUtil.fetchDataFromJsonString(json, GrpcTrainModelResDTO.class);

            if(grpcTrainModelResDTO.getStatus().equals("ERROR")) {
                trainingHistoryDTO.setStatus('E');
            }

            // actualizar los resultados del entrenamiento
            trainingHistoryDTO.setResultTrining(json);
            trainingHistoryDTO.setStatus('S');
            trainingHistoryService.save(trainingHistoryMapper.trainingHistoryTo(trainingHistoryDTO));
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
                .build();
        String responseGrpc = grpcTrainingModelOpenAi.useModel(Tddt4iotsUtil.convertObjectToJsonString(grpcRequest));
        Pattern pattern = Pattern.compile("Processing result:\\s*(\\{.*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseGrpc);
        if (!matcher.find()) throw new GenericException("El servicio de IA no devolvió un JSON de proyecto reconocible.");

        try {
            JsonNode envelope = JSON_MAPPER.readTree(matcher.group(1));
            JsonNode dataNode = envelope.get("data");
            if (dataNode == null || dataNode.isNull()) throw new GenericException("La IA devolvió una respuesta sin datos.");
            String rawData = dataNode.isTextual() ? dataNode.asText() : dataNode.toString();
            rawData = rawData.trim().replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
            JsonNode workspace = JSON_MAPPER.readTree(rawData);
            if (!"TDDT4IOTS_CASE_WORKSPACE".equals(workspace.path("format").asText()) || !workspace.path("diagrams").isArray()) {
                throw new GenericException("La IA devolvió datos que no cumplen el contrato del espacio CASE.");
            }
            return workspace;
        } catch (JsonProcessingException exception) {
            log.error("Invalid CASE workspace generated by AI: {}", exception.getMessage());
            throw new GenericException("La IA generó un JSON incompleto o inválido.");
        }
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
