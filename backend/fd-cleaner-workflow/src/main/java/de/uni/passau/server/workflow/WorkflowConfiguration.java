/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 *
 * @author pavel.koupil
 */
@Configuration
public class WorkflowConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowConfiguration.class);

    public static class Neo4jConfiguration {

        private String host;

        private String port;

        private String username;

        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Neo4jConfiguration{");
            sb.append("host=").append(host);
            sb.append(", port=").append(port);
            sb.append(", username=").append(username);
            sb.append(", password=").append(password);
            sb.append('}');
            return sb.toString();
        }

    }

    @Bean
    public Neo4jConfiguration neo4jConfiguration() {
        LOGGER.warn("THIS CONFIGURATION HAS TO BE MOVED TO CONFIG FILE, DO NOT HARDCODE!");
        var config = new Neo4jConfiguration();
        config.setHost("localhost");
        config.setPort("7687");
        config.setUsername("neo4j");
        config.setPassword("password");
        return config;
    }

    @Bean
    public Driver neo4jDriver(@Qualifier("neo4jConfiguration") Neo4jConfiguration config) {
        String uri = String.join("", "bolt://", config.getHost(), ":", config.getPort());
        return GraphDatabase.driver(uri, AuthTokens.basic(config.getUsername(), config.getPassword()));
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
