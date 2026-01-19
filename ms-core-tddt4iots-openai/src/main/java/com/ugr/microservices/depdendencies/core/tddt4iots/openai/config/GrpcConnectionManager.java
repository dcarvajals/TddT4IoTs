package com.ugr.microservices.depdendencies.core.tddt4iots.openai.config;

import com.ugr.microservices.dependencies.core.tddt4iots.config.GrpcClientConfigurationTddt4iots;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GrpcConnectionManager {
    @Value("${grpc.deadline.after.timeout:0}")
    private int deadlineAfterTimeline;
    private final GrpcClientConfigurationTddt4iots clientConfig;

    public GrpcConnectionManager(GrpcClientConfigurationTddt4iots clientConfig) {
        this.clientConfig = clientConfig;
    }

    public <T extends AbstractStub<T>> T getStubByChannel(String serverClient, Class<?> serviceClass, String methodName) {
        try {
            Map<String, ManagedChannel> channelMap = this.clientConfig.getManagedChannelMap();
            ManagedChannel channel = (ManagedChannel)channelMap.get(serverClient);
            Method newStubMethod = null;
            Method[] var7 = serviceClass.getMethods();
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                Method method = var7[var9];
                if (method.getName().equals(methodName)) {
                    newStubMethod = method;
                    break;
                }
            }

            if (newStubMethod == null) {
                throw new NoSuchMethodException(String.format("El método %s no fue encontrado en la clase %s.", methodName, serviceClass.getName()));
            } else {
                T stub = (T) newStubMethod.invoke((Object)null, channel);
                stub = stub.withWaitForReady().withDeadlineAfter((long)this.deadlineAfterTimeline, TimeUnit.SECONDS);
                return stub;
            }
        } catch (Exception var11) {
            Exception e = var11;
            throw new RuntimeException(e.getMessage());
        }
    }
}
