package com.logiflow.authservice_core.dto.request;

import com.logiflow.authservice_core.utils.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de inicio de sesión
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = Constants.VALIDATION_EMAIL_REQUIRED)
    @Email(regexp = ".+@.+\\..+", message = Constants.VALIDATION_EMAIL_REQUIRED)
    private String email;

    @NotBlank(message = Constants.VALIDATION_PASSWORD_REQUIRED)
    @Size(min = 8, message = Constants.VALIDATION_PASSWORD_SIZE)
    private String password;
}