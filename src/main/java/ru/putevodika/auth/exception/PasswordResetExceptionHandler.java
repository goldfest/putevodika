package ru.putevodika.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PasswordResetExceptionHandler {

    @ExceptionHandler(
            InvalidPasswordResetTokenException.class
    )
    public ProblemDetail handleInvalidToken(
            InvalidPasswordResetTokenException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Недействительная ссылка восстановления"
        );

        return problemDetail;
    }

    @ExceptionHandler(
            PasswordResetPasswordsMismatchException.class
    )
    public ProblemDetail handlePasswordsMismatch(
            PasswordResetPasswordsMismatchException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Не удалось изменить пароль"
        );

        return problemDetail;
    }

    @ExceptionHandler(
            PasswordResetRateLimitException.class
    )
    public ProblemDetail handleRateLimit(
            PasswordResetRateLimitException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Слишком много запросов"
        );

        return problemDetail;
    }
}
