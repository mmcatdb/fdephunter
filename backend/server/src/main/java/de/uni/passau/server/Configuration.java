package de.uni.passau.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

public class Configuration {

    @ConfigurationProperties("server")
    public record ServerProperties(
        Integer port,
        String origin,
        String datasetDirectory
    ) {}

    @ConfigurationProperties("database")
    public record DatabaseProperties(
        String host,
        String port,
        String username,
        String password,
        String database
    ) {}

}
