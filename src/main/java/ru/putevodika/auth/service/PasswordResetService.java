package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.auth.dto.PasswordResetConfirmRequest;
import ru.putevodika.auth.exception.InvalidPasswordResetTokenException;
import ru.putevodika.auth.exception.PasswordResetPasswordsMismatchException;
import ru.putevodika.auth.exception.PasswordResetRateLimitException;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.repository.UserRepository;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PasswordResetTokenService tokenService;

    private final PasswordResetRateLimiter rateLimiter;

    private final PasswordResetMailService mailService;

    private final RefreshTokenService refreshTokenService;

    public void requestReset(
            String email,
            String remoteAddress
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        if (!rateLimiter.allowIp(remoteAddress)) {
            throw new PasswordResetRateLimitException();
        }

        boolean emailAllowed =
                rateLimiter.allowEmail(
                        normalizedEmail
                );

        // Генерация выполняется и для несуществующего email,
        // чтобы уменьшить разницу во времени ответа.
        tokenService.generateUnusedTokenCandidate();

        Optional<UserAccount> userOptional =
                userRepository
                        .findByEmailIgnoreCase(
                                normalizedEmail
                        );

        if (!emailAllowed
                || userOptional.isEmpty()
                || !userOptional.get().isActive()) {

            return;
        }

        UserAccount user =
                userOptional.get();

        PasswordResetTokenService.IssuedToken issued =
                tokenService.issue(
                        user.getId()
                );

        mailService.sendResetLink(
                user.getEmail(),
                issued.rawToken()
        );
    }

    public boolean validateToken(
            String rawToken
    ) {
        return tokenService.isValid(rawToken);
    }

    @Transactional
    public void confirmReset(
            PasswordResetConfirmRequest request
    ) {
        if (!request
                .getNewPassword()
                .equals(
                        request.getRepeatPassword()
                )) {

            throw new PasswordResetPasswordsMismatchException();
        }

        Long userId =
                tokenService.consume(
                        request.getToken()
                );

        UserAccount user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                InvalidPasswordResetTokenException::new
                        );

        if (!user.isActive()) {
            throw new InvalidPasswordResetTokenException();
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.changePasswordHash(
                passwordHash
        );

        user.incrementAuthVersion();

        refreshTokenService.revokeAllForUser(
                userId
        );

        mailService.sendPasswordChangedNotice(
                user.getEmail()
        );
    }

    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
