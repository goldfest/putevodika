package ru.putevodika.security;

import jakarta.servlet.http.Cookie;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.putevodika.auth.service.AuthCookieService;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties(SecurityWebProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            BearerTokenResolver bearerTokenResolver,
            CookieCsrfTokenRepository csrfTokenRepository
    ) throws Exception {

        CsrfTokenRequestAttributeHandler csrfHandler =
                new CsrfTokenRequestAttributeHandler();

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf ->
                        csrf
                                .csrfTokenRepository(
                                        csrfTokenRepository
                                )
                                .csrfTokenRequestHandler(
                                        csrfHandler
                                )
                )

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

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/auth/csrf",
                                        "/api/v1/auth/password-reset/validate"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh",
                                        "/api/v1/auth/logout",
                                        "/api/v1/auth/password-reset/request",
                                        "/api/v1/auth/password-reset/confirm"
                                )
                                .permitAll()

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

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/categories"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/features"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/places/*"
                                )
                                .permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/places"
                                )
                                .hasRole("ADMIN")

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

                                .anyRequest()
                                .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .bearerTokenResolver(
                                        bearerTokenResolver
                                )
                                .jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter
                                        )
                                )
                );

        return http.build();
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository(
            SecurityWebProperties properties
    ) {
        CookieCsrfTokenRepository repository =
                CookieCsrfTokenRepository
                        .withHttpOnlyFalse();

        repository.setCookieCustomizer(cookie ->
                cookie
                        .path("/")
                        .secure(properties.cookieSecure())
                        .sameSite(properties.cookieSameSite())
        );

        return repository;
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        DefaultBearerTokenResolver headerResolver =
                new DefaultBearerTokenResolver();

        return request -> {
            String headerToken =
                    headerResolver.resolve(request);

            if (headerToken != null) {
                return headerToken;
            }

            Cookie[] cookies = request.getCookies();

            if (cookies == null) {
                return null;
            }

            return Arrays.stream(cookies)
                    .filter(cookie ->
                            AuthCookieService.ACCESS_TOKEN_COOKIE
                                    .equals(cookie.getName())
                    )
                    .map(Cookie::getValue)
                    .filter(value ->
                            value != null && !value.isBlank()
                    )
                    .findFirst()
                    .orElse(null);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            SecurityWebProperties properties
    ) {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                properties.allowedOrigins()
        );
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );
        configuration.setAllowedHeaders(
                List.of(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.AUTHORIZATION,
                        "X-XSRF-TOKEN"
                )
        );
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
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
