package ru.putevodika.admin.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.putevodika.admin.security.AdminLoginRateLimiter;
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
    private final AdminLoginRateLimiter loginRateLimiter;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginRequest());
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginRequest loginForm,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("loginError", "Проверьте введённые данные.");
            return "admin/login";
        }

        String remoteAddress = request.getRemoteAddr();
        String email = loginForm.getEmail();

        if (!loginRateLimiter.canAttempt(email, remoteAddress)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            model.addAttribute("loginError",
                    "Слишком много попыток входа. Повторите попытку через 15 минут.");
            return "admin/login";
        }

        try {
            AuthService.AuthSession session = authService.loginAdmin(loginForm);
            loginRateLimiter.recordSuccess(email, remoteAddress);
            authCookieService.writeSessionCookies(
                    response, session.accessToken(), session.refreshToken());
            return "redirect:/admin";
        } catch (InvalidCredentialsException | UserInactiveException exception) {
            loginRateLimiter.recordFailure(email, remoteAddress);
            model.addAttribute("loginError",
                    "Неверные данные для входа или отсутствует доступ администратора.");
            return "admin/login";
        }
    }
}
