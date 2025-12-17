# 🚚 LogiFlow - Sistema de Gestión de Delivery

Plataforma de microservicios para gestión integral de operaciones de delivery: pedidos, flota, facturación y autenticación JWT.

## 🏗️ Arquitectura

```
Kong Gateway :8000 (Proxy) + JWT HS512 + Rate Limiting
    ├── Auth Service :8081 (Register, Login, Refresh Tokens)
    ├── Pedido Service :8082 (CRUD Pedidos, Estados, Validaciones)
    ├── Fleet Service :8083 (Vehículos, Repartidores, Disponibilidad)
    └── Billing Service :8084 (Facturas BORRADOR, Cálculo Tarifa)
         ↓
PostgreSQL :5432 (4 DBs: auth, pedido, fleet, billing)
```

## ⚡ Inicio Rápido

### 1. Levantar servicios
```bash
docker compose up -d
# Esperar ~2 minutos para compilación Maven en contenedores
```

### 2. Verificar estado
```bash
docker compose ps
# Todos deben estar "healthy" o "Up X seconds"
```

### 3. Probar con Postman
Importa `LOGIFLOW-Fase1.postman_collection.json`:
- Run collection completa (11 requests)
- Verifica tests automáticos (status codes, JWT, rate limit)

## 📖 Endpoints Principales

### Auth Service (vía Kong :8000)
- `POST /api/auth/register` - Registro usuarios (CLIENTE, SUPERVISOR, REPARTIDOR)
- `POST /api/auth/login` - Login + generación JWT
- `POST /api/auth/token/refresh` - Refresh access token

### Pedido Service (requiere JWT)
- `POST /api/pedidos` - Crear pedido (URBANA/INTERMUNICIPAL/NACIONAL)
- `GET /api/pedidos/{id}` - Consultar pedido
- `PATCH /api/pedidos/{id}` - Actualizar parcialmente
- `DELETE /api/pedidos/{id}` - Cancelación lógica

### Fleet Service (requiere JWT)
- `GET /api/fleet/disponible?fleetType=AUTO` - Vehículos disponibles
- `GET /api/fleet/vehiculos` - Listar todos
- `POST /api/fleet/vehiculos` - Registrar vehículo

### Billing Service (requiere JWT)
- `POST /api/billing/facturas` - Crear factura BORRADOR
- `GET /api/billing/facturas` - Listar facturas
- `GET /api/billing/facturas/{id}` - Consultar factura

## ✅ Fase 1 Completada (100%)

### Requisitos Técnicos
- [x] Microservicios REST con CRUD básico
- [x] API Gateway (Kong) con enrutamiento por prefijo
- [x] Validación JWT en rutas protegidas (401/403)
- [x] Rate limiting 100 req/min
- [x] Transacciones ACID (@Transactional)
- [x] Validación de esquemas (@Valid, @NotNull, @NotBlank)
- [x] OpenAPI 3.0 (Swagger UI en :8081-8084/swagger-ui.html)

### Criterio de Aceptación
✅ **Verificado:** Cliente autenticado crea pedido URBANA → Supervisor consulta y ve estado RECIBIDO

```bash
# Evidence real ejecutada:
1. POST /api/auth/register (CLIENTE) → HTTP 201
2. POST /api/auth/login → HTTP 200, JWT extraído
3. POST /api/pedidos + JWT → HTTP 201, estado=RECIBIDO
4. POST /api/auth/register (SUPERVISOR) → HTTP 201
5. POST /api/auth/login (SUPERVISOR) → HTTP 200, JWT extraído
6. GET /api/pedidos/{id} + JWT Supervisor → HTTP 200, estado=RECIBIDO ✅
7. 105 requests → 100x HTTP 200 + 5x HTTP 429 (rate limit OK)
```

## 🛠️ Tecnologías

- **Backend**: Spring Boot 3.4.0, Java 17
- **Gateway**: Kong 3.5
- **Database**: PostgreSQL 16
- **Auth**: JWT HS512, BCrypt
- **Build**: Maven 3.9, Docker Compose
- **Tests**: JUnit 5, Mockito, Postman

---

**Estado:** ✅ Fase 1 Producción Ready  
**Última verificación:** 2025-12-17  
**Collection Postman:** `LOGIFLOW-Fase1.postman_collection.json`
