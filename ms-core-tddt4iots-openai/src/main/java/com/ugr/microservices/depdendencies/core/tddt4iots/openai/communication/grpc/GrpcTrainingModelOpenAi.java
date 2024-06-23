package com.ugr.microservices.depdendencies.core.tddt4iots.openai.communication.grpc;

import com.ugr.microservices.depdendencies.core.tddt4iots.openai.config.GrpcConnectionManager;
import grpc_service.OpenAIServiceGrpc;
import grpc_service.Openai;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrpcTrainingModelOpenAi {

    private final GrpcConnectionManager grpcConnectionManager;

    public String trainModel(String jsonData) {
        Openai.TrainRequest request = Openai.TrainRequest.newBuilder()
                .setJsonData(jsonData)
                .build();
        OpenAIServiceGrpc.OpenAIServiceBlockingStub openAIServiceBlockingStub  = grpcConnectionManager.getStubByChannel("integrator", OpenAIServiceGrpc.class, "newBlockingStub");
        Openai.TrainResponse response = openAIServiceBlockingStub.trainModel(request);
        return response.getMessage();
    }

}
