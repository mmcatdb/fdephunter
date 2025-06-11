package de.uni.passau.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni.passau.server.Configuration.DatabaseProperties;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
public class DatabaseConfiguration {

    @Autowired
    private DatabaseProperties databaseProperties;

    @Bean
    public Driver neo4jDriver() {
        String url = new StringBuilder()
            .append("bolt://")
            .append(databaseProperties.host())
            .append(":")
            .append(databaseProperties.port())
            .toString();

        return GraphDatabase.driver(url, AuthTokens.basic(databaseProperties.username(), databaseProperties.password()));
    }

    @Bean
    public ReactiveTransactionManager reactiveTransactionManager(Driver neo4jDriver) {
        return new ReactiveNeo4jTransactionManager(neo4jDriver);
    }

    @Bean
    public Neo4jClient neo4jClient(Driver neo4jDriver) {
        return Neo4jClient.create(neo4jDriver);
    }

    @Bean
    public Neo4jTemplate neo4jTemplate(Neo4jClient neo4jClient/*, ReactiveTransactionManager reactiveTransactionManager*/) {
        return new Neo4jTemplate(neo4jClient);
    }

    @Bean
    public ObjectMapper objectMapperJSON() {
        return new ObjectMapper();
    }

}
