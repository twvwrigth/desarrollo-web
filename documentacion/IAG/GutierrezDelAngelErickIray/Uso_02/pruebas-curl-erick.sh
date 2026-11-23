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

echo "== Crear segundo servicio (para la cotización) =="
llamar 201 POST /api/servicios -H "X-Usuario-Id: $ADMIN_ID" -- '{"nombre":"Barba E2","descripcion":"Arreglo de barba","duracionMin":20,"precio":80.00}'
SERVICIO2_ID=$(echo "$CUERPO" | extraer_id)
echo "   SERVICIO2_ID=$SERVICIO2_ID"

echo "== Crear cotización (total esperado: 1x150 + 2x80 = 310) =="
llamar 201 POST /api/cotizaciones -- "{\"cliente\":{\"id\":$CLIENTE_ID},\"detalle\":[{\"servicio\":{\"id\":$SERVICIO_ID},\"cantidad\":1,\"precioUnitario\":150.00},{\"servicio\":{\"id\":$SERVICIO2_ID},\"cantidad\":2}]}"
COTIZACION_ID=$(echo "$CUERPO" | extraer_id)
echo "   COTIZACION_ID=$COTIZACION_ID"
echo "   Respuesta: $CUERPO"

echo "== GETs =="
llamar 200 GET /api/cotizaciones/$COTIZACION_ID --
llamar 200 GET /api/clientes/$CLIENTE_ID/cotizaciones --

echo "== 404 =="
llamar 404 GET /api/servicios/999999 --

if [ "$FALLOS" = 0 ]; then
  echo "TODO OK: camino feliz E2 de Erick pasó."
else
  echo "$FALLOS prueba(s) fallaron."
  exit 1
fi
