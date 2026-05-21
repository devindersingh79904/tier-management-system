package com.devinder.loyalty.dto;

import com.devinder.loyalty.util.CorrelationIdContext;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private int status;
    private T data;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    private String correlationId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant timestamp;

    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(status)
                .data(data)
                .correlationId(CorrelationIdContext.getCorrelationId())
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, 200);
    }

    public static <T> ApiResponse<T> error(List<String> errors, String message, int status) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(status)
                .errors(errors != null ? errors : new ArrayList<>())
                .correlationId(CorrelationIdContext.getCorrelationId())
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String error, String message, int status) {
        List<String> errorsList = new ArrayList<>();
        if (error != null) {
            errorsList.add(error);
        }
        return error(errorsList, message, status);
    }
}
