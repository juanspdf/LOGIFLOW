# 📚 Arquitectura Técnica

## 🏗️ Diagrama de Arquitectura del Sistema

```
┌────────────────────────────────────────────────────────────┐
│                    CLIENTE (Postman/Browser)                │
│                   HTTP/HTTPS Requests                       │
└─────────────────────────┬──────────────────────────────────┘
                          │
                          ▼
        ┌─────────────────────────────────────┐
        │      KONG API GATEWAY :8000         │
        │  ┌───────────────────────────────┐  │
        │  │  • JWT Validation (HS512)     │  │
        │  │  • Rate Limiting (100/min)    │  │
        │  │  • Request Routing            │  │
        │  │  • File Logging               │  │
        │  │  • CORS Headers               │  │
        │  └───────────────────────────────┘  │
        └───┬─────────┬─────────┬─────────┬───┘
            │         │         │         │
    ┌───────▼───┐ ┌──▼────┐ ┌──▼────┐ ┌──▼─────┐
    │   Auth    │ │Pedido │ │ Fleet │ │Billing │
    │  Service  │ │Service│ │Service│ │Service │
    │   :8081   │ │ :8082 │ │ :8083 │ │ :8084  │
    │           │ │       │ │       │ │        │
    │ • JWT Gen │ │• CRUD │ │•Vehíc.│ │• Fact. │
    │ • BCrypt  │ │•Valid.│ │•Repart│ │• Calc. │
    │ • Refresh │ │•Estados│ │•Asign │ │• Tarif │
    └─────┬─────┘ └───┬───┘ └───┬───┘ └───┬────┘
          │           │         │         │
          └───────────┴─────────┴─────────┘
                      │
                      ▼
        ┌─────────────────────────────────┐
        │      PostgreSQL 16 :5432        │
        │  ┌───────────────────────────┐  │
        │  │ • logiflow_auth           │  │
        │  │ • logiflow_pedidos        │  │
        │  │ • logiflow_fleet          │  │
        │  │ • logiflow_billing        │  │
        │  │ • kong (metadata)         │  │
        │  └───────────────────────────┘  │
        └─────────────────────────────────┘
```

**Componentes:**
- **Kong Gateway**: Proxy centralizado con seguridad JWT y rate limiting
- **Auth Service**: Autenticación JWT + BCrypt + gestión usuarios/roles
- **Pedido Service**: CRUD pedidos + validaciones + estados (RECIBIDO → ENTREGADO)
- **Fleet Service**: Gestión flota (vehículos + repartidores + asignación)
- **Billing Service**: Facturación + cálculo tarifas por tipo de entrega
- **PostgreSQL**: 5 bases de datos independientes

---

## Estructura del Proyecto

```
LOGIFLOW/
├── services/
│   ├── authservice_core/          # Auth + JWT + BCrypt
│   │   ├── src/main/java/com/logiflow/authservice_core/
│   │   │   ├── config/
│   │   │   │   ├── PasswordEncoderConfig.java
│   │   │   │   ├── DataInitializer.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   └── UsuarioController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── RefreshTokenService.java
│   │   │   ├── model/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Rol.java
│   │   │   │   └── RefreshToken.java
│   │   │   └── repository/
│   │   │       ├── UsuarioRepository.java
│   │   │       └── RolRepository.java
│   │   └── src/main/resources/
│   │       ├── application.yaml
│   │       └── application-docker.yaml
│   │
│   ├── pedido-service/            # CRUD Pedidos + Estados
│   │   ├── src/main/java/com/logiflow/pedido/
│   │   │   ├── controller/PedidoController.java
│   │   │   ├── service/PedidoService.java
│   │   │   ├── model/Pedido.java
│   │   │   ├── dto/
│   │   │   │   ├── CrearPedidoRequest.java
│   │   │   │   └── PedidoResponse.java
│   │   │   └── repository/PedidoRepository.java
│   │   └── src/main/resources/
│   │       └── application.yaml
│   │
│   ├── fleet-service/             # Vehículos + Repartidores
│   │   ├── src/main/java/espe/edu/ec/fleet_service/
│   │   │   ├── controller/FleetController.java
│   │   │   ├── service/FleetService.java
│   │   │   └── model/Vehiculo.java
│   │   └── src/main/resources/
│   │       └── application.yaml
│   │
│   └── billing-service/           # Facturación + Cálculo
│       ├── src/main/java/espe/edu/ec/billing_service/
│       │   ├── controller/BillingController.java
│       │   ├── service/BillingService.java
│       │   └── model/Billing.java
│       └── src/main/resources/
│           └── application.yaml
│
├── database/
│   └── migrations/                # Flyway SQL scripts (pendiente)
│
├── scripts/
│   └── configure-kong.ps1         # Configuración Kong automática
│
├── docker-compose.yml             # Orquestación 7 containers
├── .env                          # JWT_SECRET (no versionado)
├── .gitignore
└── LOGIFLOW-Fase1.postman_collection.json
```

