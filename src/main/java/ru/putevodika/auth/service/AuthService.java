package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.entity.Category;
import ru.putevodika.place.repository.CategoryRepository;
import ru.putevodika.user.dto.RegisterRequest;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.exception.EmailAlreadyUsedException;
import ru.putevodika.user.exception.UnknownPreferenceCategoryException;
import ru.putevodika.user.repository.UserRepository;
import ru.putevodika.auth.dto.AuthTokenResponse;
import ru.putevodika.auth.dto.LoginRequest;
import ru.putevodika.auth.exception.InvalidCredentialsException;
import ru.putevodika.auth.exception.UserInactiveException;
import ru.putevodika.security.JwtTokenService;

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

    public AuthTokenResponse login(
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

        return jwtTokenService.createAccessToken(user);
    }

    @Transactional
    public UserResponse register(
            RegisterRequest request
    ) {
        String email = normalizeEmail(
                request.getEmail()
        );

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
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

        user.replacePreferredCategories(
                categories
        );

        UserAccount saved =
                userRepository.save(user);

        return toResponse(saved);
    }


    private String normalizeEmail(String email) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
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