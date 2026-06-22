package com.training.mts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 1. Remove "*" and specify exactly who can talk to your backend
                .allowedOriginPatterns(
                        "http://localhost:4200",     // Your local Angular app
                        "http://localhost:[*]"     // Fallback for any other local port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // Now safely allowed by SonarQube
    }
}
