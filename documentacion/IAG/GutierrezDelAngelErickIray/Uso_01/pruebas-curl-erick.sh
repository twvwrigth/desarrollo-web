#!/bin/bash
# Camino feliz E2 de Erick: servicios, cortes, citas y cotizaciones.
# Uso: bash docs/pruebas-curl-erick.sh  (con el backend en :8081 y MySQL arriba)
# Solo usa curl + grep, sin dependencias extra.
set -u
BASE="http://localhost:8081"
FALLOS=0

# Extrae el primer "id" de una respuesta JSON.
extraer_id() { grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'; }

# Llama a la API y guarda cuerpo + código. Uso: llamar <codigo_esperado> <método> <ruta> [header...] -- <json>
# Ejemplo: llamar 201 POST /api/servicios -H "X-Usuario-Id: 1" -- '{"nombre":"..."}'
llamar() {
  local esperado="$1"; shift
  local metodo="$1"; shift
  local ruta="$1"; shift
  local headers=()
  while [ "$1" != "--" ]; do headers+=("$1"); shift; done
  shift
  local cuerpo="$1"
  local resp codigo
  if [ -n "$cuerpo" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X "$metodo" "$BASE$ruta" -H "Content-Type: application/json" "${headers[@]}" -d "$cuerpo")
  else
    resp=$(curl -s -w "\n%{http_code}" -X "$metodo" "$BASE$ruta" "${headers[@]}")
  fi
  codigo=$(echo "$resp" | tail -1)
  CUERPO=$(echo "$resp" | sed '$d')
  if [ "$codigo" = "$esperado" ]; then
    echo "OK   $metodo $ruta -> $codigo"
  else
    echo "FALLO $metodo $ruta -> $codigo (esperado $esperado): $CUERPO"
    FALLOS=$((FALLOS + 1))
  fi
}

echo "== Login admin de demo =="
llamar 200 POST /api/auth/login -- '{"email":"admin@barberia.com","password":"admin123"}'
ADMIN_ID=$(echo "$CUERPO" | extraer_id)
echo "   ADMIN_ID=$ADMIN_ID"

echo "== Registrar cliente =="
llamar 201 POST /api/usuarios -- '{"nombre":"Erick Cliente","email":"cliente-e2@barberia.com","password":"cliente123","telefono":"2281000000"}'
CLIENTE_ID=$(echo "$CUERPO" | extraer_id)
echo "   CLIENTE_ID=$CLIENTE_ID"

echo "== Crear barbero (solo admin) =="
llamar 201 POST /api/usuarios -H "X-Usuario-Id: $ADMIN_ID" -- '{"nombre":"Barbero E2","email":"barbero-e2@barberia.com","password":"barbero123","telefono":"2281000001","rol":"BARBERO"}'
BARBERO_ID=$(echo "$CUERPO" | extraer_id)
echo "   BARBERO_ID=$BARBERO_ID"

echo "== Crear servicio =="
llamar 201 POST /api/servicios -H "X-Usuario-Id: $ADMIN_ID" -- '{"nombre":"Corte clásico E2","descripcion":"Corte tradicional","duracionMin":30,"precio":150.00}'
SERVICIO_ID=$(echo "$CUERPO" | extraer_id)
echo "   SERVICIO_ID=$SERVICIO_ID"

echo "== Servicio duplicado -> 409 =="
llamar 409 POST /api/servicios -H "X-Usuario-Id: $ADMIN_ID" -- '{"nombre":"Corte clásico E2","descripcion":"Otro","duracionMin":30,"precio":150.00}'

echo "== Servicio sin admin -> 403 =="
llamar 403 POST /api/servicios -- '{"nombre":"Barba E2","descripcion":"Arreglo de barba","duracionMin":20,"precio":80.00}'

echo "== Crear corte =="
llamar 201 POST /api/cortes -H "X-Usuario-Id: $ADMIN_ID" -- "{\"nombre\":\"Fade bajo E2\",\"descripcion\":\"Degradado bajo\",\"servicio\":{\"id\":$SERVICIO_ID}}"
CORTE_ID=$(echo "$CUERPO" | extraer_id)
echo "   CORTE_ID=$CORTE_ID"

echo "== Crear cita =="
llamar 201 POST /api/citas -- "{\"cliente\":{\"id\":$CLIENTE_ID},\"barbero\":{\"id\":$BARBERO_ID},\"servicio\":{\"id\":$SERVICIO_ID},\"corte\":{\"id\":$CORTE_ID},\"fechaHoraInicio\":\"2026-11-20T10:00:00\",\"fechaHoraFin\":\"2026-11-20T10:30:00\",\"notas\":\"Con navaja\"}"
CITA_ID=$(echo "$CUERPO" | extraer_id)
echo "   CITA_ID=$CITA_ID"

echo "== Cita traslapada -> 409 =="
llamar 409 POST /api/citas -- "{\"cliente\":{\"id\":$CLIENTE_ID},\"barbero\":{\"id\":$BARBERO_ID},\"servicio\":{\"id\":$SERVICIO_ID},\"corte\":{\"id\":$CORTE_ID},\"fechaHoraInicio\":\"2026-11-20T10:15:00\",\"fechaHoraFin\":\"2026-11-20T10:45:00\"}"

echo "== GETs =="
llamar 200 GET /api/servicios --
llamar 200 GET /api/servicios/$SERVICIO_ID --
llamar 200 GET /api/servicios/$SERVICIO_ID/cortes --
llamar 200 GET /api/cortes --
llamar 200 GET /api/cortes/$CORTE_ID --
llamar 200 GET /api/citas/$CITA_ID --
llamar 200 GET /api/clientes/$CLIENTE_ID/citas --
llamar 200 GET /api/barberos/$BARBERO_ID/citas --

echo "== 404 =="
llamar 404 GET /api/servicios/999999 --

if [ "$FALLOS" = 0 ]; then
  echo "TODO OK: camino feliz E2 de Erick pasó."
else
  echo "$FALLOS prueba(s) fallaron."
  exit 1
fi
