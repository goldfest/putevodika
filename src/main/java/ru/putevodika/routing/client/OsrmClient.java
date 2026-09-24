package ru.putevodika.routing.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.putevodika.routing.client.dto.OsrmRouteApiResponse;
import ru.putevodika.routing.exception.RoutingProviderUnavailableException;
import ru.putevodika.routing.exception.WalkingRouteNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import ru.putevodika.routing.client.dto.OsrmTableApiResponse;

@Component
public class OsrmClient {

    private final RestClient restClient;


    public OsrmClient(
            @Qualifier("osrmRestClient")
            RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public OsrmRouteResult buildWalkingRoute(
            double startLatitude,
            double startLongitude,
            double finishLatitude,
            double finishLongitude
    ) {
        return buildWalkingRoute(
                List.of(
                        new RoutingPoint(
                                startLatitude,
                                startLongitude
                        ),
                        new RoutingPoint(
                                finishLatitude,
                                finishLongitude
                        )
                )
        );
    }

    public OsrmRouteResult buildWalkingRoute(
            List<RoutingPoint> points
    ) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException(
                    "Для построения маршрута нужны минимум две точки"
            );
        }

        String coordinates =
                points.stream()
                        .map(point ->
                                point.longitude()
                                        + ","
                                        + point.latitude()
                        )
                        .collect(
                                Collectors.joining(";")
                        );

        try {
            OsrmRouteApiResponse response =
                    restClient
                            .get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path("/route/v1/foot/")
                                            .path(coordinates)
                                            .queryParam(
                                                    "overview",
                                                    "full"
                                            )
                                            .queryParam(
                                                    "geometries",
                                                    "geojson"
                                            )
                                            .build()
                            )
                            .retrieve()
                            .body(
                                    OsrmRouteApiResponse.class
                            );

            if (response == null) {
                throw new RoutingProviderUnavailableException(
                        "OSRM вернул пустой ответ"
                );
            }

            if (!"Ok".equals(response.code())
                    || response.routes() == null
                    || response.routes().isEmpty()) {

                throw new WalkingRouteNotFoundException(
                        buildRoutingErrorMessage(
                                response
                        )
                );
            }

            OsrmRouteApiResponse.Route route =
                    response.routes().getFirst();

            if (route.geometry() == null) {
                throw new RoutingProviderUnavailableException(
                        "OSRM не вернул геометрию маршрута"
                );
            }

            return new OsrmRouteResult(
                    route.distance(),
                    route.duration(),
                    route.geometry().type(),
                    route.geometry().coordinates()
            );

        } catch (WalkingRouteNotFoundException |
                 RoutingProviderUnavailableException exception) {

            throw exception;

        } catch (RestClientResponseException exception) {

            handleHttpError(exception);
            throw exception;

        } catch (RestClientException exception) {

            throw new RoutingProviderUnavailableException(
                    "Не удалось подключиться к OSRM",
                    exception
            );
        }
    }


    private void handleHttpError(
            RestClientResponseException exception
    ) {

        HttpStatusCode status =
                exception.getStatusCode();

        if (status.is4xxClientError()) {

            throw new WalkingRouteNotFoundException(
                    "OSRM не смог построить маршрут"
            );
        }

        throw new RoutingProviderUnavailableException(
                "Ошибка сервиса OSRM",
                exception
        );
    }


    private String buildRoutingErrorMessage(
            OsrmRouteApiResponse response
    ) {

        if (response.message() != null
                && !response.message().isBlank()) {

            return "Не удалось построить маршрут: "
                    + response.message();
        }

        return "Не удалось построить пешеходный маршрут";
    }

    public OsrmMatrixResult buildWalkingMatrix(
            List<RoutingPoint> points
    ) {

        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException(
                    "Для построения матрицы нужны минимум две точки"
            );
        }

        String coordinates = points.stream()
                .map(point ->
                        point.longitude()
                                + ","
                                + point.latitude()
                )
                .collect(
                        Collectors.joining(";")
                );

        try {

            OsrmTableApiResponse response =
                    restClient
                            .get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/table/v1/foot/"
                                            )
                                            .path(coordinates)
                                            .queryParam(
                                                    "annotations",
                                                    "duration,distance"
                                            )
                                            .build()
                            )
                            .retrieve()
                            .body(
                                    OsrmTableApiResponse.class
                            );

            if (response == null) {
                throw new RoutingProviderUnavailableException(
                        "OSRM вернул пустой ответ матрицы"
                );
            }

            if (!"Ok".equals(response.code())) {
                throw new WalkingRouteNotFoundException(
                        response.message() != null
                                ? response.message()
                                : "OSRM не смог построить матрицу маршрутов"
                );
            }

            if (response.durations() == null
                    || response.distances() == null) {

                throw new RoutingProviderUnavailableException(
                        "OSRM не вернул матрицу расстояний или времени"
                );
            }

            return new OsrmMatrixResult(
                    response.durations(),
                    response.distances()
            );

        } catch (WalkingRouteNotFoundException |
                 RoutingProviderUnavailableException exception) {

            throw exception;

        } catch (RestClientResponseException exception) {

            handleHttpError(exception);

            throw exception;

        } catch (RestClientException exception) {

            throw new RoutingProviderUnavailableException(
                    "Не удалось подключиться к OSRM",
                    exception
            );
        }
    }
}