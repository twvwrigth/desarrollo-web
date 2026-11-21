// Frontend
// Consume la API en http://localhost:8081/api (backend en WSL, ./mvnw spring-boot:run desde demo/).
//.
const API_BASE = 'http://localhost:8081/api';


async function apiFetch(ruta, opciones) {
  const opts = opciones || {};
  const headers = Object.assign({ 'Content-Type': 'application/json' }, opts.headers || {});
  const usuarioId = sesionUsuarioId();
  if (usuarioId && !headers['X-Usuario-Id']) headers['X-Usuario-Id'] = usuarioId;
  let resp;
  try {
    resp = await fetch(API_BASE + ruta, Object.assign({}, opts, { headers: headers }));
  } catch (e) {
    throw new Error('No se pudo conectar con el backend en localhost:8081.');
  }
  const datos = await resp.json().catch(function () { return {}; });
  return { ok: resp.ok, status: resp.status, datos: datos };
}
function sesionUsuarioId() {
  try {
    const v = sessionStorage.getItem('usuarioId');
    return v ? Number(v) : null;
  } catch (e) { return null; }
}

function leerUsuarioId() {
  const sesion = sesionUsuarioId();
  if (sesion) return sesion;
  const el = document.getElementById('usuario-id');
  return el && el.value ? Number(el.value) : null;
}

// Muestra en la barra superior quién está en sesión
function mostrarSesion() {
  const el = document.getElementById('sesion-info');
  if (!el) return;
  const id = sesionUsuarioId();
  el.textContent = id ? ('Sesión: usuario #' + id) : '';
}

// Muestra el "mensaje" que manda el backend
function mostrarMensaje(id, texto, esError) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = texto;
  el.className = 'mensaje ' + (esError ? 'error' : 'exito');
}

function mensajeBackend(datos, porDefecto) {
  return datos.mensaje || datos.error || porDefecto;
}

function estrellas(n) {
  const v = Math.max(0, Math.min(5, Number(n) || 0));
  return '★'.repeat(v) + '☆'.repeat(5 - v);
}

