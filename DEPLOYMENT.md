# 🚀 Guía de Despliegue

## Requisitos Previos

- Docker Desktop 4.25+ (Windows/Mac) o Docker Engine 24+ (Linux)
- Docker Compose 2.20+
- Git 2.40+
- Postman 10+ (opcional, para tests)

## 📦 Instalación Completa

### 1. Clonar Repositorio
```bash
git clone https://github.com/juanspdf/LOGIFLOW.git
cd LOGIFLOW
```

### 2. Configurar Variables de Entorno
```bash
# Crear archivo .env en raíz del proyecto
echo "JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" > .env
echo "DB_USER=postgres" >> .env
echo "DB_PASSWORD=admin123" >> .env
echo "DB_HOST=postgres" >> .env
```

### 3. Levantar Infraestructura
```bash
# Construir y levantar todos los containers
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f

# Esperar 2-3 minutos para:
# - Maven compile (Spring Boot microservicios)
# - PostgreSQL initialization
# - Kong Gateway bootstrap
```

### 4. Verificar Servicios
```bash
# Ver estado de containers
docker compose ps

# Expected output:
# NAME                      STATUS                PORTS
# logiflow-postgres         Up (healthy)          0.0.0.0:5432->5432/tcp
# logiflow-kong-gateway     Up (healthy)          0.0.0.0:8000-8002->8000-8002/tcp
# logiflow-auth-service     Up 2 minutes          0.0.0.0:8081->8081/tcp
# logiflow-pedido-service   Up 2 minutes          0.0.0.0:8082->8082/tcp
# logiflow-fleet-service    Up 2 minutes          0.0.0.0:8083->8083/tcp
# logiflow-billing-service  Up 2 minutes          0.0.0.0:8084->8084/tcp
```

### 5. Configurar Kong Gateway
```bash
# Ejecutar script PowerShell (Windows)
.\scripts\configure-kong.ps1

# O manualmente (Linux/Mac):
# Ver ARCHITECTURE.md sección "Configuración Kong Gateway"
```

### 6. Verificar Kong
```bash
# Health check
curl http://localhost:8001/status

# Listar services
curl http://localhost:8001/services

# Listar routes
curl http://localhost:8001/routes

# Verificar plugins JWT
curl http://localhost:8001/plugins | jq '.data[] | select(.name=="jwt")'

# Verificar rate limiting
curl http://localhost:8001/plugins | jq '.data[] | select(.name=="rate-limiting")'
```

## 🧪 Pruebas End-to-End

### Opción 1: Postman Collection (Recomendado)
```bash
# 1. Abrir Postman
# 2. Import → Seleccionar "LOGIFLOW-Fase1.postman_collection.json"
# 3. Run Collection → Ejecutar todos los tests (11 requests)
# 4. Verificar tests pasaron (verde)
```

### Opción 2: Comandos curl Manual

#### Test 1: Register CLIENTE
```bash
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "cliente1@test.com",
    "password": "Pass1234!",
    "nombre": "Cliente",
    "apellido": "Test",
    "telefono": "0999999999",
    "roleName": "CLIENTE"
  }'

# Expected: HTTP 201 + JSON con accessToken
```

#### Test 2: Login CLIENTE
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "cliente1@test.com",
    "password": "Pass1234!"
  }'

# Expected: HTTP 200 + JSON con accessToken + user info
# Copiar accessToken para siguiente test
```

#### Test 3: Crear Pedido URBANA (con JWT)
```bash
# Reemplazar {TOKEN} con accessToken del test anterior
curl -X POST http://localhost:8000/api/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "clienteId": "{USER_ID}",
    "direccionOrigen": "Av. Amazonas, Quito",
    "direccionDestino": "La Carolina, Quito",
    "tipoEntrega": "URBANA",
    "zonaId": "QUITO-NORTE",
    "distanciaKm": 5.2,
    "notas": "Paquete frágil"
  }'

# Expected: HTTP 201 + JSON con pedido (estado=RECIBIDO)
# Copiar pedido.id para siguiente test
```

#### Test 4: GET Pedido (CLIENTE)
```bash
curl -X GET http://localhost:8000/api/pedidos/{PEDIDO_ID} \
  -H "Authorization: Bearer {TOKEN}"

# Expected: HTTP 200 + JSON con pedido (estado=RECIBIDO)
```

#### Test 5: Register + Login SUPERVISOR
```bash
# Register
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supervisor1@test.com",
    "password": "Pass1234!",
    "nombre": "Super",
    "apellido": "Visor",
    "telefono": "0988888888",
    "roleName": "SUPERVISOR"
  }'

# Login
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supervisor1@test.com",
    "password": "Pass1234!"
  }'

# Copiar SUPERVISOR accessToken
```

#### Test 6: GET Pedido (SUPERVISOR) ✅ CRITERIO FASE 1
```bash
curl -X GET http://localhost:8000/api/pedidos/{PEDIDO_ID} \
  -H "Authorization: Bearer {SUPERVISOR_TOKEN}"

# Expected: HTTP 200 + JSON con pedido (estado=RECIBIDO)
# ✅ Supervisor consulta pedido creado por cliente
```

#### Test 7: Test 401 sin JWT
```bash
curl -X GET http://localhost:8000/api/pedidos/{PEDIDO_ID}