## 🔐 Seguridad y Configuración

### Spring Security Deshabilitado
Kong maneja autenticación upstream. Spring Boot services sin filtros Security:

**application.yaml**:
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
      - org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
```

### Beans Esenciales (PasswordEncoderConfig.java)
```java
@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
}
```

### Variables de Entorno (.env)
```env
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
DB_USER=postgres
DB_PASSWORD=admin123
DB_HOST=postgres
```

## 🌐 Configuración Kong Gateway

### Services
```bash
# Auth Service
curl -X POST http://localhost:8001/services \
  -d name=auth-service \
  -d url=http://auth-service:8081/api/v1/auth

# Pedido Service
curl -X POST http://localhost:8001/services \
  -d name=pedido-service \
  -d url=http://pedido-service:8082/pedidos

# Fleet Service
curl -X POST http://localhost:8001/services \
  -d name=fleet-service \
  -d url=http://fleet-service:8083/fleet

# Billing Service
curl -X POST http://localhost:8001/services \
  -d name=billing-service \
  -d url=http://billing-service:8084/billing
```

### Routes (strip_path=true)
```bash
# Auth Route (sin JWT)
curl -X POST http://localhost:8001/routes \
  -d name=auth-route \
  -d paths[]=/api/auth \
  -d service.name=auth-service \
  -d strip_path=true

# Pedido Route (con JWT + Rate Limiting)
curl -X POST http://localhost:8001/routes \
  -d name=pedido-route \
  -d paths[]=/api/pedidos \
  -d service.name=pedido-service \
  -d strip_path=true

# Fleet Route (con JWT)
curl -X POST http://localhost:8001/routes \
  -d name=fleet-route \
  -d paths[]=/api/fleet \
  -d service.name=fleet-service \
  -d strip_path=true

# Billing Route (con JWT)
curl -X POST http://localhost:8001/routes \
  -d name=billing-route \
  -d paths[]=/api/billing \
  -d service.name=billing-service \
  -d strip_path=true
```

### JWT Plugin (HS512)
```bash
# Consumer
curl -X POST http://localhost:8001/consumers \
  -d username=logiflow-jwt-validator

# JWT Credential
curl -X POST http://localhost:8001/consumers/logiflow-jwt-validator/jwt \
  -d key=logiflow-auth-service \
  -d secret=$JWT_SECRET \
  -d algorithm=HS512

# Aplicar JWT a routes protegidas
for route in pedido-route fleet-route billing-route; do
  curl -X POST http://localhost:8001/routes/$route/plugins \
    -d name=jwt \
    -d config.key_claim_name=iss \
    -d config.claims_to_verify[]=exp \
    -d config.algorithm=HS512
done
```

### Rate Limiting (100 req/min)
```bash
curl -X POST http://localhost:8001/services/pedido-service/plugins \
  -d name=rate-limiting \
  -d config.minute=100 \
  -d config.policy=local
```

## 🗄️ Base de Datos PostgreSQL

### Esquemas por Servicio
- **logiflow_auth**: usuarios, roles, refresh_tokens
- **logiflow_pedido**: pedidos
- **logiflow_fleet**: vehiculos, repartidores
- **logiflow_billing**: facturas

### Conexión Docker
```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_MULTIPLE_DATABASES: logiflow_auth,logiflow_pedido,logiflow_fleet,logiflow_billing
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

### Connection Strings
```yaml
# application-docker.yaml (cada servicio)
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:postgres}:5432/logiflow_{service}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
```

