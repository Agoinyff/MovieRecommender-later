package com.rcd.movierecommender.backend.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiErrorResponse {

    private final int code;
    private final String message;
    private final String path;
    private final Instant timestamp;
    private final List<String> details;

    private ApiErrorResponse(int code, String message, String path, Instant timestamp, List<String> details) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
        this.details = details == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(details));
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String message, String path, List<String> details) {
        String fallbackMessage = message == null || message.trim().isEmpty() ? errorCode.getMessage() : message;
        return new ApiErrorResponse(errorCode.getCode(), fallbackMessage, path, Instant.now(), details);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<String> getDetails() {
        return details;
    }
}
