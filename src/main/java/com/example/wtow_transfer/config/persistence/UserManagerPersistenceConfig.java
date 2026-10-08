package com.example.wtow_transfer.config.persistence;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.wtow_transfer.jpa.repository.usermanager",
        entityManagerFactoryRef = "userManagerEntityManagerFactory",
        transactionManagerRef = "userManagerTransactionManager"
)
public class UserManagerPersistenceConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean userManagerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("userManagerDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.wtow_transfer.jpa.entity")
                .persistenceUnit("userManager")
                .build();
    }

    @Bean
    public PlatformTransactionManager userManagerTransactionManager(
            @Qualifier("userManagerEntityManagerFactory") EntityManagerFactory emf
            ) {
        return new JpaTransactionManager(emf);
    }
}
