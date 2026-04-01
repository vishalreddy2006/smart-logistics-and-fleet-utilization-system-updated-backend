package com.smartlogistics.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

   @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        // ✅ Enable CORS (uses your CorsConfig.java)
        .cors(Customizer.withDefaults())

        // ✅ Disable CSRF (for APIs)
        .csrf(AbstractHttpConfigurer::disable)

        // ✅ Stateless (JWT based)
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        // ✅ Authorization rules
        .authorizeHttpRequests(auth -> auth

            // 🔥 CRITICAL: allow preflight requests
            .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

            // ✅ Public auth endpoints
            .requestMatchers("/api/auth/**").permitAll()

            // ✅ Allow all API (you can restrict later)
            .requestMatchers("/api/**").permitAll()

            // ✅ Everything else allowed (for now)
            .anyRequest().permitAll()
        )

        // ✅ JWT filter
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
