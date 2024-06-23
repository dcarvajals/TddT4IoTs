package com.ugr.microservices.depdendencies.core.tddt4iots.openai.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DbPostgreSqlConfig {

    @Value("${spring.datasource.url}")
    private String pathJdbc;

    @Value("${spring.datasource.username}")
    private String userNameDataBase;

    @Value("${spring.datasource.password}")
    private String passwordDataBase;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassname;

    @Bean(name = "dsTddt4iots")
    public DataSource dsTddt4iots(@Qualifier("dsTddt4iotsProperties") HikariConfig dataSourceConfig) {
        return new HikariDataSource(dataSourceConfig);
    }

    @Primary
    @Bean(name = "dsTddt4iotsProperties")
    public HikariConfig dsComprobantesConfig() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(pathJdbc);
        config.setUsername(userNameDataBase);
        config.setPassword(passwordDataBase);
        config.setDriverClassName(driverClassname);
        // Configuración adicional de HikariCP si es necesario
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        return config;
    }

    @Primary
    @Bean(name = "jdbcTddt4iots")
    @Autowired
    public JdbcTemplate jdbcFlowersTemplate(@Qualifier("dsTddt4iots") DataSource dsTddt4iots) {
        return new JdbcTemplate(dsTddt4iots);
    }

    @Primary
    @Bean(name = "Tddt4IoTsEMFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryTddt4iots(EntityManagerFactoryBuilder builder, @Qualifier("dsTddt4iots") DataSource dsTddt4iots) {
        return builder.dataSource(dsTddt4iots).packages("com.ugr.microservices.dependencies.scheme.tddt4iots.entity")
                .persistenceUnit("dsTddt4iots").build();
    }

    @Primary
    @Bean(name = "Tddt4IoTsTransactionManager")
    public PlatformTransactionManager transactionManagerTddt4iots(
            @Qualifier("Tddt4IoTsEMFactory") EntityManagerFactory tddt4IoTsEMFactory) {
        return new JpaTransactionManager(tddt4IoTsEMFactory);
    }

}
