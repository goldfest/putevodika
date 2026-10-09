package ru.putevodika.admin.web;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.putevodika.auth.service.AuthCookieService;
import ru.putevodika.auth.service.AuthService;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AdminLogoutController {

    private final AuthService authService;

    private final AuthCookieService authCookieService;

    @PostMapping("/admin-logout")
    public String logout(
            @CookieValue(
                    name = AuthCookieService.REFRESH_TOKEN_COOKIE,
                    required = false
            )
            String refreshToken,

            HttpServletResponse response
    ) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        authCookieService.clearSessionCookies(response);

        return "redirect:/admin/login";
    }
}