package com.logiflow.authservice_core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para respuestas de error
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDto {

    private int status;

    private String error;

    private String message;

    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Para errores de validación
    private Map<String, String> validationErrors;

    // Para múltiples mensajes de error
    private List<String> errors;

    /**
     * Crea una respuesta de error simple
     */
    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return ErrorResponseDto.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Crea una respuesta de error con validaciones
     */
    public static ErrorResponseDto withValidationErrors(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors) {
        return ErrorResponseDto.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
