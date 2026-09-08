package ru.putevodika.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import ru.putevodika.security.JwtProperties;
import ru.putevodika.security.SecurityWebProperties;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE =
            "access_token";

    public static final String REFRESH_TOKEN_COOKIE =
            "refresh_token";

    private static final String ACCESS_COOKIE_PATH = "/api";

    private static final String REFRESH_COOKIE_PATH =
            "/api/v1/auth";

    private final JwtProperties jwtProperties;

    private final SecurityWebProperties webProperties;

    public void writeSessionCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        addCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                accessToken,
                ACCESS_COOKIE_PATH,
                jwtProperties.accessTokenTtl()
        );

        addCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                refreshToken,
                REFRESH_COOKIE_PATH,
                jwtProperties.refreshTokenTtl()
        );
    }

    public void clearSessionCookies(
            HttpServletResponse response
    ) {
        clearCookie(
                response,
                ACCESS_TOKEN_COOKIE,
                ACCESS_COOKIE_PATH
        );

        clearCookie(
                response,
                REFRESH_TOKEN_COOKIE,
                REFRESH_COOKIE_PATH
        );
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            String path,
            Duration maxAge
    ) {
        ResponseCookie cookie =
                ResponseCookie.from(name, value)
                        .httpOnly(true)
                        .secure(webProperties.cookieSecure())
                        .sameSite(webProperties.cookieSameSite())
                        .path(path)
                        .maxAge(maxAge)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    private void clearCookie(
            HttpServletResponse response,
            String name,
            String path
    ) {
        ResponseCookie cookie =
                ResponseCookie.from(name, "")
                        .httpOnly(true)
                        .secure(webProperties.cookieSecure())
                        .sameSite(webProperties.cookieSameSite())
                        .path(path)
                        .maxAge(Duration.ZERO)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}
