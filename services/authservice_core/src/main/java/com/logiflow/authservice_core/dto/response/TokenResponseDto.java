package com.logiflow.authservice_core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDto {

    private String token;

    private String tokenType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    private String message;
}