## 🔄 Flujo de Autenticación JWT

### Diagrama Completo de Autenticación

```
┌─────────────────────────────────────────────────────────────┐
│                    FASE 1: REGISTRO                         │
└─────────────────────────────────────────────────────────────┘

1. Cliente → POST /api/auth/register
   Body: {
     "email": "user@example.com",
     "password": "pass123",
     "nombre": "Juan",
     "apellido": "Pérez",
     "rol": "CLIENTE"
   }
   │
   ▼
2. Kong Gateway (ruta sin JWT) → pasa directo
   │
   ▼
3. Auth Service
   ├─ Valida: email único, password >= 8 chars
   ├─ BCrypt.hash(password) → $2a$10$...
   ├─ Guarda Usuario + Rol en BD
   │
   ▼
4. Response HTTP 201 Created
   Body: {
     "id": "uuid-123",
     "email": "user@example.com",
     "rol": "CLIENTE"
   }


┌─────────────────────────────────────────────────────────────┐
│                    FASE 2: LOGIN                            │
└─────────────────────────────────────────────────────────────┘

5. Cliente → POST /api/auth/login
   Body: {
     "email": "user@example.com",
     "password": "pass123"
   }
   │
   ▼
6. Kong Gateway (ruta sin JWT) → pasa directo
   │
   ▼
7. Auth Service
   ├─ Busca usuario por email
   ├─ BCrypt.matches(password, hash_almacenado)
   ├─ Si válido:
   │  ├─ JwtService.generateToken()
   │  │  ├─ Payload: { iss, sub, exp, roles }
   │  │  ├─ Algorithm: HS512
   │  │  └─ Secret: $JWT_SECRET
   │  │
   │  └─ RefreshTokenService.create()
   │     ├─ Token: UUID random
   │     ├─ Expires: now() + 7 días
   │     └─ Guarda en refresh_tokens table
   │
   ▼
8. Response HTTP 200 OK
   Body: {
     "access_token": "eyJhbGciOiJIUzUxMiJ9...",
     "refresh_token": "uuid-refresh-token",
     "token_type": "Bearer",
     "expires_in": 3600
   }


┌─────────────────────────────────────────────────────────────┐
│                FASE 3: ACCESO A RECURSOS                    │
└─────────────────────────────────────────────────────────────┘

9. Cliente → GET /api/pedidos/{id}
   Headers: {
     "Authorization": "Bearer eyJhbGciOiJIUzUxMiJ9..."
   }
   │
   ▼
10. Kong Gateway
    ├─ JWT Plugin activo en ruta
    ├─ Extrae token del header
    ├─ Valida firma HMAC-SHA512 con $JWT_SECRET
    ├─ Verifica claim "exp" (no expirado)
    ├─ Verifica claim "iss" = "logiflow-auth-service"
    │
    ├─ ✅ JWT VÁLIDO
    │  └─ Proxy request a pedido-service:8082
    │
    └─ ❌ JWT INVÁLIDO
       └─ Response HTTP 401 Unauthorized
          Body: { "message": "Unauthorized" }
   │
   ▼
11. Pedido Service
    ├─ Recibe request sin validación adicional
    ├─ Consulta pedido en BD (logiflow_pedidos)
    │
    ▼
12. Response HTTP 200 OK
    Body: {
      "id": "uuid-pedido",
      "estado": "RECIBIDO",
      "tipoEntrega": "URBANA"
    }


┌─────────────────────────────────────────────────────────────┐
│              FASE 4: RENOVACIÓN DE TOKEN                    │
└─────────────────────────────────────────────────────────────┘

13. Cliente → POST /api/auth/token/refresh
    Body: {
      "refresh_token": "uuid-refresh-token"
    }
    │
    ▼
14. Auth Service
    ├─ Busca refresh_token en BD
    ├─ Verifica no expirado (< 7 días)
    ├─ Si válido:
    │  └─ Genera nuevo access_token
    │
    ▼
15. Response HTTP 200 OK
    Body: {
      "access_token": "eyJhbGciOiJIUzUxMiJ9...",
      "expires_in": 3600
    }
```

### Estructura del JWT (Decoded)

