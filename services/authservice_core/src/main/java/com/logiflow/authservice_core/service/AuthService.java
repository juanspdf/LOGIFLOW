package com.logiflow.authservice_core.service;

import com.logiflow.authservice_core.dto.mapper.UsuarioMapper;
import com.logiflow.authservice_core.dto.request.LoginRequestDto;
import com.logiflow.authservice_core.dto.request.RegisterRequestDto;
import com.logiflow.authservice_core.dto.response.AuthResponseDto;
import com.logiflow.authservice_core.dto.response.UserResponseDto;
import com.logiflow.authservice_core.exception.BadRequestException;
import com.logiflow.authservice_core.exception.ResourceNotFoundException;
import com.logiflow.authservice_core.exception.UnauthorizedException;
import com.logiflow.authservice_core.model.entity.RefreshToken;
import com.logiflow.authservice_core.model.entity.Rol;
import com.logiflow.authservice_core.model.entity.Usuario;
import com.logiflow.authservice_core.model.enums.FleetType;
import com.logiflow.authservice_core.model.enums.UserStatus;
import com.logiflow.authservice_core.repository.RolRepository;
import com.logiflow.authservice_core.repository.UsuarioRepository;
import com.logiflow.authservice_core.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación y autorización
 * Maneja el registro, login y gestión de tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioMapper usuarioMapper;

    /**
     * Registra un nuevo usuario en el sistema
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request, String ipAddress, String userAgent) {
        log.info("Iniciando registro de usuario con email: {}", request.getEmail());

        // Validar que el email no exista
        if (usuarioRepository.existsByEmailAndNotDeleted(request.getEmail())) {
            throw new BadRequestException(Constants.ERROR_EMAIL_ALREADY_EXISTS);
        }

        // Obtener el rol
        Rol rol = rolRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));

        // Validar fleet type para repartidores
        if (request.getRoleName().isOperational()) {
            if (request.getFleetType() == null || request.getFleetType() == FleetType.NONE) {
                throw new BadRequestException("El repartidor debe tener un tipo de flota asignado");
            }
        }

        // Mapear DTO a entidad
        Usuario usuario = usuarioMapper.toUsuario(request);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setStatus(UserStatus.ACTIVE);

        // Si no es repartidor, asegurar que fleet type sea NONE
        if (!request.getRoleName().isOperational()) {
            usuario.setFleetType(FleetType.NONE);
        }

        // Guardar usuario
        usuario = usuarioRepository.save(usuario);

        log.info("Usuario registrado exitosamente: {}", usuario.getEmail());

        // Generar tokens
        String accessToken = jwtService.generateToken(usuario);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario, ipAddress, userAgent);

        // Construir respuesta
        UserResponseDto userResponse = usuarioMapper.toUserResponse(usuario);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType(Constants.TOKEN_TYPE)
                .expiresAt(jwtService.getExpirationDate())
                .user(userResponse)
                .message(Constants.SUCCESS_USER_REGISTERED)
                .build();
    }

    /**
     * Autentica un usuario y genera tokens
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto request, String ipAddress, String userAgent) {
        log.info("Intento de login para usuario: {}", request.getEmail());

        Usuario usuario = usuarioRepository.findByEmailAndNotDeleted(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(Constants.ERROR_INVALID_CREDENTIALS));

        try {
            // Autenticar con Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Resetear intentos fallidos
            if (usuario.getFailedLoginAttempts() > 0) {
                usuario.resetFailedAttempts();
            }

            // Actualizar último login
            usuario.updateLastLogin();
            usuarioRepository.save(usuario);

            // Generar tokens
            String accessToken = jwtService.generateToken(usuario);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario, ipAddress, userAgent);

            log.info("Login exitoso para usuario: {}", usuario.getEmail());

            // Construir respuesta
            UserResponseDto userResponse = usuarioMapper.toUserResponse(usuario);

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType(Constants.TOKEN_TYPE)
                    .expiresAt(jwtService.getExpirationDate())
                    .user(userResponse)
                    .message(Constants.SUCCESS_LOGIN)
                    .build();

        } catch (AuthenticationException e) {
            // Incrementar intentos fallidos
            usuario.incrementFailedAttempts();
            usuarioRepository.save(usuario);

            log.warn("Login fallido para usuario: {}. Intentos: {}",
                    usuario.getEmail(), usuario.getFailedLoginAttempts());

            throw new UnauthorizedException(Constants.ERROR_INVALID_CREDENTIALS);
        }
    }

    /**
     * Renueva el access token usando un refresh token
     */
    @Transactional
    public AuthResponseDto refreshToken(String refreshTokenStr, String ipAddress, String userAgent) {
        log.info("Renovando token...");

        // Validar refresh token
        RefreshToken refreshToken = refreshTokenService.validateAndGetToken(refreshTokenStr);
        Usuario usuario = refreshToken.getUsuario();

        // Generar nuevo access token
        String accessToken = jwtService.generateToken(usuario);

        // Opcionalmente, generar nuevo refresh token (rotación)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(usuario, ipAddress, userAgent);

        // Revocar el refresh token anterior
        refreshToken.revoke();

        log.info("Token renovado exitosamente para usuario: {}", usuario.getEmail());

        // Construir respuesta
        UserResponseDto userResponse = usuarioMapper.toUserResponse(usuario);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType(Constants.TOKEN_TYPE)
                .expiresAt(jwtService.getExpirationDate())
                .user(userResponse)
                .message(Constants.SUCCESS_TOKEN_REFRESHED)
                .build();
    }

    /**
     * Cierra sesión revocando los tokens del usuario
     */
    @Transactional
    public void logout(String refreshTokenStr) {
        log.info("Cerrando sesión...");

        try {
            refreshTokenService.revokeToken(refreshTokenStr);
            log.info("Sesión cerrada exitosamente");
        } catch (Exception e) {
            log.error("Error al cerrar sesión: {}", e.getMessage());
            throw new UnauthorizedException("Error al cerrar sesión");
        }
    }
}