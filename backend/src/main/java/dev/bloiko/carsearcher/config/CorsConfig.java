package dev.bloiko.carsearcher.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Local dev only: the frontend (Vite, http://localhost:5173) and backend
 * (http://localhost:8080) are different origins, so the browser blocks
 * fetches without this — confirmed by actually running both together
 * (no test exercises real cross-origin behavior). Not something to widen
 * for a deployed environment without reconsidering the allowed origin.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST");
    }
}
