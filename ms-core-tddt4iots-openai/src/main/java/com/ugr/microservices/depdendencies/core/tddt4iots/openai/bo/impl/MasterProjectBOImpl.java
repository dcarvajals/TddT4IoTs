package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.MasterProjectBO;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.grpc.GrpcTrainingModelOpenAi;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.config.ApplicationConfig;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.MasterProjectMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.PersonMapper;
import com.ugr.microservices.depdendencies.core.tddt4iots.openai.mapper.TrainingHistoryMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.cons.Tddt4iotsCons;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.*;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.response.*;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.core.tddt4iots.util.Tddt4iotsUtil;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.MasterProjectService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.TrainingHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MasterProjectBOImpl implements MasterProjectBO {

    private final ApplicationConfig applicationConfig;

    @Autowired
    MasterProjectService masterProjectService;

    @Autowired
    TrainingHistoryService trainingHistoryService;

    @Autowired
    PersonService personService;

    @Autowired
    MasterProjectMapper masterProjectMapper;

    @Autowired
    TrainingHistoryMapper trainingHistoryMapper;

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
        ValidateTokenReqDTO validateTokenReqDTO = ValidateTokenReqDTO.builder()
                .user_token(request.getUserToken())
                .build();
        ValidateTokenResDTO validateTokenResDTO = Tddt4iotsUtil.postRequest(
                applicationConfig.getTddt4iotsServer() +
                        Tddt4iotsCons.URL_VALIDATE_TOKEN,
                validateTokenReqDTO,
                ValidateTokenResDTO.class
        );

        if(validateTokenResDTO.getStatus() != 2 && validateTokenResDTO.getData().getUserId() != null) {
            throw new GenericException(validateTokenResDTO.getInformation());
        }

        log.info(validateTokenResDTO.getInformation());
        PersonDTO personDTO = personMapper.personTo(
                personService.findById(validateTokenResDTO.getData().getUserId())
        );

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
            for (TrainingDataDTO trainingDataDTO : trainingDataDTOList) {
                String json = JSON_MAPPER.writeValueAsString(trainingDataDTO);
                jsonlContent.append(json).append("\n");
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
                trainingHistoryDTO.setCountRowDataTrining(Long.parseLong(String.valueOf(masterProjectDTOList.size())));
                trainingHistoryService.save(trainingHistoryMapper.trainingHistoryTo(trainingHistoryDTO));
                createFileTrainingResDTO.setPathCsvFile(pathFileJson);
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
        List<TrainingHistoryDTO> trainingHistoryDTOList = trainingHistoryMapper.trainingHistoryDTOListTo(
                trainingHistoryService.getTrainingFromToDate(getProjectFromDateReq));
        for(TrainingHistoryDTO trainingHistoryDTO : trainingHistoryDTOList) {
            // entrenamos todos los archivos que se han encontrado con la fecha indicada
            String response = grpcTrainingModelOpenAi.trainModel(trainingHistoryDTO.getPathFileJson());
            // actualizar los resultados del entrenamiento
            trainingHistoryDTO.setResultTrining(response);
            trainingHistoryService.save(trainingHistoryMapper.trainingHistoryTo(trainingHistoryDTO));
            TrainingModelOpenAiRes trainingModelOpenAiRes = TrainingModelOpenAiRes.builder()
                    .result(response)
                    .pathFileJson(trainingHistoryDTO.getPathFileJson())
                    .build();
            trainingModelOpenAiResList.add(trainingModelOpenAiRes);
        }

        return trainingModelOpenAiResList;
    }

    private List<ArmadilloApiResDTO> postMultipleRequests(String... names) {
        return Tddt4iotsUtil.postMultipleRequests(applicationConfig.getTddt4iotsArmadilloApi(),
                ArmadilloApiResDTO.class, ArmadilloApiReqDTO.class, names);
    }


}
