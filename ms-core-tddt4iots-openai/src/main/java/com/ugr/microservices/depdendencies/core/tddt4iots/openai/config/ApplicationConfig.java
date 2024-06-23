package com.ugr.microservices.depdendencies.core.tddt4iots.openai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@Getter
@Setter
public class ApplicationConfig {

    @Value("${spring.path.tddt4iots.util.nfs}")
    private String tddt4iotsNfs;

    @Value("${spring.path.tddt4iots.util.server}")
    private String tddt4iotsServer;

    @Value("${spring.path.tddt4iots.util.armadillo}")
    private String tddt4iotsArmadilloApi;

}
