package de.uni.passau.server;

import de.uni.passau.server.Configuration.ServerProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SpringBootApplication
// First load the default.properties file. Then it will be overriden by application.properties.
@PropertySource("classpath:default.properties")
@ComponentScan({ "de.uni.passau.server" })
// This is needed to make the @ConfigurationProperties annotations work with records.
@ConfigurationPropertiesScan({ "de.uni.passau.server" })
public class App {

    private static ApplicationContext applicationContext;

    public static void main(String... args) {
        applicationContext = SpringApplication.run(App.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(ServerProperties server) {
        return new WebMvcConfigurer() {
            @Override public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry
                    .addMapping("/**")
                    .allowedMethods("GET", "POST", "OPTIONS")
                    .allowedOrigins(server.origin())
                    .allowCredentials(true);
            }
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

}
