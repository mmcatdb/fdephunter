/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package de.uni.passau.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author pavel.koupil
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.uni.passau.server"})
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static ApplicationContext applicationContext;

    public static void main(String... args) {
        applicationContext = SpringApplication.run(App.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(ServerProperties server) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/**")
                    .allowedMethods("GET", "POST", "OPTIONS")
                    .allowedOrigins(server.origin())
                    .allowCredentials(true);
            }
        };
    }

}
