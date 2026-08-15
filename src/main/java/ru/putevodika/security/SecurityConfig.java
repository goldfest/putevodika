package ru.putevodika.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize ->
                        authorize

                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()

                                // Регистрация и вход
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login"
                                )
                                .permitAll()

                                // Служебные публичные endpoints
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/ping",
                                        "/actuator/health"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/api/v1/admin/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        "/api/v1/users/**"
                                )
                                .authenticated()

                                // Категории
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/categories"
                                )
                                .permitAll()

                                // Числовые характеристики
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/features"
                                )
                                .permitAll()

                                // Карточка места и spatial API
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/places/*"
                                )
                                .permitAll()

                                // Административный каталог
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/places"
                                )
                                .hasRole("ADMIN")

                                // Изменение Place
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/places"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/v1/places/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/v1/places/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/v1/places/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        "/api/v1/routes/**"
                                )
                                .authenticated()

                                // Все остальное требует входа
                                .anyRequest()
                                .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }


    @Bean
    public JwtAuthenticationConverter
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter rolesConverter =
                new JwtGrantedAuthoritiesConverter();

        rolesConverter.setAuthoritiesClaimName(
                "roles"
        );

        rolesConverter.setAuthorityPrefix(
                "ROLE_"
        );

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                rolesConverter
        );

        return converter;
    }
}