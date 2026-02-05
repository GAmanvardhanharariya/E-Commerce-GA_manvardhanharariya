package com.ecommerce.demo.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**"
                        ).permitAll()

                        // CART (any logged-in user)
                        .requestMatchers("/api/v1/cart/**").authenticated()

                        // ADMIN WRITE
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/products/**",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/products/**",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/products/**",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
