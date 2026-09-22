package ru.putevodika.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.putevodika.place.exception.PlaceNotFoundException;
import ru.putevodika.place.exception.UnknownPlaceCategoryException;
import ru.putevodika.place.exception.InvalidMapBoundsException;
import ru.putevodika.user.exception.LoginAlreadyUsedException;
import ru.putevodika.user.exception.UnknownPreferenceCategoryException;
import ru.putevodika.auth.exception.InvalidCredentialsException;
import ru.putevodika.auth.exception.UserInactiveException;
import ru.putevodika.user.exception.UserNotFoundException;
import ru.putevodika.user.exception.IncorrectCurrentPasswordException;
import ru.putevodika.user.exception.SelfAdministrationException;
import ru.putevodika.feature.exception.UnknownFeatureException;
import ru.putevodika.route.exception.DuplicateRoutePlaceException;
import ru.putevodika.route.exception.RouteNotFoundException;
import ru.putevodika.route.exception.UnavailableRoutePlaceException;
import ru.putevodika.routing.exception.RoutingProviderUnavailableException;
import ru.putevodika.routing.exception.WalkingRouteNotFoundException;
import ru.putevodika.auth.exception.InvalidRefreshTokenException;
import ru.putevodika.place.exception.InvalidOsmImportFileException;

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

    @ExceptionHandler(LoginAlreadyUsedException.class)
    public ProblemDetail handleLoginAlreadyUsed(
            LoginAlreadyUsedException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Почта уже используется"
        );

        return problemDetail;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(
            InvalidRefreshTokenException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Ошибка обновления авторизации"
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

    @ExceptionHandler(IncorrectCurrentPasswordException.class)
    public ProblemDetail handleIncorrectCurrentPassword(
            IncorrectCurrentPasswordException exception
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

    @ExceptionHandler(SelfAdministrationException.class)
    public ProblemDetail handleSelfAdministration(
            SelfAdministrationException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Недопустимая административная операция"
        );

        return problemDetail;
    }

    @ExceptionHandler(UnknownFeatureException.class)
    public ProblemDetail handleUnknownFeature(
            UnknownFeatureException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Некорректные числовые характеристики"
        );

        problemDetail.setProperty(
                "unknownFeatures",
                exception.getFeatureCodes()
        );

        return problemDetail;
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ProblemDetail handleRouteNotFound(
            RouteNotFoundException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Маршрут не найден"
        );

        return problemDetail;
    }


    @ExceptionHandler(UnavailableRoutePlaceException.class)
    public ProblemDetail handleUnavailableRoutePlaces(
            UnavailableRoutePlaceException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Некорректные объекты маршрута"
        );

        problemDetail.setProperty(
                "placeIds",
                exception.getPlaceIds()
        );

        return problemDetail;
    }


    @ExceptionHandler(DuplicateRoutePlaceException.class)
    public ProblemDetail handleDuplicateRoutePlace(
            DuplicateRoutePlaceException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Некорректный маршрут"
        );

        return problemDetail;
    }

    @ExceptionHandler(WalkingRouteNotFoundException.class)
    public ProblemDetail handleWalkingRouteNotFound(
            WalkingRouteNotFoundException exception
    ) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Маршрут не построен"
        );

        return problemDetail;
    }


    @ExceptionHandler(RoutingProviderUnavailableException.class)
    public ProblemDetail handleRoutingUnavailable(
            RoutingProviderUnavailableException exception
    ) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Сервис маршрутизации недоступен"
        );

        return problemDetail;
    }

    @ExceptionHandler(InvalidOsmImportFileException.class)
    public ProblemDetail handleInvalidOsmImportFile(
            InvalidOsmImportFileException exception
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problemDetail.setTitle(
                "Ошибка импорта OSM-файла"
        );

        return problemDetail;
    }
}