package com.mathlit.backend.config;

import com.mathlit.backend.filter.AdminTokenFilter;
import com.mathlit.backend.filter.FirebaseAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;
    private final AdminTokenFilter adminTokenFilter;

    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter,
                          AdminTokenFilter adminTokenFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
        this.adminTokenFilter   = adminTokenFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health").permitAll()
                // Admin static files (HTML/CSS/JS) + login endpoint need no Firebase auth
                .requestMatchers("/admin/**").permitAll()
                .anyRequest().authenticated()
            )
            // AdminTokenFilter runs first; it self-skips non-admin-API paths
            .addFilterBefore(adminTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(firebaseAuthFilter, AdminTokenFilter.class);
        return http.build();
    }
}
