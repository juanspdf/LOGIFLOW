# Script de configuración de Kong API Gateway para LogiFlow (Windows PowerShell)
# Ejecutar después de levantar docker-compose

$KONG_ADMIN = "http://localhost:8001"

# Cargar JWT_SECRET desde variables de entorno
if (-not $env:JWT_SECRET) {
    Write-Host "⚠️  JWT_SECRET no encontrado en variables de entorno" -ForegroundColor Yellow
    Write-Host "   Cargando desde archivo .env..." -ForegroundColor Yellow
    
    $envFile = Join-Path $PSScriptRoot "..\\.env"
    if (Test-Path $envFile) {
        Get-Content $envFile | ForEach-Object {
            if ($_ -match '^JWT_SECRET=(.+)$') {
                $env:JWT_SECRET = $matches[1]
            }
        }
    }
    
    if (-not $env:JWT_SECRET) {
        Write-Host "❌ ERROR: JWT_SECRET no disponible" -ForegroundColor Red
        exit 1
    }
}

$JWT_SECRET = $env:JWT_SECRET

Write-Host "🔧 Configurando Kong API Gateway para LogiFlow..." -ForegroundColor Cyan
Write-Host ""

# Esperar a que Kong esté listo
Write-Host "⏳ Esperando a que Kong esté listo..." -ForegroundColor Yellow
do {
    try {
        $null = Invoke-WebRequest -Uri $KONG_ADMIN -Method Head -ErrorAction Stop
        $kongReady = $true
    } catch {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
        $kongReady = $false
    }
} while (-not $kongReady)

Write-Host ""
Write-Host "✅ Kong está listo" -ForegroundColor Green
Write-Host ""

# ==================== SERVICIOS ====================
Write-Host "📦 Registrando servicios..." -ForegroundColor Cyan

# Auth Service
Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
    name = "auth-service"
    url = "http://auth-service:8081"
} | Out-Null

# Pedido Service
Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
    name = "pedido-service"
    url = "http://pedido-service:8082"
} | Out-Null

# Fleet Service
Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
    name = "fleet-service"
    url = "http://fleet-service:8083"
} | Out-Null

# Billing Service
Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method Post -Body @{
    name = "billing-service"
    url = "http://billing-service:8084"
} | Out-Null

Write-Host "✅ Servicios registrados" -ForegroundColor Green
Write-Host ""

# ==================== RUTAS ====================
Write-Host "🛣️  Configurando rutas..." -ForegroundColor Cyan

# Auth Routes (públicas)
Invoke-RestMethod -Uri "$KONG_ADMIN/services/auth-service/routes" -Method Post -Body @{
    "paths[]" = "/api/auth"
    strip_path = $true
    name = "auth-route"
} | Out-Null

# Pedido Routes
Invoke-RestMethod -Uri "$KONG_ADMIN/services/pedido-service/routes" -Method Post -Body @{
    "paths[]" = "/api/pedidos"
    strip_path = $true
    name = "pedido-route"
} | Out-Null

# Fleet Routes
Invoke-RestMethod -Uri "$KONG_ADMIN/services/fleet-service/routes" -Method Post -Body @{
    "paths[]" = "/api/fleet"
    strip_path = $true
    name = "fleet-route"
} | Out-Null

# Billing Routes
Invoke-RestMethod -Uri "$KONG_ADMIN/services/billing-service/routes" -Method Post -Body @{
    "paths[]" = "/api/billing"
    strip_path = $true
    name = "billing-route"
} | Out-Null

Write-Host "✅ Rutas configuradas" -ForegroundColor Green
Write-Host ""

# ==================== JWT PLUGIN ====================
Write-Host "🔐 Configurando autenticación JWT..." -ForegroundColor Cyan

# Crear consumidor
Invoke-RestMethod -Uri "$KONG_ADMIN/consumers" -Method Post -Body @{
    username = "logiflow-jwt-validator"
} | Out-Null

