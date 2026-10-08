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
        basePackages = "com.example.wtow_transfer.jpa.repository.accountmanager",
        entityManagerFactoryRef = "accountManagerEntityManagerFactory",
        transactionManagerRef = "accountManagerTransactionManager"
)
public class AccountManagerPersistenceConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean accountManagerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("accountManagerDataSource") DataSource dataSource
            ) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.wtow_transfer.jpa.entity")
                .persistenceUnit("accountManager")
                .build();
    }

    @Bean
    public PlatformTransactionManager accountManagerTransactionManager(
            @Qualifier("accountManagerEntityManagerFactory") EntityManagerFactory emf
            ) {
        return new JpaTransactionManager(emf);
    }
}
