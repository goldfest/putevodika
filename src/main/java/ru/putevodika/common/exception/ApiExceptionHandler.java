package ru.putevodika.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.putevodika.place.exception.PlaceNotFoundException;
import ru.putevodika.place.exception.UnknownPlaceCategoryException;
import ru.putevodika.place.exception.InvalidMapBoundsException;
import ru.putevodika.user.exception.EmailAlreadyUsedException;
import ru.putevodika.user.exception.UnknownPreferenceCategoryException;
import ru.putevodika.auth.exception.InvalidCredentialsException;
import ru.putevodika.auth.exception.UserInactiveException;
import ru.putevodika.user.exception.UserNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PlaceNotFoundException.class)
    public ProblemDetail handlePlaceNotFound(
            PlaceNotFoundException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problemDetail.setTitle("Место не найдено");

        return problemDetail;
    }

    @ExceptionHandler(UnknownPlaceCategoryException.class)
    public ProblemDetail handleUnknownCategory(
            UnknownPlaceCategoryException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Некорректная категория места"
        );

        problemDetail.setProperty(
                "unknownCategories",
                exception.getCategoryCodes()
        );

        return problemDetail;
    }

    @ExceptionHandler(InvalidMapBoundsException.class)
    public ProblemDetail handleInvalidMapBounds(
            InvalidMapBoundsException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Некорректные границы карты"
        );

        return problemDetail;
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail handleEmailAlreadyUsed(
            EmailAlreadyUsedException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Email уже используется"
        );

        return problemDetail;
    }

    @ExceptionHandler(
            UnknownPreferenceCategoryException.class
    )
    public ProblemDetail handleUnknownPreferenceCategory(
            UnknownPreferenceCategoryException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Некорректные предпочтения"
        );

        problemDetail.setProperty(
                "unknownCategories",
                exception.getCategoryCodes()
        );

        return problemDetail;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Ошибка авторизации"
        );

        return problemDetail;
    }

    @ExceptionHandler(UserInactiveException.class)
    public ProblemDetail handleInactiveUser(
            UserInactiveException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Учетная запись отключена"
        );

        return problemDetail;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(
            UserNotFoundException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Пользователь не найден"
        );

        return problemDetail;
    }
}