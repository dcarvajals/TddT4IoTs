package com.ugr.microservices.dependencies.scheme.tddt4iots.config;

import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphJpaRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "Tddt4IoTsEMFactory",
        transactionManagerRef = "Tddt4IoTsTransactionManager",
        basePackages = {"com.ugr.microservices.dependencies.scheme.tddt4iots.repository"},
        repositoryFactoryBeanClass = EntityGraphJpaRepositoryFactoryBean.class
)
@EntityScan(basePackages = { "com.ugr.microservices.dependencies.scheme.tddt4iots.entity" })
public class DsTddt4iotsConfig {
}
