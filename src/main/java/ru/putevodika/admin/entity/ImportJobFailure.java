package ru.putevodika.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportJobFailure {
    @Column(name = "object_id", nullable = false, length = 128)
    private String objectId;

    @Column(name = "error_text", nullable = false, columnDefinition = "text")
    private String errorText;

    public ImportJobFailure(String objectId, String errorText) {
        this.objectId = objectId == null ? "—" : shorten(objectId, 128);
        this.errorText = errorText == null ? "Неизвестная ошибка" : shorten(errorText, 2000);
    }

    private static String shorten(String input, int max) {
        return input.substring(0, Math.min(input.length(), max));
    }
}
