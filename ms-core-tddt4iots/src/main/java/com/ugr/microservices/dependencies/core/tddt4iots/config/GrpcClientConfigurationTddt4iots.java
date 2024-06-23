package com.ugr.microservices.dependencies.core.tddt4iots.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class GrpcClientConfigurationTddt4iots {

    @Value("#{${grpc.cliente.map:{}}}")
    private Map<String, String> grpcClienteConfigMap;
    @Value("${grpc.idle.timeout:0}")
    private int idleTimeout;

    public GrpcClientConfigurationTddt4iots() {
    }

    @Bean
    public Map<String, ManagedChannel> getManagedChannelMap() {
        Map<String, ManagedChannel> channelMap = new HashMap();
        if (this.grpcClienteConfigMap != null && !this.grpcClienteConfigMap.isEmpty()) {
            Iterator var3 = this.grpcClienteConfigMap.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var3.next();
                ManagedChannel channel = ManagedChannelBuilder.forTarget((String)entry.getValue()).usePlaintext().idleTimeout((long)this.idleTimeout, TimeUnit.MINUTES).build();
                channelMap.put((String)entry.getKey(), channel);
            }
        }

        return channelMap;
    }

}
