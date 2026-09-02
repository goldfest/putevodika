package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.auth.dto.AuthTokenResponse;
import ru.putevodika.auth.dto.LoginRequest;
import ru.putevodika.auth.dto.RefreshTokenRequest;
import ru.putevodika.auth.exception.InvalidCredentialsException;
import ru.putevodika.auth.exception.UserInactiveException;
import ru.putevodika.place.entity.Category;
import ru.putevodika.place.repository.CategoryRepository;
import ru.putevodika.security.JwtTokenService;
import ru.putevodika.user.dto.RegisterRequest;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.exception.LoginAlreadyUsedException;
import ru.putevodika.user.exception.UnknownPreferenceCategoryException;
import ru.putevodika.user.repository.UserRepository;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenService jwtTokenService;

    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthTokenResponse login(
            LoginRequest request
    ) {
        String login = normalizeLogin(
                request.getLogin()
        );

        UserAccount user =
                userRepository
                        .findByLoginIgnoreCase(login)
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        if (!user.isActive()) {
            throw new UserInactiveException();
        }

        String refreshToken =
                refreshTokenService.issue(user);

        return createTokenResponse(
                user,
                refreshToken
        );
    }

    @Transactional
    public AuthTokenResponse refresh(
            RefreshTokenRequest request
    ) {
        RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate(
                        request.getRefreshToken()
                );

        return createTokenResponse(
                rotated.user(),
                rotated.refreshToken()
        );
    }

    @Transactional
    public void logout(
            RefreshTokenRequest request
    ) {
        refreshTokenService.revoke(
                request.getRefreshToken()
        );
    }

    @Transactional
    public UserResponse register(
            RegisterRequest request
    ) {
        String login = normalizeLogin(
                request.getLogin()
        );

        if (userRepository.existsByLoginIgnoreCase(login)) {
            throw new LoginAlreadyUsedException(login);
        }

        Set<String> requestedCategories =
                request.getPreferredCategories() == null
                        ? Set.of()
                        : request.getPreferredCategories();

        Set<Category> categories =
                requestedCategories.isEmpty()
                        ? Set.of()
                        : categoryRepository
                        .findAllByCodeInAndActiveTrue(
                                requestedCategories
                        );

        validateCategories(
                requestedCategories,
                categories
        );

        String passwordHash =
                passwordEncoder.encode(
                        request.getPassword()
                );

        UserAccount user = new UserAccount(
                login,
                passwordHash,
                request.getDisplayName().trim()
        );

        if (request.getAvatarUrl() != null) {
            user.changeAvatarUrl(
                    normalizeAvatarUrl(
                            request.getAvatarUrl()
                    )
            );
        }

        user.replacePreferredCategories(
                categories
        );

        UserAccount saved =
                userRepository.save(user);

        return toResponse(saved);
    }

    private AuthTokenResponse createTokenResponse(
            UserAccount user,
            String refreshToken
    ) {
        JwtTokenService.AccessToken accessToken =
                jwtTokenService.createAccessToken(user);

        return AuthTokenResponse.builder()
                .accessToken(
                        accessToken.value()
                )
                .refreshToken(
                        refreshToken
                )
                .tokenType("Bearer")
                .accessExpiresInSeconds(
                        accessToken.expiresInSeconds()
                )
                .refreshExpiresInSeconds(
                        refreshTokenService
                                .getRefreshTokenTtlSeconds()
                )
                .build();
    }

    private String normalizeLogin(String login) {
        return login
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeAvatarUrl(
            String avatarUrl
    ) {
        String value = avatarUrl.trim();

        return value.isEmpty()
                ? null
                : value;
    }

    private void validateCategories(
            Set<String> requestedCodes,
            Set<Category> categories
    ) {
        Set<String> existingCodes =
                categories.stream()
                        .map(Category::getCode)
                        .collect(Collectors.toSet());

        Set<String> unknownCodes =
                new HashSet<>(requestedCodes);

        unknownCodes.removeAll(existingCodes);

        if (!unknownCodes.isEmpty()) {
            throw new UnknownPreferenceCategoryException(
                    unknownCodes
            );
        }
    }

    private UserResponse toResponse(
            UserAccount user
    ) {
        Set<String> preferredCategories =
                user.getPreferredCategories()
                        .stream()
                        .map(Category::getCode)
                        .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .login(user.getLogin())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .active(user.isActive())
                .preferredCategories(
                        preferredCategories
                )
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}