// Esqueleto frontend E2 — José Emmanuel.
// Consume la API en http://localhost:8081/api (backend en WSL, ./mvnw spring-boot:run).
// Reseñas: endpoints reales de ResenaControlador (POST y GET /api/resenas).
// Servicios y citas: endpoints aún no existen en el repo; el código lo dice
// claro y usa datos de ejemplo en vez de fingir que el backend respondió.

const API_BASE = 'http://localhost:8081/api';

// Muestra el "mensaje" que manda el backend: {"error": "...", "mensaje": "..."}.
function mostrarMensaje(id, texto, esError) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = texto;
  el.className = 'mensaje ' + (esError ? 'error' : 'exito');
}

function estrellas(n) {
  const v = Math.max(0, Math.min(5, Number(n) || 0));
  return '★'.repeat(v) + '☆'.repeat(5 - v);
}

// ---- Servicios (index.html) ----
// GET /api/servicios aún no existe. Si algún día existe, se usa; si no,
// se muestran los servicios de ejemplo del cURL de reseñas.
const SERVICIOS_EJEMPLO = [
  { nombre: 'Corte clásico', descripcion: 'Corte con tijera y máquina', duracionMin: 30, precio: 150.00 },
  { nombre: 'Corte + barba', descripcion: 'Corte clásico y arreglo de barba', duracionMin: 45, precio: 200.00 },
  { nombre: 'Afeitado clásico', descripcion: 'Afeitado con navaja y toalla caliente', duracionMin: 20, precio: 120.00 },
];

async function cargarServicios() {
  const lista = document.getElementById('lista-servicios');
  const aviso = document.getElementById('aviso-servicios');
  if (!lista) return;
  try {
    const resp = await fetch(API_BASE + '/servicios');
    if (!resp.ok) throw new Error('sin endpoint');
    const servicios = await resp.json();
    pintarServicios(servicios);
    if (aviso) aviso.textContent = '';
  } catch (e) {
    pintarServicios(SERVICIOS_EJEMPLO);
    if (aviso) aviso.textContent = 'Mostrando servicios de ejemplo: GET /api/servicios aún no existe en el backend.';
  }
}

function pintarServicios(servicios) {
  const lista = document.getElementById('lista-servicios');
  lista.innerHTML = servicios.map(function (s) {
    const nombre = s.nombre || 'Servicio';
    const desc = s.descripcion || '';
    const dur = s.duracionMin || s.duracion_min || '';
    const precio = s.precio != null ? '$' + Number(s.precio).toFixed(2) : '';
    return '<article class="servicio"><h3>' + nombre + '</h3><p>' + desc + '</p>' +
      '<p>' + (dur ? dur + ' min · ' : '') + '<span class="precio">' + precio + '</span></p></article>';
  }).join('');
}

// ---- Agendar cita (agendar.html) ----
// POST /api/citas es de Erick y aún no existe. El formulario ya manda el
// cuerpo esperado; si el endpoint responde 404, se avisa en vez de fingir éxito.
async function agendarCita(event) {
  event.preventDefault();
  const fecha = document.getElementById('fecha-hora').value;
  const cuerpo = {
    clienteId: Number(document.getElementById('cliente-id').value),
    barberoId: Number(document.getElementById('barbero-id').value),
    servicioId: Number(document.getElementById('servicio-id').value),
    fechaHoraInicio: fecha ? fecha.replace('T', ' ') + ':00' : null,
  };
  try {
    const resp = await fetch(API_BASE + '/citas', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(cuerpo),
    });
    const datos = await resp.json().catch(function () { return {}; });
    if (!resp.ok) {
      mostrarMensaje('mensaje-cita', (datos.mensaje || datos.error || 'No se pudo agendar') + ' (HTTP ' + resp.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-cita', 'Cita agendada. ID: ' + (datos.id || '(sin id en la respuesta)'), false);
  } catch (e) {
    mostrarMensaje('mensaje-cita', 'No se pudo conectar con el backend en localhost:8081, o POST /api/citas aún no existe (módulo de Erick).', true);
  }
}

// ---- Reseñas (resenas.html) ----
async function cargarResenas(barberoId) {
  const url = barberoId ? API_BASE + '/resenas?barberoId=' + barberoId : API_BASE + '/resenas';
  const resp = await fetch(url);
  if (!resp.ok) {
    const datos = await resp.json().catch(function () { return {}; });
    throw new Error(datos.mensaje || datos.error || ('HTTP ' + resp.status));
  }
  return resp.json();
}

function pintarResenas(resenas) {
  const lista = document.getElementById('lista-resenas');
  if (!resenas.length) {
    lista.innerHTML = '<li>No hay reseñas todavía.</li>';
    return;
  }
  lista.innerHTML = resenas.map(function (r) {
    const fecha = r.fechaCreacion ? String(r.fechaCreacion).replace('T', ' ').slice(0, 16) : '';
    return '<li><span class="estrellas">' + estrellas(r.calificacion) + '</span> ' +
      '<strong>' + (r.calificacion || '') + '/5</strong>' +
      '<p>' + (r.comentario || '(sin comentario)') + '</p>' +
      '<small>Cita #' + (r.citaId || '?') + (fecha ? ' · ' + fecha : '') + '</small></li>';
  }).join('');
}

async function listarResenas(barberoId) {
  try {
    const resenas = await cargarResenas(barberoId);
    pintarResenas(resenas);
    mostrarMensaje('mensaje-lista', resenas.length + ' reseña(s).', false);
  } catch (e) {
    mostrarMensaje('mensaje-lista', 'No se pudieron cargar las reseñas: ' + e.message + '. ¿Está corriendo el backend en localhost:8081?', true);
  }
}

function listarTodasResenas() {
  return listarResenas(null);
}

function buscarResenas(event) {
  event.preventDefault();
  const id = document.getElementById('buscar-barbero-id').value;
  return listarResenas(id ? Number(id) : null);
}

// POST /api/resenas — 201 / 400 / 403 / 404 / 409 según ResenaControlador.
async function enviarResena(event) {
  event.preventDefault();
  const usuarioId = document.getElementById('usuario-id').value;
  const headers = { 'Content-Type': 'application/json' };
  if (usuarioId) headers['X-Usuario-Id'] = usuarioId;
  const cuerpo = {
    citaId: Number(document.getElementById('cita-id').value),
    calificacion: Number(document.getElementById('calificacion').value),
    comentario: document.getElementById('comentario').value,
  };
  try {
    const resp = await fetch(API_BASE + '/resenas', {
      method: 'POST',
      headers: headers,
      body: JSON.stringify(cuerpo),
    });
    const datos = await resp.json().catch(function () { return {}; });
    if (!resp.ok) {
      // 400 calificación inválida, 404 cita inexistente, 409 cita no
      // completada o ya reseñada, 403 no eres el cliente de la cita.
      mostrarMensaje('mensaje-resena', (datos.mensaje || datos.error || 'No se pudo enviar') + ' (HTTP ' + resp.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-resena', 'Reseña guardada. ID: ' + (datos.id || '(sin id en la respuesta)'), false);
    document.getElementById('form-resena').reset();
    listarTodasResenas();
  } catch (e) {
    mostrarMensaje('mensaje-resena', 'No se pudo conectar con el backend en localhost:8081.', true);
  }
}
