package ru.putevodika.admin.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import ru.putevodika.auth.dto.LoginRequest;
import ru.putevodika.auth.exception.InvalidCredentialsException;
import ru.putevodika.auth.exception.UserInactiveException;
import ru.putevodika.auth.service.AuthCookieService;
import ru.putevodika.auth.service.AuthService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AuthService authService;

    private final AuthCookieService authCookieService;

    @GetMapping("/login")
    public String loginPage(Model model) {

        model.addAttribute(
                "loginForm",
                new LoginRequest()
        );

        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm")
            LoginRequest loginForm,

            BindingResult bindingResult,

            HttpServletResponse response,

            Model model
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "loginError",
                    "Проверьте введённые данные."
            );

            return "admin/login";
        }

        try {
            AuthService.AuthSession session =
                    authService.loginAdmin(loginForm);

            authCookieService.writeSessionCookies(
                    response,
                    session.accessToken(),
                    session.refreshToken()
            );

            return "redirect:/admin";

        } catch (
                InvalidCredentialsException |
                UserInactiveException exception
        ) {

            model.addAttribute(
                    "loginError",
                    "Неверные данные для входа " +
                            "или отсутствует доступ администратора."
            );

            return "admin/login";
        }
    }
}