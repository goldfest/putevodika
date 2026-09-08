package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.auth.dto.LoginRequest;
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
    public AuthSession login(
            LoginRequest request
    ) {
        String email = normalizeEmail(
                request.getEmail()
        );

        UserAccount user =
                userRepository
                        .findByEmailIgnoreCase(email)
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

        return createSession(
                user,
                refreshToken
        );
    }

    @Transactional
    public AuthSession refresh(
            String refreshToken
    ) {
        RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate(
                        refreshToken
                );

        return createSession(
                rotated.user(),
                rotated.refreshToken()
        );
    }

    @Transactional
    public void logout(
            String refreshToken
    ) {
        refreshTokenService.revoke(refreshToken);
    }

    @Transactional
    public UserResponse register(
            RegisterRequest request
    ) {
        String email = normalizeEmail(
                request.getEmail()
        );

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new LoginAlreadyUsedException(email);
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
                email,
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

    private AuthSession createSession(
            UserAccount user,
            String refreshToken
    ) {
        JwtTokenService.AccessToken accessToken =
                jwtTokenService.createAccessToken(user);

        return new AuthSession(
                toResponse(user),
                accessToken.value(),
                refreshToken
        );
    }

    private String normalizeEmail(String email) {
        return email
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
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .active(user.isActive())
                .preferredCategories(
                        preferredCategories
                )
                .onboardingCompleted(
                        user.isOnboardingCompleted()
                )
                .onboardingCompletedAt(
                        user.getOnboardingCompletedAt()
                )
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public record AuthSession(
            UserResponse user,
            String accessToken,
            String refreshToken
    ) {
    }
}