**Header:**
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "iss": "logiflow-auth-service",
  "sub": "user@example.com",
  "iat": 1734437700,
  "exp": 1734441300,
  "roles": ["CLIENTE"]
}
```

**Signature:**
```
HMACSHA512(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  JWT_SECRET
)
```

---

## 📊 Estados y Enums

### EstadoPedido (PedidoService)
- `RECIBIDO` - Estado inicial al crear pedido
- `EN_PROCESO` - Asignado a repartidor
- `EN_RUTA` - Repartidor recogió paquete
- `ENTREGADO` - Completado exitosamente
- `CANCELADO` - Cancelación lógica

### TipoEntrega (PedidoService)
- `URBANA` - Mismo municipio
- `INTERMUNICIPAL` - Entre ciudades
- `NACIONAL` - Cobertura país

### FleetType (FleetService)
- `AUTO` - Vehículo tipo auto
- `MOTO` - Motocicleta
- `BICICLETA` - Bicicleta
- `VAN` - Van de carga
- `NONE` - Sin vehículo asignado

### UserStatus (AuthService)
- `ACTIVE` - Usuario activo
- `INACTIVE` - Usuario inactivo
- `SUSPENDED` - Usuario suspendido

### RoleName (AuthService)
- `CLIENTE` - Cliente final
- `REPARTIDOR` - Delivery person
- `SUPERVISOR` - Supervisor operaciones
- `ADMIN` - Administrador sistema

---

## 🎯 Justificación de Decisiones Técnicas

### 1. Kong Gateway vs Spring Cloud Gateway

**Decisión:** Kong Gateway 3.5 (C + Nginx)

**Justificación:**
- ✅ **Rendimiento superior**: C/Nginx vs JVM reduce latencia ~40%
- ✅ **Plugins nativos**: JWT, rate limiting, logging sin código Java
- ✅ **Estabilidad**: Usado por Netflix, Samsung, Goldman Sachs
- ✅ **Menor huella de memoria**: ~50MB vs ~200MB Spring Cloud Gateway
- ✅ **Configuración declarativa**: `kong-declarative.yml` para IaC
- ❌ **Contra**: Más complejo de configurar inicialmente

**Alternativas evaluadas:**
- Spring Cloud Gateway (descartado: overhead JVM)
- Nginx + Lua (descartado: mantenimiento manual)

---

### 2. Transacciones Locales (No Saga Pattern)

**Decisión:** `@Transactional` local en cada microservicio

**Justificación:**
- ✅ **Simplicidad Fase 1**: No hay operaciones multi-servicio atómicas
- ✅ **ACID garantizado**: PostgreSQL maneja transacciones locales
- ✅ **Rollback automático**: Spring gestiona excepciones
- ✅ **Preparado para Fase 2**: Arquitectura permite migrar a Saga

**Ejemplo:**
```java
@Transactional
public PedidoResponse crearPedido(CrearPedidoRequest request) {
    // Si falla, rollback automático
    Pedido pedido = new Pedido();
    pedido.setEstado(EstadoPedido.RECIBIDO);
    return pedidoRepository.save(pedido);
}
```

**Cuando migrar a Saga:**
- Fase 2: Crear pedido + asignar repartidor + generar factura (multi-servicio)
- Usar Orchestration Saga con compensating transactions

---

### 3. PostgreSQL Multi-Database

**Decisión:** 5 bases de datos en 1 instancia PostgreSQL

**Justificación:**
- ✅ **Aislamiento lógico**: Cada servicio tiene su schema independiente
- ✅ **Integridad referencial**: Foreign keys funcionan dentro de cada DB
- ✅ **Costo-efectivo**: 1 instancia vs 5 instancias
- ✅ **Backup simplificado**: `pg_dump` por database
- ✅ **Migrations independientes**: Flyway/Liquibase por servicio
- ❌ **Contra**: No es multi-tenancy completo (mismo server)

**Alternativas evaluadas:**
- 1 DB + schemas: Descartado (acoplamiento)
- 5 instancias PostgreSQL: Descartado (overhead recursos)

---

### 4. SpringDoc OpenAPI 2.7.0

**Decisión:** Estandarizar versión 2.7.0 en todos los servicios

**Justificación:**
- ✅ **Compatibilidad Spring Boot 3.4.0**: Versión certificada
- ✅ **Fix de bug crítico**: 2.3.0 causaba HTTP 500 en `/api-docs`
- ✅ **Swagger UI integrado**: Sin configuración adicional
- ✅ **Generación automática**: Contratos desde anotaciones `@Schema`
- ✅ **OpenAPI 3.0 spec**: Estándar de la industria

**Problema resuelto:**
```
Error: NoSuchMethodError: ControllerAdviceBean.<init>
Causa: SpringDoc 2.3.0 incompatible con Spring Framework 6.2
Solución: Actualizar a 2.7.0 en todos los pom.xml
```

---

### 5. BCrypt para Passwords (Factor 10)

**Decisión:** `BCryptPasswordEncoder` con strength=10

**Justificación:**
- ✅ **Estándar de la industria**: OWASP recomendado
- ✅ **Protección rainbow tables**: Salt automático
- ✅ **Resistente a GPU cracking**: Algoritmo adaptativo
- ✅ **Configurable**: Strength ajustable (10 = 2^10 = 1024 iteraciones)
- ✅ **Compatible Spring Security**: Integración nativa

**Benchmark:**
```
Strength 10: ~100ms por hash (aceptable para login)
Strength 12: ~400ms por hash (mejor seguridad, más lento)
Strength 8: ~25ms por hash (no recomendado)
```

**Alternativas evaluadas:**
- Argon2: Mejor, pero menos soporte en Java
- PBKDF2: Bueno, pero más vulnerable a GPU cracking
- SHA256: ❌ NO usar (sin salt, vulnerable)

---

### 6. JWT HS512 (No RS256)

**Decisión:** HMAC-SHA512 con secret compartido

**Justificación:**
- ✅ **Simplicidad Fase 1**: 1 microservicio genera tokens
- ✅ **Rendimiento**: HS512 es ~3x más rápido que RS256
- ✅ **Kong compatible**: Plugin JWT soporta HMAC nativamente
- ✅ **Secret management**: Variable de entorno `.env`
- ❌ **Contra**: Secret debe estar en Kong + Auth Service

**Cuándo migrar a RS256:**
- Fase 2+: Múltiples emisores de tokens
- Public key distribution necesaria
- Microservicios validan sin secret compartido

---

### 7. Docker Compose (No Kubernetes)

**Decisión:** Orquestación con Docker Compose

**Justificación:**
- ✅ **Simplicidad desarrollo**: 1 comando para levantar todo
- ✅ **Suficiente Fase 1**: 7 contenedores manejables
- ✅ **Portabilidad**: Funciona en Windows/Mac/Linux
- ✅ **Cost-effective**: No requiere cluster Kubernetes
- ✅ **Debugging fácil**: `docker logs` directo

**Cuándo migrar a Kubernetes:**
- Producción multi-región
- Auto-scaling horizontal
- Service mesh (Istio)
- Alta disponibilidad (réplicas + load balancing)

---

### 8. Rate Limiting 100 req/min

**Decisión:** Kong rate-limiting plugin con límite 100/min

**Justificación:**
- ✅ **Protección DoS**: Evita sobrecarga de servicios
- ✅ **Fair usage**: 100 req/min suficiente para uso normal
- ✅ **Granularidad por servicio**: Pedidos más restrictivo que Auth
- ✅ **Policy local**: No requiere Redis (simplicidad)

**Cálculo:**
```
Usuario normal: ~10-20 req/min
Spike máximo: ~50 req/min
Límite 100/min = 2x margen de seguridad
```

---

## 📈 Métricas de Arquitectura

**Performance:**
- Latencia Kong → Service: <10ms
- Validación JWT: <5ms
- Tiempo total request: <100ms (95 percentile)

**Escalabilidad:**
- Contenedores: 7 (Kong, Postgres, 4 services, 1 kong-db)
- RAM total: ~2GB
- CPU: 4 cores recomendado

**Disponibilidad:**
- Healthchecks: Cada 30s
- Restart policy: on-failure
- Depends_on: Secuenciación de inicio

---

**Documentación completa:** Ver `README.md` y `DEPLOYMENT.md`  
**Tests end-to-end:** `LOGIFLOW-Fase1.postman_collection.json`
