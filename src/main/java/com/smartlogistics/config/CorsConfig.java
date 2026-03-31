package com.smartlogistics.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Use patterns instead of strict origins (Render-friendly)
        configuration.setAllowedOriginPatterns(List.of(
            "https://*.onrender.com",
            "https://vishalreddy2006.github.io",
            "http://localhost:*"
        ));

        // ✅ Explicit methods
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // ✅ Avoid "*" when using credentials
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept"
        ));

        configuration.setExposedHeaders(List.of("Authorization"));

        // ✅ Required for JWT cookies / auth headers
        configuration.setAllowCredentials(true);

        // Optional: cache preflight response (performance)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}