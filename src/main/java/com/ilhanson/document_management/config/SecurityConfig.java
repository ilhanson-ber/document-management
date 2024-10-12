package com.ilhanson.document_management.config;

import com.ilhanson.document_management.services.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import static com.ilhanson.document_management.models.Role.EDITOR;
import static com.ilhanson.document_management.models.Role.READER;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FilterChainExceptionHandler filterChainExceptionHandler;

    private static final String[] URL_WHITELIST = {
            // login and signup
            "/api/v1/auth/**",
            // openapi and swagger
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            // spring uses this for all errors
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry
                                .requestMatchers(URL_WHITELIST)
                                .permitAll()
                                .requestMatchers(GET, "/api/v1/authors").hasAnyRole(READER.name(), EDITOR.name())
                                .requestMatchers(GET, "/api/v1/authors/**").hasAnyRole(READER.name(), EDITOR.name())
                                .requestMatchers(GET, "/api/v1/documents").hasAnyRole(READER.name(), EDITOR.name())
                                .requestMatchers(GET, "/api/v1/documents/**").hasAnyRole(READER.name(), EDITOR.name())
                                .requestMatchers(POST, "/api/v1/authors").hasRole(EDITOR.name())
                                .requestMatchers(POST, "/api/v1/authors/**").hasRole(EDITOR.name())
                                .requestMatchers(PUT, "/api/v1/authors/**").hasRole(EDITOR.name())
                                .requestMatchers(DELETE, "/api/v1/authors/**").hasRole(EDITOR.name())
                                .requestMatchers(POST, "/api/v1/documents").hasRole(EDITOR.name())
                                .requestMatchers(POST, "/api/v1/documents/**").hasRole(EDITOR.name())
                                .requestMatchers(PUT, "/api/v1/documents/**").hasRole(EDITOR.name())
                                .requestMatchers(DELETE, "/api/v1/documents/**").hasRole(EDITOR.name())
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(exh -> exh.authenticationEntryPoint(
                        (request, response, ex) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filterChainExceptionHandler, LogoutFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
