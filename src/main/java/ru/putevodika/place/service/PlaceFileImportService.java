package ru.putevodika.place.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.putevodika.place.dto.OsmPlaceBatchImportResponse;
import ru.putevodika.place.dto.OsmPlaceImportRequest;
import ru.putevodika.place.exception.InvalidOsmImportFileException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceFileImportService {

    private final JsonMapper jsonMapper;

    private final Validator validator;

    private final PlaceBatchImportService
            batchImportService;


    public OsmPlaceBatchImportResponse importFile(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOsmImportFileException(
                    "Файл импорта пуст"
            );
        }

        ObjectReader requestReader =
                jsonMapper
                        .readerFor(
                                OsmPlaceImportRequest.class
                        )
                        .without(
                                DeserializationFeature
                                        .FAIL_ON_TRAILING_TOKENS
                        );

        int total = 0;
        int created = 0;
        int updated = 0;

        List<OsmPlaceBatchImportResponse.Failure> failures =
                new ArrayList<>();

        try (
                InputStream inputStream =
                        file.getInputStream();

                JsonParser parser =
                        jsonMapper.createParser(
                                inputStream
                        )
        ) {
            JsonToken rootToken =
                    parser.nextToken();

            if (rootToken != JsonToken.START_ARRAY) {
                throw new InvalidOsmImportFileException(
                        "Корень JSON-файла должен быть массивом"
                );
            }

            while (
                    parser.nextToken()
                            != JsonToken.END_ARRAY
            ) {
                total++;

                OsmPlaceImportRequest request;

                try {
                    request =
                            requestReader.readValue(
                                    parser
                            );

                } catch (JacksonException exception) {

                    throw new InvalidOsmImportFileException(
                            "Некорректный JSON "
                                    + "в объекте #"
                                    + total
                                    + ": "
                                    + exception.getMessage(),
                            exception
                    );
                }

                if (request == null) {

                    failures.add(
                            failure(
                                    "#" + total,
                                    "Объект не может быть null"
                            )
                    );

                    continue;
                }

                String validationError =
                        validate(request);

                if (validationError != null) {

                    failures.add(
                            failure(
                                    failureId(
                                            request,
                                            total
                                    ),
                                    validationError
                            )
                    );

                    continue;
                }

                try {
                    PlaceBatchImportService.ImportStatus status =
                            batchImportService
                                    .importWithStatus(
                                            request
                                    );

                    if (
                            status
                                    == PlaceBatchImportService
                                    .ImportStatus.CREATED
                    ) {
                        created++;

                    } else {
                        updated++;
                    }

                } catch (Exception exception) {

                    failures.add(
                            failure(
                                    failureId(
                                            request,
                                            total
                                    ),
                                    exception.getMessage()
                            )
                    );
                }
            }

            if (parser.nextToken() != null) {
                throw new InvalidOsmImportFileException(
                        "После JSON-массива обнаружены лишние данные"
                );
            }

        } catch (IOException exception) {

            throw new InvalidOsmImportFileException(
                    "Не удалось прочитать файл импорта",
                    exception
            );
        }

        return OsmPlaceBatchImportResponse
                .builder()
                .total(total)
                .created(created)
                .updated(updated)
                .failed(failures.size())
                .failures(failures)
                .build();
    }


    private String validate(
            OsmPlaceImportRequest request
    ) {
        Set<ConstraintViolation<OsmPlaceImportRequest>>
                violations =
                validator.validate(request);

        if (violations.isEmpty()) {
            return null;
        }

        return violations.stream()
                .map(violation ->
                        violation
                                .getPropertyPath()
                                + ": "
                                + violation.getMessage()
                )
                .sorted()
                .collect(
                        Collectors.joining("; ")
                );
    }


    private String failureId(
            OsmPlaceImportRequest request,
            int index
    ) {
        if (
                request.getId() == null
                        || request
                        .getId()
                        .isBlank()
        ) {
            return "#" + index;
        }

        return request.getId();
    }


    private OsmPlaceBatchImportResponse.Failure
    failure(
            String id,
            String error
    ) {
        return OsmPlaceBatchImportResponse
                .Failure
                .builder()
                .id(id)
                .error(
                        error == null
                                ? "Неизвестная ошибка импорта"
                                : error
                )
                .build();
    }
}