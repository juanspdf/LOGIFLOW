# 📚 Arquitectura Técnica

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

## 🔄 Flujo de Autenticación

```
1. Cliente → POST /api/auth/register
   ↓
2. Kong Gateway (sin JWT, pasa directo)
   ↓
3. Auth Service → BCrypt password → Guarda usuario
   ↓
4. Response HTTP 201

5. Cliente → POST /api/auth/login
   ↓
6. Kong Gateway (sin JWT, pasa directo)
   ↓
7. Auth Service → Valida password → Genera JWT HS512
   ↓
8. Response HTTP 200 + access_token + refresh_token

9. Cliente → GET /api/pedidos/{id} + Header: Authorization Bearer {token}
   ↓
10. Kong Gateway → Valida JWT (iss=logiflow-auth-service, exp)
    ↓ (JWT válido)
11. Pedido Service (recibe request sin validación adicional)
    ↓
12. Response HTTP 200 + Pedido JSON
```

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

**Documentación completa:** Ver `README.md` y `DEPLOYMENT.md`  
**Tests end-to-end:** `LOGIFLOW-Fase1.postman_collection.json`
