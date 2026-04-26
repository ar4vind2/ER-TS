package com.erts.config;

import com.erts.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Preflight requests must pass before protected endpoints
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ── Public endpoints ──────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()

                // ── ORGANISER endpoints ───────────────────────────────────────
                // Organisers can create and manage their own events
                .requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole("ORGANISER", "ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/events/**").hasAnyRole("ORGANISER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("ORGANISER", "ADMIN")

                // Organisers can view registrations for their events
                .requestMatchers("/api/registrations/event/**").hasAnyRole("ORGANISER", "ADMIN")
                .requestMatchers("/api/registrations/all").hasRole("ADMIN")

                // ── ADMIN-only endpoints ──────────────────────────────────────
                // Event approval (admin approves organiser-created events)
                .requestMatchers("/api/events/*/approve").hasRole("ADMIN")
                .requestMatchers("/api/events/*/reject").hasRole("ADMIN")
                .requestMatchers("/api/events/pending-approval").hasRole("ADMIN")

                // Payment verification
                .requestMatchers("/api/payments/pending").hasRole("ADMIN")
                .requestMatchers("/api/payments/*/approve").hasRole("ADMIN")
                .requestMatchers("/api/payments/*/reject").hasRole("ADMIN")

                // Ticket management
                .requestMatchers("/api/tickets/all").hasRole("ADMIN")
                .requestMatchers("/api/tickets/*/revoke").hasRole("ADMIN")

                // User management
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // Organiser management
                .requestMatchers("/api/organisers/**").hasRole("ADMIN")

                // ── Any authenticated user ────────────────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
