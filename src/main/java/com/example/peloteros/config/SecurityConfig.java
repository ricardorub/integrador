/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.peloteros.config;

import com.example.peloteros.security.jwt.JwtRequestFilter;
import com.example.peloteros.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // For ignoring CSRF on specific paths

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return (request, response, authException) -> 
               response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")) // Enable CSRF but ignore for /api/**
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(
                                        "/", "/home/**",
                                        "/usuario/login", "/usuario/home/registro", // login & registration pages
                                        // "/logout" is handled by Spring Security, permitAll for logoutSuccessUrl if needed
                                        "/login", "/registro", // Old paths, kept if still used
                                        "/api/authenticate",   // JWT auth endpoint
                                        "/css/**", "/js/**", "/img/**", "/vendor/**", "/scss/**"
                                ).permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN") // Added rule for admin section
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // Allow sessions
                .userDetailsService(customUserDetailsService) 
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class) // JWT filter
                .formLogin(form -> form // Re-enable form login
                        .loginPage("/usuario/login")
                        .loginProcessingUrl("/usuario/login") // URL where the login form POSTs
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/usuario/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout // Re-enable logout
                        .logoutUrl("/logout") // Standard Spring Security logout URL (handles POST to /logout)
                        .logoutSuccessUrl("/usuario/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> 
                        // For API calls, jwtAuthenticationEntryPoint will be triggered due to the filter chain order and request matching.
                        // For browser-based interactions not handled by JWT (e.g. no token), formLogin's default entry point
                        // (redirecting to /usuario/login) will typically take precedence or be invoked.
                        exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint())
                ); 
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

