package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

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
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.core.tddt4iots.util.Tddt4iotsUtil;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        log.info(requestGrpcTrainModel);
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
    public String useModelOpenAi(GenericTddt4iotsReqDTO<UseModelOpenaiRedDTO> genericTddt4iotsReqDTO) throws GenericException, IOException, InterruptedException {
        // validar la session de la herramienta
        PersonDTO personDTO = tddt4iotsGenericBO.validateSession(genericTddt4iotsReqDTO.getUserToken());
        // buscamos los datos del modelo a utilizar por la persona
        ModelUseToolDto modelUseToolDto = modelUseToolMapper.modelUseToolDtoTo(modelUseToolService.getModelUseToolByPerson(personDTO.getId()));
        GrpcTrainModelResDTO grpcTrainModelResDTO =
                Tddt4iotsUtil.fetchDataFromJsonString(modelUseToolDto.getModelTraining().getResultTrining(), GrpcTrainModelResDTO.class);
        List<MessageDTO> messages = new ArrayList<>();
        MessageDTO messageDTOSystem = MessageDTO.builder()
                .role(Tddt4iotsCons.SYSTEM)
                .content("You are a wizard at interpreting natural text that describes the requirements of a computer system, and you must generate everything needed for a " +
                        "class diagram and return the JSON as I have instructed you in the training. Make sure to return the complete JSON as a response, because I will parse " +
                        "it from string to JSON to use it in a web application. The JSON must be exactly like the one I provided you in the training; do not change the structure." +
                        "It must be a structure like this; if you are not adding data to an array, it should have at least 0 elements." +
                        "{\n" +
                        "  \"diagram\": [\n" +
                        "    {\n" +
                        "      \"packName\": \"string\",\n" +
                        "      \"class\": [\n" +
                        "        {\n" +
                        "          \"action\": \"string\",\n" +
                        "          \"derivative\": \"array\",\n" +
                        "          \"className\": \"string\",\n" +
                        "          \"visibility\": \"string\",\n" +
                        "          \"modifiers\": \"string\",\n" +
                        "          \"attributes\": [\n" +
                        "            {\n" +
                        "              \"visibility\": \"string\",\n" +
                        "              \"name\": \"string\",\n" +
                        "              \"type\": \"string\",\n" +
                        "              \"cardinalidate\": \"string\",\n" +
                        "              \"idToOrFrom\": \"string\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"methods\": [\n" +
                        "            {\n" +
                        "              \"visibility\": \"string\",\n" +
                        "              \"name\": \"string\",\n" +
                        "              \"type\": \"string\",\n" +
                        "              \"parameters\": [\n" +
                        "                {\n" +
                        "                  \"name\": \"string\",\n" +
                        "                  \"type\": \"string\",\n" +
                        "                  \"$$hashKey\": \"string\"\n" +
                        "                }\n" +
                        "              ],\n" +
                        "              \"$$hashKey\": \"string\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"constructors\": \"array\",\n" +
                        "          \"$$hashKey\": \"string\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"enums\": [\n" +
                        "        {\n" +
                        "          \"name\": \"string\",\n" +
                        "          \"visibility\": \"string\",\n" +
                        "          \"elements\": \"array\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"$$hashKey\": \"string\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"xmldiagram\": \"string\",\n" +
                        "  \"relationships\": [\n" +
                        "    {\n" +
                        "      \"from\": \"string\",\n" +
                        "      \"to\": \"string\",\n" +
                        "      \"typeRelatioship\": \"string\",\n" +
                        "      \"value\": \"string\",\n" +
                        "      \"cardinalidate\": \"string\",\n" +
                        "      \"from_fk\": \"string\",\n" +
                        "      \"to_fk\": \"string\",\n" +
                        "      \"simbol\": \"string\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"action\": \"array\",\n" +
                        "  \"notifications\": [\n" +
                        "    {\n" +
                        "      \"status\": \"number\",\n" +
                        "      \"information\": \"string\",\n" +
                        "      \"type\": \"string\",\n" +
                        "      \"data\": \"array\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"edition\": \"boolean\"\n" +
                        "}\n")
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
        // usamos el modelo entrenado
        log.info(grpcUseModelString);
        String response = grpcTrainingModelOpenAi.useModel(grpcUseModelString);
        log.info(response);

        // Define the regex pattern to capture the JSON part of the string
        Pattern pattern = Pattern.compile("Processing result: (\\{.*\\})");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // Extract the JSON part
            response = matcher.group(1);
        }

        return response;
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
