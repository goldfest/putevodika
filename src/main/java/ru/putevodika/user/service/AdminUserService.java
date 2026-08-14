package ru.putevodika.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevodika.common.dto.PageResponse;
import ru.putevodika.place.entity.Category;
import ru.putevodika.user.dto.ChangeUserRoleRequest;
import ru.putevodika.user.dto.UserListItemResponse;
import ru.putevodika.user.dto.UserResponse;
import ru.putevodika.user.entity.UserAccount;
import ru.putevodika.user.entity.UserRole;
import ru.putevodika.user.exception.SelfAdministrationException;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.repository.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.putevodika.user.repository.specification.UserSpecifications.containsSearch;
import static ru.putevodika.user.repository.specification.UserSpecifications.hasActive;
import static ru.putevodika.user.repository.specification.UserSpecifications.hasRole;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;


    public PageResponse<UserListItemResponse> findAll(
            int page,
            int size,
            Boolean active,
            UserRole role,
            String search
    ) {
        Specification<UserAccount> specification =
                Specification.allOf(
                        hasActive(active),
                        hasRole(role),
                        containsSearch(search)
                );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "updatedAt"
                        )
                );

        Page<UserListItemResponse> result =
                userRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(this::toListItemResponse);

        return PageResponse.from(result);
    }


    public UserResponse getById(Long id) {
        return toResponse(
                getUser(id)
        );
    }


    @Transactional
    public UserResponse changeRole(
            Long administratorId,
            Long userId,
            ChangeUserRoleRequest request
    ) {
        validateNotSelf(
                administratorId,
                userId
        );

        UserAccount user = getUser(userId);

        user.changeRole(
                request.getRole()
        );

        return toResponse(user);
    }


    @Transactional
    public void deactivate(
            Long administratorId,
            Long userId
    ) {
        validateNotSelf(
                administratorId,
                userId
        );

        UserAccount user = getUser(userId);

        user.deactivate();
    }


    @Transactional
    public UserResponse activate(Long userId) {
        UserAccount user = getUser(userId);

        user.activate();

        return toResponse(user);
    }


    private UserAccount getUser(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }


    private void validateNotSelf(
            Long administratorId,
            Long targetUserId
    ) {
        if (administratorId.equals(targetUserId)) {
            throw new SelfAdministrationException();
        }
    }


    private UserListItemResponse toListItemResponse(
            UserAccount user
    ) {
        return UserListItemResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
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