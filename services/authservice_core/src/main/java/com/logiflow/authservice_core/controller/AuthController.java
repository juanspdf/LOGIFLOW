package com.logiflow.authservice_core.controller;

import com.logiflow.authservice_core.dto.request.LoginRequestDto;
import com.logiflow.authservice_core.dto.request.RefreshTokenRequestDto;
import com.logiflow.authservice_core.dto.request.RegisterRequestDto;
import com.logiflow.authservice_core.dto.response.ApiResponseDto;
import com.logiflow.authservice_core.dto.response.AuthResponseDto;
import com.logiflow.authservice_core.service.AuthService;
import com.logiflow.authservice_core.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación
 * Maneja login, registro, renovación de tokens y logout
 */
@RestController
@RequestMapping(Constants.AUTH_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y autorización de usuarios")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario en el sistema
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario en el sistema y devuelve los tokens de autenticación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de registro inválidos o email ya registrado"
            )
    })
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request,
            HttpServletRequest httpRequest
    ) {
        log.info("Solicitud de registro recibida para email: {}", request.getEmail());

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponseDto response = authService.register(request, ipAddress, userAgent);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(response, response.getMessage()));
    }

    /**
     * Inicia sesión con credenciales de usuario
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica a un usuario y devuelve los tokens de acceso"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inicio de sesión exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            )
    })
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest
    ) {
        log.info("Solicitud de login recibida para email: {}", request.getEmail());

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponseDto response = authService.login(request, ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponseDto.success(response, response.getMessage()));
    }

    /**
     * Renueva el access token usando un refresh token válido
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar token de acceso",
            description = "Genera un nuevo access token usando un refresh token válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado"
            )
    })
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request,
            HttpServletRequest httpRequest
    ) {
        log.info("Solicitud de renovación de token recibida");

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponseDto response = authService.refreshToken(
                request.getRefreshToken(),
                ipAddress,
                userAgent
        );

        return ResponseEntity.ok(ApiResponseDto.success(response, response.getMessage()));
    }

    /**
     * Cierra sesión y revoca los tokens del usuario
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Revoca el refresh token del usuario y cierra su sesión"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido"
            )
    })
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        log.info("Solicitud de logout recibida");

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponseDto.success(Constants.SUCCESS_LOGOUT));
    }

    /**
     * Obtiene la dirección IP del cliente desde la petición HTTP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
