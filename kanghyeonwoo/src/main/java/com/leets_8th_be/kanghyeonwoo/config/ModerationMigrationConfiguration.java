package com.leets_8th_be.kanghyeonwoo.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ModerationMigrationConfiguration {
    @Bean(initMethod = "migrate")
    @DependsOnDatabaseInitialization
    ModerationSchemaMigration moderationSchemaMigration(DataSource source) {
        return new ModerationSchemaMigration(source);
    }

    @Bean
    static EntityManagerFactoryDependsOnPostProcessor moderationMigrationDependency() {
        return new EntityManagerFactoryDependsOnPostProcessor("moderationSchemaMigration");
    }
}