# Crear credencial JWT
Invoke-RestMethod -Uri "$KONG_ADMIN/consumers/logiflow-jwt-validator/jwt" -Method Post -Body @{
    key = "logiflow-auth-service"
    algorithm = "HS256"
    secret = $JWT_SECRET
} | Out-Null

# Aplicar JWT a rutas protegidas
$protectedRoutes = @("pedido-route", "fleet-route", "billing-route")
foreach ($route in $protectedRoutes) {
    Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$route/plugins" -Method Post -Body @{
        name = "jwt"
        "config.claims_to_verify" = "exp"
    } | Out-Null
}

Write-Host "✅ JWT configurado" -ForegroundColor Green
Write-Host ""

# ==================== RATE LIMITING ====================
Write-Host "⏱️  Configurando rate limiting (100 req/min)..." -ForegroundColor Cyan

$services = @("pedido-service", "fleet-service", "billing-service")
foreach ($service in $services) {
    Invoke-RestMethod -Uri "$KONG_ADMIN/services/$service/plugins" -Method Post -Body @{
        name = "rate-limiting"
        "config.minute" = 100
        "config.policy" = "local"
    } | Out-Null
}

Write-Host "✅ Rate limiting configurado" -ForegroundColor Green
Write-Host ""

# ==================== CORS ====================
Write-Host "🌐 Configurando CORS..." -ForegroundColor Cyan

Invoke-RestMethod -Uri "$KONG_ADMIN/plugins" -Method Post -Body @{
    name = "cors"
    "config.origins" = "*"
    "config.methods" = "GET,POST,PUT,PATCH,DELETE,OPTIONS"
    "config.headers" = "Accept,Authorization,Content-Type"
    "config.exposed_headers" = "X-Auth-Token"
    "config.credentials" = $true
    "config.max_age" = 3600
} | Out-Null

Write-Host "✅ CORS configurado" -ForegroundColor Green
Write-Host ""

# ==================== CORRELATION ID ====================
Write-Host "🔄 Configurando correlation ID..." -ForegroundColor Cyan

Invoke-RestMethod -Uri "$KONG_ADMIN/plugins" -Method Post -Body @{
    name = "correlation-id"
    "config.header_name" = "X-Request-ID"
    "config.generator" = "uuid"
    "config.echo_downstream" = $true
} | Out-Null

Write-Host "✅ Correlation ID configurado" -ForegroundColor Green
Write-Host ""

# ==================== VERIFICACIÓN ====================
Write-Host "🔍 Verificando configuración..." -ForegroundColor Cyan
Write-Host ""

Write-Host "Servicios registrados:" -ForegroundColor Yellow
$services = Invoke-RestMethod -Uri "$KONG_ADMIN/services"
$services.data | ForEach-Object { Write-Host "  - $($_.name)" }
Write-Host ""

Write-Host "Rutas configuradas:" -ForegroundColor Yellow
$routes = Invoke-RestMethod -Uri "$KONG_ADMIN/routes"
$routes.data | ForEach-Object { Write-Host "  - $($_.name)" }
Write-Host ""

Write-Host "Plugins activos:" -ForegroundColor Yellow
$plugins = Invoke-RestMethod -Uri "$KONG_ADMIN/plugins"
$plugins.data | ForEach-Object { Write-Host "  - $($_.name)" }
Write-Host ""

Write-Host "✅ ¡Configuración de Kong completada!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Información útil:" -ForegroundColor Cyan
Write-Host "   - Kong Proxy (API Gateway): http://localhost:8000"
Write-Host "   - Kong Admin API: http://localhost:8001"
Write-Host "   - Kong Admin GUI: http://localhost:8002"
Write-Host "   - Konga UI: http://localhost:1337"
Write-Host ""
Write-Host "🚀 Prueba la API:" -ForegroundColor Yellow
Write-Host '   curl http://localhost:8000/api/auth/login -H "Content-Type: application/json"'
Write-Host ""
