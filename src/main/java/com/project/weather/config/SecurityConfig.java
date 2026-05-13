package com.project.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${api.key}")
    private String apiKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for stateless API
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to health and Swagger endpoints
                        .requestMatchers("/actuator/health", "/actuator/info", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());  // Enable HTTP Basic Auth with API Key as username

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Create an in-memory user with the API key as password
        // The username is "api" but not actually used - the password is the key
        UserDetails user = User.builder()
                .username("api")
                .password("{noop}" + apiKey)  // {noop} means no password encoding
                .roles("API_CLIENT")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}