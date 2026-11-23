#!/usr/bin/env bash
# Pruebas cURL E2 - Jose Luis (usuarios y horarios), estilo clase
# Uso: bash pruebas-curl.sh
# Requiere el backend corriendo en http://localhost:8081
set -u
BASE="${BASE:-http://localhost:8081/api}"

echo "== 0. Login admin =="
ADMIN_JSON=$(curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" \
  -d '{"email":"admin@barberia.com","password":"admin123"}')
echo "$ADMIN_JSON"; echo; echo
# El id del admin sale del login (no se asume que sea 1)
ADMIN_ID="${ADMIN_ID:-$(echo "$ADMIN_JSON" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)}"
ADMIN_ID="${ADMIN_ID:-1}"

TS=$(date +%s)
echo "== 1. Registrar cliente =="
curl -s -X POST "$BASE/usuarios" -H "Content-Type: application/json" \
  -d "{\"nombre\":\"Ana $TS\",\"email\":\"ana$TS@correo.com\",\"password\":\"secreto123\",\"telefono\":\"2281234567\"}"; echo; echo

echo "== 2. Crear barbero con admin (id $ADMIN_ID) =="
BARBERO_JSON=$(curl -s -X POST "$BASE/usuarios" -H "Content-Type: application/json" -H "X-Usuario-Id: $ADMIN_ID" \
  -d "{\"nombre\":\"Barbero $TS\",\"email\":\"barbero$TS@correo.com\",\"password\":\"secreto123\",\"rol\":\"BARBERO\"}")
echo "$BARBERO_JSON"; echo; echo

# El script saca solo el BARBERO_ID para no tener que copiarlo a mano
BARBERO_ID="${BARBERO_ID:-$(echo "$BARBERO_JSON" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)}"
BARBERO_ID="${BARBERO_ID:-2}"
echo ">> BARBERO_ID detectado: $BARBERO_ID <<"; echo

echo "== 3. Agregar horario al barbero $BARBERO_ID =="
curl -s -X POST "$BASE/barberos/$BARBERO_ID/horarios" -H "Content-Type: application/json" -H "X-Usuario-Id: $ADMIN_ID" \
  -d '{"diaSemana":1,"horaInicio":"10:00","horaFin":"18:00"}'; echo; echo

echo "== 4. Consultar horarios =="
curl -s "$BASE/barberos/$BARBERO_ID/horarios"; echo; echo

echo "== 5. Consultar usuario =="
curl -s "$BASE/usuarios/$BARBERO_ID" -H "X-Usuario-Id: $ADMIN_ID"; echo; echo

echo "== 6. Login cliente =="
curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" \
  -d "{\"email\":\"ana$TS@correo.com\",\"password\":\"secreto123\"}"; echo; echo

echo "Listo. Usa curl -i para ver los codigos HTTP (201, 200, 401, 403, 404, 409)."
