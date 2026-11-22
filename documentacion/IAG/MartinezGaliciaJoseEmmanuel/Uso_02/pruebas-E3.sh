#!/bin/bash
# Pruebas E3 — reseñas (José Emmanuel), desarrollo-web
# Uso: con el backend corriendo en WSL (desde demo/: ./mvnw spring-boot:run,
# puerto 8081), corre:  bash pruebas-E3.sh
# Corre los casos 1-11 de docs/PRUEBAS-CURL-JoseEmmanuel-E3.md en orden seguro:
# resumen y PUT primero, pausa para la prueba de persistencia (parar y
# rearrancar el backend), y el DELETE al final porque borra la reseña 1.
# NO incluye la sección 6 (reprogramar/cancelar cita, de Erick): esos
# endpoints aún no están en el repo; un 404 ahí no es error de tu prueba.
# Cada caso muestra el código real y el esperado, y guarda todo en
# resultados-E3-AAAAMMDD-HHMM.txt para pegarlo en el chat o en el Reporte 3.
set -u
BASE="http://localhost:8081/api"
LOG="resultados-E3-$(date +%Y%m%d-%H%M).txt"

echo "Pruebas E3 — reseñas. Resultados en $LOG" | tee "$LOG"

# code_body <método> <url> [datos] [header extra]
# Imprime cuerpo + código, y lo anota en el log.
run() {
  local method="$1"; local url="$2"; local data="${3:-}"; local extra="${4:-}"
  local body_file
  body_file=$(mktemp)
  local code
  if [ -n "$data" ]; then
    code=$(curl -s -o "$body_file" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" ${extra:+-H "$extra"} -d "$data")
  elif [ -n "$extra" ]; then
    code=$(curl -s -o "$body_file" -w "%{http_code}" -X "$method" "$url" -H "$extra")
  else
    code=$(curl -s -o "$body_file" -w "%{http_code}" -X "$method" "$url")
  fi
  # El cuerpo va a stderr para que CODE=$(run ...) capture solo el código.
  echo "HTTP $code — $(cat "$body_file")" >&2
  echo "HTTP $code — $(cat "$body_file")" >> "$LOG"
  rm -f "$body_file"
  echo "$code"
}

expect() {
  local label="$1"; local esperado="$2"; local real="$3"
  if [ "$real" = "$esperado" ]; then
    echo "  OK: $label (esperado $esperado, salió $real)" | tee -a "$LOG"
  else
    echo "  REVISAR: $label (esperado $esperado, salió $real) — pásame este código" | tee -a "$LOG"
  fi
}

# --- Preflight: ¿está el backend arriba? ---
echo | tee -a "$LOG"
echo "== Preflight: backend en $BASE ==" | tee -a "$LOG"
if ! curl -s --max-time 5 "$BASE/resenas/resumen?barberoId=3" > /dev/null; then
  echo "No responde el backend. Arráncalo en WSL desde demo/: ./mvnw spring-boot:run" | tee -a "$LOG"
  echo "Si arranca pero da error de MySQL, revisa que application.properties" | tee -a "$LOG"
  echo "apunte a tu IP 172.22.128.1 (no a la de Erick, 172.20.96.1) y que MySQL esté en Windows." | tee -a "$LOG"
  exit 1
fi
echo "Backend responde. Datos de partida esperados: cita 1 COMPLETADA, reseña 1 (calificación 5, cliente 2, barbero 3)." | tee -a "$LOG"

# --- 1. Resumen ---
echo | tee -a "$LOG"
echo "== 1. Resumen del barbero ==" | tee -a "$LOG"
echo "-- Caso 1: barbero 3 (esperado 200, promedio 5.0, total 1)" | tee -a "$LOG"
CODE=$(run GET "$BASE/resenas/resumen?barberoId=3"); expect "Caso 1 resumen barbero 3" 200 "$CODE"
echo "-- Caso 2: sin barberoId (esperado 400)" | tee -a "$LOG"
CODE=$(run GET "$BASE/resenas/resumen"); expect "Caso 2 sin barberoId" 400 "$CODE"
echo "-- Caso 3: barbero 999 (esperado 404)" | tee -a "$LOG"
CODE=$(run GET "$BASE/resenas/resumen?barberoId=999"); expect "Caso 3 barbero inexistente" 404 "$CODE"

# --- 2. PUT ---
echo | tee -a "$LOG"
echo "== 2. Editar reseña (PUT) ==" | tee -a "$LOG"
echo "-- Caso 4: cliente 2 edita reseña 1, 5 -> 4 (esperado 200)" | tee -a "$LOG"
CODE=$(run PUT "$BASE/resenas/1" '{"calificacion":4,"comentario":"Buen corte, un poco lento"}' "X-Usuario-Id: 2"); expect "Caso 4 PUT 5->4" 200 "$CODE"
echo "-- Caso 5: calificación 6 (esperado 400)" | tee -a "$LOG"
CODE=$(run PUT "$BASE/resenas/1" '{"calificacion":6,"comentario":"prueba rango"}' "X-Usuario-Id: 2"); expect "Caso 5 PUT rango" 400 "$CODE"
echo "-- Caso 6: barbero 3 intenta editarla (esperado 403)" | tee -a "$LOG"
CODE=$(run PUT "$BASE/resenas/1" '{"calificacion":1,"comentario":"no soy el cliente"}' "X-Usuario-Id: 3"); expect "Caso 6 PUT otro usuario" 403 "$CODE"
echo "-- Caso 7: reseña 999 (esperado 404)" | tee -a "$LOG"
CODE=$(run PUT "$BASE/resenas/999" '{"calificacion":5,"comentario":"prueba 404"}' "X-Usuario-Id: 2"); expect "Caso 7 PUT inexistente" 404 "$CODE"
echo "-- Verificación: resumen ya debe decir promedio 4.0, total 1" | tee -a "$LOG"
CODE=$(run GET "$BASE/resenas/resumen?barberoId=3"); expect "Resumen tras PUT" 200 "$CODE"

# --- Persistencia: pausa manual ---
echo | tee -a "$LOG"
echo "== Persistencia ==" | tee -a "$LOG"
echo "Para el backend (Ctrl+C en su terminal), vuelve a arrancarlo con" | tee -a "$LOG"
echo "./mvnw spring-boot:run desde demo/, y luego pulsa Enter aquí." | tee -a "$LOG"
read -r -p "Pulsa Enter cuando el backend esté arriba de nuevo... "
CODE=$(run GET "$BASE/resenas/resumen?barberoId=3"); expect "Resumen tras reinicio" 200 "$CODE"
echo "Si arriba dice promedio 4.0, total 1, la persistencia está bien." | tee -a "$LOG"
echo "Si dice total 0, revisa MySQL en Windows y application.properties (tu IP 172.22.128.1)." | tee -a "$LOG"

# --- 3. DELETE (al final: borra la reseña 1) ---
echo | tee -a "$LOG"
echo "== 3. Eliminar reseña (DELETE) — esto borra la reseña 1 ==" | tee -a "$LOG"
read -r -p "¿Seguro que ya anotaste lo de arriba? Pulsa Enter para seguir con el DELETE, o Ctrl+C para parar aquí... "
echo "-- Caso 8: barbero 3 intenta eliminarla (esperado 403)" | tee -a "$LOG"
CODE=$(run DELETE "$BASE/resenas/1" "" "X-Usuario-Id: 3"); expect "Caso 8 DELETE otro usuario" 403 "$CODE"
echo "-- Caso 9: reseña 999 (esperado 404)" | tee -a "$LOG"
CODE=$(run DELETE "$BASE/resenas/999" "" "X-Usuario-Id: 2"); expect "Caso 9 DELETE inexistente" 404 "$CODE"
echo "-- Caso 10: cliente 2 elimina su reseña 1 (esperado 204, sin cuerpo)" | tee -a "$LOG"
CODE=$(run DELETE "$BASE/resenas/1" "" "X-Usuario-Id: 2"); expect "Caso 10 DELETE propia" 204 "$CODE"
echo "-- Verificación: lista vacía y resumen total 0" | tee -a "$LOG"
CODE=$(run GET "$BASE/resenas?barberoId=3"); expect "Lista tras DELETE" 200 "$CODE"
CODE=$(run GET "$BASE/resenas/resumen?barberoId=3"); expect "Resumen tras DELETE" 200 "$CODE"
echo "-- Caso 11: eliminar otra vez la misma (esperado 404)" | tee -a "$LOG"
CODE=$(run DELETE "$BASE/resenas/1" "" "X-Usuario-Id: 2"); expect "Caso 11 DELETE repetido" 404 "$CODE"

echo | tee -a "$LOG"
echo "Listo. Resultados guardados en $LOG" | tee -a "$LOG"
echo "Para dejar la base como en E2 (opcional, para la demo), recrea la reseña:" | tee -a "$LOG"
echo "  curl -X POST $BASE/resenas -H 'Content-Type: application/json' -H 'X-Usuario-Id: 2' -d '{\"citaId\":1,\"calificacion\":5,\"comentario\":\"Excelente corte y buen trato\"}'" | tee -a "$LOG"
echo "Esperado: 201, con un id nuevo (ya no será el 1)." | tee -a "$LOG"
echo "Pásame el $LOG o los códigos que salieron distintos de lo esperado." | tee -a "$LOG"
