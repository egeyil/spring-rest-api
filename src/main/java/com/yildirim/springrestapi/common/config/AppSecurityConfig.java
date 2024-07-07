package com.yildirim.springrestapi.common.config;

import com.yildirim.springrestapi.features.auth.JpaUserDetailsService;
import com.yildirim.springrestapi.features.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppSecurityConfig {
    private final JpaUserDetailsService jpaUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher((request) -> request.getServletPath().startsWith(apiPrefix))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/auth/public/**",
                                "/api/v2/api-docs",
                                "/api/v3/api-docs",
                                "/api/v3/api-docs/**",
                                "/api/swagger-resources",
                                "/api/swagger-resources/**",
                                "/api/configuration/ui",
                                "/api/configuration/security",
                                "/api/swagger-ui/**",
                                "/api/webjars/**",
                                "/api/swagger-ui.html",
                                "/api/docs"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                /*
                .oauth2ResourceServer(server -> server.jwt((Customizer.withDefaults())))
                .rememberMe((rememberMe) ->
                        rememberMe
                                .key(jwtService.getRefreshTokenKey())
                )
                */
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Data access object which is responsible to fetch user details
     */
    @Bean
    public AuthenticationManager daoProvider() {
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setUserDetailsService(jpaUserDetailsService);
        daoAuthProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(daoAuthProvider);
    }
}

