#!/bin/bash
# Script de configuración automática de Kong API Gateway para LogiFlow
# Ejecutar después de levantar docker-compose

set -e

KONG_ADMIN="http://localhost:8001"
JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

echo "🔧 Configurando Kong API Gateway para LogiFlow..."
echo ""

# Esperar a que Kong esté listo
echo "⏳ Esperando a que Kong esté listo..."
until $(curl --output /dev/null --silent --head --fail $KONG_ADMIN); do
    printf '.'
    sleep 2
done
echo ""
echo "✅ Kong está listo"
echo ""

# ==================== SERVICIOS ====================
echo "📦 Registrando servicios..."

# Auth Service
curl -i -X POST $KONG_ADMIN/services \
  --data name=auth-service \
  --data url=http://auth-service:8081

# Pedido Service
curl -i -X POST $KONG_ADMIN/services \
  --data name=pedido-service \
  --data url=http://pedido-service:8082

# Fleet Service
curl -i -X POST $KONG_ADMIN/services \
  --data name=fleet-service \
  --data url=http://fleet-service:8083

# Billing Service
curl -i -X POST $KONG_ADMIN/services \
  --data name=billing-service \
  --data url=http://billing-service:8084

echo "✅ Servicios registrados"
echo ""

# ==================== RUTAS ====================
echo "🛣️  Configurando rutas..."

# Auth Routes (públicas)
curl -i -X POST $KONG_ADMIN/services/auth-service/routes \
  --data "paths[]=/api/auth" \
  --data "strip_path=true" \
  --data "name=auth-route"

# Pedido Routes (protegidas)
curl -i -X POST $KONG_ADMIN/services/pedido-service/routes \
  --data "paths[]=/api/pedidos" \
  --data "strip_path=true" \
  --data "name=pedido-route"

# Fleet Routes (protegidas)
curl -i -X POST $KONG_ADMIN/services/fleet-service/routes \
  --data "paths[]=/api/fleet" \
  --data "strip_path=true" \
  --data "name=fleet-route"

# Billing Routes (protegidas)
curl -i -X POST $KONG_ADMIN/services/billing-service/routes \
  --data "paths[]=/api/billing" \
  --data "strip_path=true" \
  --data "name=billing-route"

echo "✅ Rutas configuradas"
echo ""

# ==================== JWT PLUGIN ====================
echo "🔐 Configurando autenticación JWT..."

# Crear consumidor para validación JWT
curl -i -X POST $KONG_ADMIN/consumers \
  --data username=logiflow-jwt-validator

# Obtener consumer ID
CONSUMER_ID=$(curl -s $KONG_ADMIN/consumers/logiflow-jwt-validator | grep -o '"id":"[^"]*' | cut -d'"' -f4)

# Crear credencial JWT
curl -i -X POST $KONG_ADMIN/consumers/logiflow-jwt-validator/jwt \
  --data key=logiflow-auth-service \
  --data algorithm=HS256 \
  --data secret=$JWT_SECRET

# Aplicar JWT plugin a rutas protegidas
for route in pedido-route fleet-route billing-route; do
    curl -i -X POST $KONG_ADMIN/routes/$route/plugins \
      --data name=jwt \
      --data config.claims_to_verify=exp
done

echo "✅ JWT configurado"
echo ""

# ==================== RATE LIMITING ====================
echo "⏱️  Configurando rate limiting (100 req/min)..."

# Rate limiting global por consumer
for service in pedido-service fleet-service billing-service; do
    curl -i -X POST $KONG_ADMIN/services/$service/plugins \
      --data name=rate-limiting \
      --data config.minute=100 \
      --data config.policy=local
done

echo "✅ Rate limiting configurado"
echo ""

# ==================== LOGGING ====================
echo "📝 Configurando logging..."

# HTTP Log plugin (opcional, para logs centralizados)
# curl -i -X POST $KONG_ADMIN/plugins \
#   --data name=http-log \
#   --data config.http_endpoint=http://logstash:5000

# File log para desarrollo
curl -i -X POST $KONG_ADMIN/plugins \
  --data name=file-log \
  --data config.path=/tmp/kong-access.log

echo "✅ Logging configurado"
echo ""

# ==================== CORS ====================
echo "🌐 Configurando CORS..."

curl -i -X POST $KONG_ADMIN/plugins \
  --data name=cors \
  --data config.origins=* \
  --data config.methods=GET \
  --data config.methods=POST \
  --data config.methods=PUT \
  --data config.methods=PATCH \
  --data config.methods=DELETE \
  --data config.methods=OPTIONS \
  --data config.headers=Accept \
  --data config.headers=Authorization \
  --data config.headers=Content-Type \
  --data config.exposed_headers=X-Auth-Token \
  --data config.credentials=true \
  --data config.max_age=3600

echo "✅ CORS configurado"
echo ""

# ==================== REQUEST TRANSFORMER ====================
echo "🔄 Configurando transformadores de request..."

# Agregar headers personalizados para trazabilidad
curl -i -X POST $KONG_ADMIN/plugins \
  --data name=correlation-id \
  --data config.header_name=X-Request-ID \
  --data config.generator=uuid \
  --data config.echo_downstream=true

echo "✅ Transformadores configurados"
echo ""

# ==================== VERIFICACIÓN ====================
echo "🔍 Verificando configuración..."
echo ""

echo "Servicios registrados:"
curl -s $KONG_ADMIN/services | grep -o '"name":"[^"]*' | cut -d'"' -f4
echo ""

echo "Rutas configuradas:"
curl -s $KONG_ADMIN/routes | grep -o '"name":"[^"]*' | cut -d'"' -f4
echo ""

echo "Plugins activos:"
curl -s $KONG_ADMIN/plugins | grep -o '"name":"[^"]*' | cut -d'"' -f4
echo ""

echo "✅ ¡Configuración de Kong completada!"
echo ""
echo "📋 Información útil:"
echo "   - Kong Proxy (API Gateway): http://localhost:8000"
echo "   - Kong Admin API: http://localhost:8001"
echo "   - Kong Admin GUI: http://localhost:8002"
echo "   - Konga UI: http://localhost:1337"
echo ""
echo "🚀 Prueba la API:"
echo "   curl http://localhost:8000/api/auth/login"
echo ""
