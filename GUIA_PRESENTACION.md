# 🎯 Guía de Presentación - LOGIFLOW Fase 1

**Sistema de Logística de Última Milla con Arquitectura de Microservicios**

Duración estimada: 15-20 minutos | Fecha: Diciembre 2025

---

## 📋 Checklist Pre-Presentación 

```powershell
# 1. Verificar Docker Desktop está corriendo
docker --version
docker compose version

# 2. Verificar puerto 8000 libre (Kong Gateway)
Test-NetConnection -ComputerName localhost -Port 8000

# 3. Limpiar ambiente previo
cd C:\Users\Usuario\OneDrive\Documentos\GitHub\LOGIFLOW
docker compose down -v
```

---

## 🚀 Parte 1: Introducción 

### Presentar el Contexto

**"LOGIFLOW es un sistema de logística de última milla implementado con arquitectura de microservicios."**

**Problema que resuelve:**
- Gestión descentralizada de pedidos, repartidores y facturación
- Necesidad de escalabilidad independiente por módulo
- Seguridad centralizada con JWT
- Trazabilidad de transacciones distribuidas

**Stack Tecnológico Principal:**
- **API Gateway:** Kong 3.5 (C/Nginx)
- **Microservicios:** Spring Boot 3.4.0 + Java 21 (Virtual Threads)
- **Base de Datos:** PostgreSQL 16 (6 tablas relacionales)
- **Seguridad:** JWT HS512 + BCrypt Factor 10
- **Infraestructura:** Docker Compose (7 contenedores)

---

## 🏗️ Parte 2: Arquitectura 

###  Diagrama

Abrir `ARCHITECTURE.md` y mostrar el diagrama ASCII (líneas 3-46):

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTE (Web/Mobile)                     │
│                       HTTP/HTTPS Requests                        │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                   ┌────────────▼────────────┐
                   │   KONG API GATEWAY      │
                   │     Puerto: 8000        │
                   │  ┌──────────────────┐   │
                   │  │ JWT Plugin       │   │
                   │  │ Rate Limiting    │   │
                   │  │ File Logging     │   │
                   │  └──────────────────┘   │
                   └─┬────┬────┬────┬────────┘
```

**Explicar componentes:**
- **Kong:** Routing, autenticación JWT, rate limiting (100 req/min)
- **4 Microservicios independientes:**
  - `auth-service:8081` → Usuarios, roles, tokens
  - `pedido-service:8082` → CRUD pedidos, Saga orquestada
  - `fleet-service:8083` → Repartidores, vehículos, estados
  - `billing-service:8084` → Facturas con IVA 15%
- **PostgreSQL:** 5 bases de datos (1 para Kong + 4 para apps)

---

## 💻 Parte 3: Demostración en Vivo (8 minutos)

### 3.1 Levantar el Sistema (2 min)

```powershell
# Navegar al proyecto
cd C:\Users\Usuario\OneDrive\Documentos\GitHub\LOGIFLOW

# Levantar toda la infraestructura
docker compose up -d

# Esperar a que todos los servicios estén healthy (30-45 segundos)
Write-Host "⏳ Esperando a que los servicios inicien..." -ForegroundColor Yellow
Start-Sleep -Seconds 45

# Verificar estado de contenedores
docker compose ps --format json | ConvertFrom-Json | Select-Object Name, Status, Health | Format-Table
```

**Resultado esperado:**
```
Name                        Status              Health
logiflow-kong              Up 45 seconds       healthy
logiflow-auth-service      Up 40 seconds       healthy
logiflow-pedido-service    Up 40 seconds       healthy
logiflow-fleet-service     Up 40 seconds       healthy
logiflow-billing-service   Up 40 seconds       healthy
kong-database              Up 45 seconds       healthy
logiflow-db                Up 45 seconds       healthy
```

### 3.2 Verificar Kong Gateway (1 min)

```powershell
# Probar endpoint de status de Kong
curl.exe http://localhost:8000/api/auth/status

