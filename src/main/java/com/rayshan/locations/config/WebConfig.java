package com.rayshan.locations.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Allow access to all API endpoints
                .allowedOrigins("http://localhost:3000") // Allow React app to make requests
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Specify allowed methods
                .allowCredentials(true); // Allow credentials (cookies, etc.)
    }
}
