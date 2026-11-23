#!/usr/bin/env bash
# Pruebas cURL E4 - Jose Luis (activar/desactivar + contrasena minima de 6)
# Uso: bash pruebas-curl-e4.sh
# Requiere el backend corriendo en http://localhost:8081
# Cada caso imprime OK o FAIL y al final RESULTADO_FINAL fail=0/1.
# Se puede correr las veces que se quiera: los correos llevan marca de tiempo.
set -u
BASE="${BASE:-http://localhost:8081/api}"
TS=$(date +%s)
fail=0
check(){ desc="$1"; exp="$2"; got="$3"; if [ "$exp" = "$got" ]; then echo "OK   $desc ($got)"; else echo "FAIL $desc esperado=$exp obtenido=$got"; fail=1; fi; }

# esperar app
for i in $(seq 1 40); do curl -s -o /dev/null "$BASE/usuarios" && break; sleep 2; done

# Login admin (el id sale del login, no se asume que sea 1)
ADMIN_JSON=$(curl -s -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@barberia.com","password":"admin123"}')
ADMIN_ID="${ADMIN_ID:-$(echo "$ADMIN_JSON" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)}"
ADMIN_ID="${ADMIN_ID:-1}"
echo "admin=$ADMIN_ID"

check "login admin" 200 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@barberia.com","password":"admin123"}')"
check "login contrasena mal" 401 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/auth/login -H 'Content-Type: application/json' -d '{"email":"admin@barberia.com","password":"noesesta"}')"

# Registro: 201, 400 por contrasena corta, 409 por duplicado
C=$(curl -s -X POST $BASE/usuarios -H 'Content-Type: application/json' -d "{\"nombre\":\"Cliente E4 $TS\",\"email\":\"e4cliente$TS@correo.com\",\"password\":\"secreto123\",\"telefono\":\"2281234567\"}")
CID=$(echo "$C" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "cliente=$CID"
check "registro 201" 201 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/usuarios -H 'Content-Type: application/json' -d "{\"nombre\":\"Otro $TS\",\"email\":\"e4otro$TS@correo.com\",\"password\":\"secreto123\"}")"
check "registro contrasena 5 caracteres" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/usuarios -H 'Content-Type: application/json' -d "{\"nombre\":\"Corta $TS\",\"email\":\"e4corta$TS@correo.com\",\"password\":\"abc12\"}")"
check "registro email duplicado" 409 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/usuarios -H 'Content-Type: application/json' -d "{\"nombre\":\"Dup $TS\",\"email\":\"e4cliente$TS@correo.com\",\"password\":\"secreto123\"}")"

# Crear BARBERO sin admin -> 403; con admin -> 201
check "crear BARBERO sin admin" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/usuarios -H 'Content-Type: application/json' -d "{\"nombre\":\"Barb $TS\",\"email\":\"e4barb$TS@correo.com\",\"password\":\"secreto123\",\"rol\":\"BARBERO\"}")"
B=$(curl -s -X POST $BASE/usuarios -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d "{\"nombre\":\"Barbero E4 $TS\",\"email\":\"e4barbero$TS@correo.com\",\"password\":\"secreto123\",\"rol\":\"BARBERO\"}")
BID=$(echo "$B" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "barbero=$BID"

# Cambio de contrasena con 5 caracteres -> 400 (con header de admin)
check "PUT contrasena 5 caracteres" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT $BASE/usuarios/$BID -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d '{"password":"abc12"}')"

# Desactivar / reactivar por /estado
check "DELETE usuario (baja logica)" 204 "$(curl -s -o /dev/null -w '%{http_code}' -X DELETE $BASE/usuarios/$CID -H "X-Usuario-Id: $ADMIN_ID")"
check "GET usuario tras baja" 404 "$(curl -s -o /dev/null -w '%{http_code}' $BASE/usuarios/$CID -H "X-Usuario-Id: $ADMIN_ID")"
check "login de inactivo" 401 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"e4cliente$TS@correo.com\",\"password\":\"secreto123\"}")"
F=$(curl -s "$BASE/usuarios?activos=false" -H "X-Usuario-Id: $ADMIN_ID")
echo "$F" | grep -q "e4cliente$TS@correo.com" && echo "OK   ?activos=false trae al inactivo" || { echo "FAIL ?activos=false no trae al inactivo"; fail=1; }
echo "$F" | grep -q '"activo":false' && echo "OK   inactivo viene con activo=false" || { echo "FAIL inactivo sin activo=false"; fail=1; }
check "PUT perfil sobre inactivo" 404 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT $BASE/usuarios/$CID -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d '{"nombre":"Nadie"}')"
check "/estado sin activo" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT $BASE/usuarios/$CID/estado -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d '{}')"
check "/estado id inexistente" 404 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT $BASE/usuarios/999999/estado -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d '{"activo":true}')"
check "reactivar por /estado" 200 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT $BASE/usuarios/$CID/estado -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d '{"activo":true}')"
check "GET usuario reactivado" 200 "$(curl -s -o /dev/null -w '%{http_code}' $BASE/usuarios/$CID -H "X-Usuario-Id: $ADMIN_ID")"

# Horarios del barbero E4: crear, traslape, listar, eliminar
# El dia varia con la marca de tiempo para que el script se pueda repetir.
DIA1=$(( (TS % 7) + 1 ))
DIA2=$(( (DIA1 % 7) + 1 ))
# Limpieza: borrar horarios previos de este barbero (es uno creado aqui mismo)
for VIEJO in $(curl -s "$BASE/barberos/$BID/horarios" | grep -o '"id":[0-9]*' | cut -d: -f2); do curl -s -o /dev/null -X DELETE "$BASE/horarios/$VIEJO" -H "X-Usuario-Id: $ADMIN_ID"; done
H=$(curl -s -X POST $BASE/barberos/$BID/horarios -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d "{\"diaSemana\":$DIA1,\"horaInicio\":\"09:00\",\"horaFin\":\"13:00\"}")
HID=$(echo "$H" | grep -o '"id":[0-9]*' | tail -1 | cut -d: -f2)
echo "horario=$HID dia=$DIA1"
check "crear horario" 201 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/barberos/$BID/horarios -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d "{\"diaSemana\":$DIA2,\"horaInicio\":\"09:00\",\"horaFin\":\"13:00\"}")"
check "horario traslapado" 409 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/barberos/$BID/horarios -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d "{\"diaSemana\":$DIA1,\"horaInicio\":\"12:00\",\"horaFin\":\"15:00\"}")"
check "horario fin<=inicio" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X POST $BASE/barberos/$BID/horarios -H 'Content-Type: application/json' -H "X-Usuario-Id: $ADMIN_ID" -d "{\"diaSemana\":$DIA1,\"horaInicio\":\"15:00\",\"horaFin\":\"13:00\"}")"
check "listar horarios" 200 "$(curl -s -o /dev/null -w '%{http_code}' $BASE/barberos/$BID/horarios)"
check "DELETE horario" 204 "$(curl -s -o /dev/null -w '%{http_code}' -X DELETE $BASE/horarios/$HID -H "X-Usuario-Id: $ADMIN_ID")"

echo "RESULTADO_FINAL fail=$fail"
exit $fail