# Resultado esperado:
# {"status":"Auth Service OK","timestamp":"2025-12-17T..."}
```

**Explicar:** "Kong está redirigiendo `/api/auth/*` hacia `auth-service:8081`"

### 3.3 Demostrar Flujo de Autenticación (3 min)

#### **Paso 1: Registrar un usuario**

```powershell
$registerBody = @{
    username = "demo_cliente"
    email = "demo@logiflow.com"
    password = "SecurePass123!"
    nombres = "Juan"
    apellidos = "Pérez"
    telefono = "0998765432"
} | ConvertTo-Json

$response = Invoke-RestMethod -Method POST `
    -Uri "http://localhost:8000/api/auth/register?roleName=CLIENTE" `
    -ContentType "application/json" `
    -Body $registerBody

$response | ConvertTo-Json
```

**Explicar:**
- Kong recibe la petición y valida rate limiting
- `auth-service` valida el email (regex), encripta con BCrypt
- Guarda en tabla `usuarios` de PostgreSQL
- Responde con usuario creado (sin password)

#### **Paso 2: Login y obtener JWT**

```powershell
$loginBody = @{
    username = "demo_cliente"
    password = "SecurePass123!"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Method POST `
    -Uri "http://localhost:8000/api/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

# Guardar token para siguientes requests
$token = $loginResponse.token
Write-Host "✅ Token JWT obtenido:" -ForegroundColor Green
Write-Host $token.Substring(0, 50) + "..." -ForegroundColor Cyan

$loginResponse | ConvertTo-Json
```

**Mostrar estructura del token:**
```
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkZW1vX2NsaWVudGUi...",
  "refreshToken": "uuid-v4-refresh-token",
  "type": "Bearer",
  "expiresIn": 3600
}
```

**Explicar:**
- JWT HS512 con 1 hora de expiración
- Refresh token con 7 días de validez
- Payload incluye: `username`, `roles`, `iat`, `exp`

#### **Paso 3: Acceder a recurso protegido**

```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

$pedidoBody = @{
    clienteId = $loginResponse.userId
    direccionOrigen = "Av. 6 de Diciembre N34-120"
    direccionDestino = "Av. Mariscal Sucre S28-15"
    descripcionPaquete = "Laptop Dell XPS 15"
    pesoKg = 2.5
    alto = 0.4
    ancho = 0.3
    profundidad = 0.05
} | ConvertTo-Json

$pedido = Invoke-RestMethod -Method POST `
    -Uri "http://localhost:8000/api/pedidos" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $pedidoBody

Write-Host "✅ Pedido creado con ID: $($pedido.id)" -ForegroundColor Green
$pedido | ConvertTo-Json
```

**Explicar:**
- Kong valida el JWT con plugin (si falla → HTTP 401)
- `pedido-service` crea el pedido con estado `PENDIENTE`
- Valida dimensiones y peso con Bean Validation
- Devuelve pedido con `fechaCreacion` y `costoEstimado`

#### **Paso 4: Probar Rate Limiting**

```powershell
Write-Host "`n⚠️ Probando rate limiting (100 req/min)..." -ForegroundColor Yellow

1..105 | ForEach-Object {
    try {
        Invoke-RestMethod -Method GET `
            -Uri "http://localhost:8000/api/auth/status" `
            -ErrorAction Stop | Out-Null
        Write-Host "." -NoNewline -ForegroundColor Green
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            Write-Host "`n❌ HTTP 429 - Rate Limit Exceeded" -ForegroundColor Red
            $_.Exception.Response | Select-Object StatusCode, StatusDescription
            break
        }
    }
}
```

**Resultado esperado:** Después de ~100 requests → HTTP 429

### 3.4 Verificar Base de Datos (2 min)

```powershell
# Conectar a PostgreSQL del auth-service
docker exec -it logiflow-db psql -U logiflow -d logiflow_auth

# Dentro de PostgreSQL:
# \dt
# SELECT id, username, email, created_at FROM usuarios;
# SELECT u.username, r.nombre FROM usuarios u JOIN usuario_roles ur ON u.id = ur.usuario_id JOIN roles r ON ur.rol_id = r.id;
# \q
```

**Mostrar en pantalla:**
```sql
 id |  username    |       email           |       created_at
----+--------------+-----------------------+------------------------
  1 | demo_cliente | demo@logiflow.com     | 2025-12-17 10:30:15
```

**Explicar:** "6 tablas con foreign keys: `usuarios`, `roles`, `usuario_roles`, `pedidos`, `repartidor`, `vehiculo`, `facturas`"

---

## 📊 Parte 4: Evidencia de Calidad (2 minutos)

### 4.1 Contratos OpenAPI

```powershell
# Mostrar contratos exportados
Get-ChildItem docs/*openapi* | Select-Object Name, Length

# Abrir uno en navegador
Start-Process "http://localhost:8081/swagger-ui/index.html"
```

**Explicar:**
- 8 archivos OpenAPI 3.0 (4 JSON + 4 YAML)
- Schemas completos con validaciones
- Status codes documentados (200, 400, 401, 403, 404, 429, 500)
- SpringDoc 2.7.0 estandarizado

### 4.2 Tests Automatizados

```powershell
# Mostrar cantidad de tests
Get-ChildItem -Recurse -Filter "*Test.java" | Measure-Object | Select-Object Count
Write-Host "26 pruebas unitarias con JUnit 5 + Mockito" -ForegroundColor Cyan

# Mostrar colección de Postman
Get-Content "LOGIFLOW-Fase1.postman_collection.json" | ConvertFrom-Json | Select-Object -ExpandProperty item | Select-Object name
```

**Mencionar:**
- 26 tests unitarios (mocks de repositorios)
- 11 smoke tests funcionales (Postman)
- Cobertura de escenarios: crear pedido, login, refresh token, validaciones

### 4.3 Logs y Monitoreo

```powershell
# Mostrar logs de Kong (últimas 20 líneas)
docker logs logiflow-kong --tail 20

# Mostrar logs de auth-service (errores)
docker logs logiflow-auth-service 2>&1 | Select-String -Pattern "ERROR|WARN" | Select-Object -Last 10
```

**Explicar:** "Kong file-log plugin guarda cada request. En producción: Prometheus + Grafana + Jaeger"

---

## 🎓 Parte 5: Decisiones Técnicas 

### Mostrar Tabla de Justificaciones

Abrir `docs/LOGIFLOW-Fase1-Informe.tex` (líneas ~1050) o presentar verbalmente:

| Decisión | Alternativa | Justificación |
|----------|-------------|---------------|
| **Kong Gateway** | Spring Cloud Gateway | 40% menor latencia (C/Nginx vs Java) |
| **JWT HS512** | RS256 | 3x más rápido en firma/verificación |
| **PostgreSQL 16** | MongoDB | ACID + foreign keys nativos |
| **Saga Orquestada** | Saga Coreografiada | Control centralizado + debugging |
| **Docker Compose** | Kubernetes | Suficiente para 7 contenedores (Fase 1) |

**Explicar riesgos mitigados:**
1. **Fallo en cascada:** Kong health checks + circuit breakers
2. **Latencia de red:** Docker bridge + HikariCP pooling
3. **Inconsistencia de datos:** Saga pattern + idempotencia
4. **Exposición de credenciales:** Variables de entorno + BCrypt
5. **DDoS:** Rate limiting 100 req/min + CORS

---

## ✅ Parte 6: Entregables Cumplidos 

### Mostrar Checklist

```powershell
Get-Content VERIFICACION_ENTREGABLES_FASE1.md | Select-String -Pattern "✅|Puntuación"
```

**Recitar:**

| # | Entregable | Estado |
|---|------------|--------|
| 1 | Informe Técnico LaTeX (1280 líneas) | ✅ |
| 2 | Código Fuente Microservicios | ✅ |
| 3 | Contratos OpenAPI 3.0 (8 archivos) | ✅ |
| 4 | API Gateway Kong + plugins | ✅ |
| 5 | Base de Datos Relacional (6 tablas) | ✅ |
| 6 | Pruebas Unitarias e Integración | ✅ |
| 7 | Documento Diseño Técnico (ARCHITECTURE.md) | ✅ |

**Puntuación:** 98/100 (Gap: TestContainers pendiente para Fase 2)

---


---

## 🧹 Post-Presentación: Limpiar Ambiente

```powershell
# Detener todos los servicios
docker compose down

# Eliminar volúmenes (opcional, limpia datos)
docker compose down -v

# Verificar que todo se detuvo
docker compose ps
```

---

## 📚 Documentación de Referencia

Para profundizar durante preguntas:

- **`README.md`** → Instrucciones de instalación y comandos básicos
- **`ARCHITECTURE.md`** → 34 secciones con diagramas y decisiones técnicas
- **`DEPLOYMENT.md`** → Guía de despliegue y troubleshooting
- **`VERIFICACION_ENTREGABLES_FASE1.md`** → Auditoría completa de requisitos
- **`docs/LOGIFLOW-Fase1-Informe.tex`** → Informe formal para compilar a PDF

---

## 🎤 Tips para la Presentación

### ✅ Hacer:
- Practicar los comandos antes (la demo en vivo puede fallar)
- Tener capturas de pantalla de respaldo si Docker falla
- Explicar el "por qué" de cada decisión técnica
- Mostrar entusiasmo por el proyecto
- Preparar respuestas para: "¿Por qué no Kubernetes?" "¿Cómo escala esto?"

### ❌ Evitar:
- Leer el código fuente línea por línea (aburre)
- Entrar en detalles de implementación a menos que pregunten
- Disculparse por lo que falta (enfócate en lo logrado)
- Quedarse en silencio mientras Docker inicia (explica la arquitectura)

### 🚨 Plan B si Docker falla:
1. Mostrar capturas de Postman con los 11 tests pasando
2. Abrir Swagger UI (capturas guardadas)
3. Mostrar el código de un microservicio (ej: `PedidoController.java`)
4. Explicar la arquitectura con `ARCHITECTURE.md` abierto

---

## ⏱️ Resumen de Tiempos

| Sección | Tiempo | Contenido Clave |
|---------|--------|-----------------|
| Introducción | 2 min | Problema, stack tecnológico |
| Arquitectura | 3 min | Diagrama, componentes, comunicación |
| Demo en vivo | 8 min | Docker up, register, login, pedido, rate limit |
| Evidencia | 2 min | OpenAPI, tests, logs |
| Decisiones | 3 min | Justificaciones técnicas, riesgos |
| Entregables | 2 min | Checklist 7/7, 98/100 |
| Roadmap | 1 min | Fase 2 features |
| **TOTAL** | **21 min** | **+ 3-5 min preguntas** |

---

## 📞 Contacto y Repositorio

- **GitHub:** [https://github.com/juanspdf/LOGIFLOW](https://github.com/juanspdf/LOGIFLOW)
- **Documentación:** Ver carpeta `/docs` del repositorio
- **Postman Collection:** `LOGIFLOW-Fase1.postman_collection.json`

---

**¡Buena suerte en la presentación! 🚀**

*Última actualización: Diciembre 17, 2025*
