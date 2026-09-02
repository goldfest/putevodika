package ru.putevodika.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.place.entity.Category;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.repository.UserRepository;
import ru.putevodika.place.repository.CategoryRepository;
import ru.putevodika.user.dto.UpdatePreferencesRequest;
import ru.putevodika.user.exception.UnknownPreferenceCategoryException;
import ru.putevodika.user.dto.UpdateProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.putevodika.user.dto.ChangePasswordRequest;
import ru.putevodika.user.exception.IncorrectCurrentPasswordException;
import ru.putevodika.auth.service.RefreshTokenService;

import java.util.HashSet;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;


    public UserResponse getById(Long id) {
        return toResponse(
                getUser(id)
        );
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

    @Transactional
    public UserResponse updatePreferences(
            Long userId,
            UpdatePreferencesRequest request
    ) {
        UserAccount user = getUser(userId);

        Set<String> requestedCodes =
                request.getCategories();

        Set<Category> categories =
                requestedCodes.isEmpty()
                        ? Set.of()
                        : categoryRepository
                        .findAllByCodeInAndActiveTrue(
                                requestedCodes
                        );

        validateCategories(
                requestedCodes,
                categories
        );

        user.replacePreferredCategories(
                categories
        );

        return toResponse(user);
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

    @Transactional
    public UserResponse updateProfile(
            Long userId,
            UpdateProfileRequest request
    ) {
        UserAccount user = getUser(userId);

        if (request.getDisplayName() != null) {
            user.changeDisplayName(
                    request.getDisplayName().trim()
            );
        }

        if (request.getAvatarUrl() != null) {
            String avatarUrl =
                    request.getAvatarUrl().trim();

            user.changeAvatarUrl(
                    avatarUrl.isEmpty()
                            ? null
                            : avatarUrl
            );
        }

        return toResponse(user);
    }

    private UserAccount getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }

    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {
        UserAccount user = getUser(userId);

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash()
        )) {
            throw new IncorrectCurrentPasswordException();
        }

        String newPasswordHash =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.changePasswordHash(
                newPasswordHash
        );

        refreshTokenService.revokeAllForUser(
                userId
        );
    }
}