# Expected: HTTP 401 Unauthorized (Kong rechaza sin JWT)
```

#### Test 8: Rate Limiting (100 req/min)
```bash
# Ejecutar 105 requests en 1 minuto
for i in {1..105}; do
  curl -X GET http://localhost:8000/api/pedidos/{PEDIDO_ID} \
    -H "Authorization: Bearer {TOKEN}" \
    -w "%{http_code}\n" -o /dev/null -s
done

# Expected: 100x HTTP 200 + 5x HTTP 429
```

## 🔧 Comandos Útiles

### Ver logs de servicios
```bash
# Logs específicos
docker compose logs auth-service -f
docker compose logs pedido-service --tail 100

# Todos los logs
docker compose logs -f
```

### Reiniciar servicios
```bash
# Reiniciar solo un servicio
docker compose restart auth-service

# Reiniciar todos
docker compose restart

# Rebuild completo (después de cambios código)
docker compose up -d --build
```

### Limpiar y resetear
```bash
# Detener y eliminar containers + volúmenes
docker compose down -v

# Eliminar imágenes
docker compose down --rmi all

# Limpiar todo (containers + volúmenes + imágenes + build cache)
docker compose down -v --rmi all
docker system prune -a --volumes -f
```

### Acceso directo a bases de datos
```bash
# PostgreSQL (app databases)
docker exec -it logiflow-postgres psql -U postgres -d logiflow_auth

# Queries útiles
\l                          # Listar databases
\c logiflow_pedido          # Conectar a DB
\dt                         # Listar tablas
SELECT * FROM pedidos;      # Query pedidos
SELECT * FROM usuarios;     # Query usuarios (en logiflow_auth)
```

### Kong Admin API
```bash
# Kong database (separado de app)
docker exec -it logiflow-kong-db psql -U kong

# Kong Admin GUI (browser)
open http://localhost:8002  # Kong Manager UI
```

## 🐛 Troubleshooting

### Error: "Cannot connect to Docker daemon"
```bash
# Windows: Iniciar Docker Desktop
# Linux: sudo systemctl start docker
```

### Error: "Port already in use"
```bash
# Ver procesos usando puertos
netstat -ano | findstr :8000   # Windows
lsof -i :8000                  # Linux/Mac

# Cambiar puertos en docker-compose.yml si necesario
```

### Error: "auth-service failed to start"
```bash
# Ver logs completos
docker compose logs auth-service

# Errores comunes:
# - PasswordEncoder bean missing → Verificar PasswordEncoderConfig.java existe
# - Database connection → Verificar postgres container está "healthy"
# - Port conflict → Cambiar puerto en docker-compose.yml
```

### Error: "Kong JWT validation failed"
```bash
# Verificar JWT plugin configurado
curl http://localhost:8001/plugins | jq '.data[] | select(.name=="jwt")'

# Verificar algorithm=HS512
curl http://localhost:8001/plugins/{PLUGIN_ID} | jq '.config.algorithm'

# Reconfigurar si necesario
.\scripts\configure-kong.ps1
```

### Error: "HTTP 404 double path /api/pedidos/api/pedidos"
```bash
# Verificar strip_path=true en routes
curl http://localhost:8001/routes/pedido-route | jq '.strip_path'

# Corregir si es false
curl -X PATCH http://localhost:8001/routes/pedido-route \
  -d strip_path=true
```

### Error: "Rate limiting not working"
```bash
# Verificar plugin en pedido-service
curl http://localhost:8001/services/pedido-service/plugins \
  | jq '.data[] | select(.name=="rate-limiting")'

# Aplicar plugin si falta
curl -X POST http://localhost:8001/services/pedido-service/plugins \
  -d name=rate-limiting \
  -d config.minute=100
```

## 🔄 Actualización de Código

Después de modificar código fuente:

```bash
# 1. Rebuild solo el servicio modificado
docker compose up -d --build auth-service

# 2. O rebuild todos
docker compose up -d --build

# 3. Verificar cambios
docker compose logs auth-service -f

# 4. Probar endpoints actualizados
```

## 📊 Monitoreo

### Health checks
```bash
# Kong
curl http://localhost:8001/status

# Auth Service (direct)
curl http://localhost:8081/actuator/health

# Pedido Service (direct)
curl http://localhost:8082/actuator/health

# Via Kong (requiere JWT)
curl http://localhost:8000/api/auth/status
```

### Métricas
```bash
# Docker stats en tiempo real
docker stats

# Uso de recursos por container
docker compose ps --format json | jq '.'
```

## 🔐 Seguridad en Producción

### Cambiar JWT_SECRET
```bash
# Generar nuevo secret seguro (64 chars hex)
openssl rand -hex 32

# Actualizar en .env
JWT_SECRET={NUEVO_SECRET}

# Rebuild auth-service
docker compose up -d --build auth-service

# Reconfigurar Kong consumer credential
curl -X PATCH http://localhost:8001/consumers/logiflow-jwt-validator/jwt/{CREDENTIAL_ID} \
  -d secret={NUEVO_SECRET}
```

### HTTPS en Kong
```bash
# Agregar certificado SSL
# Ver documentación Kong: https://docs.konghq.com/gateway/latest/get-started/services-and-routes/
```

---

**Soporte:** Ver `README.md` y `ARCHITECTURE.md`  
**Tests:** `LOGIFLOW-Fase1.postman_collection.json`  
**Issues:** https://github.com/juanspdf/LOGIFLOW/issues
