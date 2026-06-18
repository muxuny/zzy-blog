package com.blog.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebMvcConfigTest {

    @Test
    void addCorsMappings_shouldAllowLocalhostAndLoopbackFrontendOrigins() {
        CapturingCorsRegistry registry = new CapturingCorsRegistry();

        new WebMvcConfig().addCorsMappings(registry);

        assertTrue(registry.allowedOrigins.contains("http://localhost:5173"));
        assertTrue(registry.allowedOrigins.contains("http://127.0.0.1:5173"));
    }

    private static class CapturingCorsRegistry extends CorsRegistry {
        private final List<String> allowedOrigins = new ArrayList<>();

        @Override
        public CorsRegistration addMapping(String pathPattern) {
            return new CapturingCorsRegistration(pathPattern, allowedOrigins);
        }
    }

    private static class CapturingCorsRegistration extends CorsRegistration {
        private final List<String> allowedOrigins;

        CapturingCorsRegistration(String pathPattern, List<String> allowedOrigins) {
            super(pathPattern);
            this.allowedOrigins = allowedOrigins;
        }

        @Override
        public CorsRegistration allowedOrigins(String... origins) {
            allowedOrigins.addAll(Arrays.asList(origins));
            return super.allowedOrigins(origins);
        }
    }
}
