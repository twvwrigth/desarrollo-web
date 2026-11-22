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

// ---- Servicios y cortes
const SERVICIOS_EJEMPLO = [
  { nombre: 'Corte clásico', descripcion: 'Corte con tijera y máquina', duracionMin: 30, precio: 150.00 },
  { nombre: 'Corte + barba', descripcion: 'Corte clásico y arreglo de barba', duracionMin: 45, precio: 200.00 },
  { nombre: 'Afeitado clásico', descripcion: 'Afeitado con navaja y toalla caliente', duracionMin: 20, precio: 120.00 },
];

async function cargarServicios() {
  const cuerpo = document.getElementById('cuerpo-servicios');
  const aviso = document.getElementById('aviso-servicios');
  if (!cuerpo) return;
  try {
    const r = await apiFetch('/servicios');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    pintarServicios(r.datos);
    if (aviso) aviso.textContent = '';
  } catch (e) {
    pintarServicios(SERVICIOS_EJEMPLO);
    if (aviso) aviso.textContent = 'Mostrando servicios de ejemplo: ' + e.message + ' (GET /api/servicios).';
  }
}

function pintarServicios(servicios) {
  const cuerpo = document.getElementById('cuerpo-servicios');
  if (!servicios.length) {
    cuerpo.innerHTML = '<tr><td colspan="5">No hay servicios todavía.</td></tr>';
    return;
  }
  cuerpo.innerHTML = servicios.map(function (s) {
    const dur = s.duracionMin || s.duracion_min || '';
    const precio = s.precio != null ? '$' + Number(s.precio).toFixed(2) : '';
    return '<tr><td>' + (s.nombre || 'Servicio') + '</td><td>' + (s.descripcion || '') + '</td>' +
      '<td>' + (dur ? dur + ' min' : '') + '</td><td>' + precio + '</td>' +
      '<td><button type="button" class="btn btn-pequeno" onclick="cargarCortes(' + s.id + ', \'' + (s.nombre || '').replace(/'/g, '') + '\')">Ver cortes</button></td></tr>';
  }).join('');
}

// Cortes de un servicio
async function cargarCortes(servicioId, nombreServicio) {
  const seccion = document.getElementById('seccion-cortes');
  const titulo = document.getElementById('titulo-cortes');
  const cuerpo = document.getElementById('cuerpo-cortes');
  const mensaje = document.getElementById('mensaje-cortes');
  if (!cuerpo) return;
  seccion.style.display = 'block';
  titulo.textContent = 'Cortes de: ' + (nombreServicio || ('servicio #' + servicioId));
  try {
    const r = await apiFetch('/servicios/' + servicioId + '/cortes');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    if (!r.datos.length) {
      cuerpo.innerHTML = '<tr><td colspan="3">Este servicio aún no tiene cortes.</td></tr>';
    } else {
      cuerpo.innerHTML = r.datos.map(function (c) {
        var imagen = c.imagenUrl ? '<img src="' + c.imagenUrl + '" alt="Imagen del corte" style="max-width: 80px;">' : '(sin imagen)';
        return '<tr><td>' + (c.nombre || 'Corte') + '</td><td>' + (c.descripcion || '') + '</td><td>' + imagen + '</td></tr>';
      }).join('');
    }
    mostrarMensaje('mensaje-cortes', r.datos.length + ' corte(s).', false);
  } catch (e) {
    cuerpo.innerHTML = '';
    mostrarMensaje('mensaje-cortes', 'No se pudieron cargar los cortes: ' + e.message, true);
  }
}
async function agendarCita(event) {
  event.preventDefault();
  const inicio = document.getElementById('fecha-hora').value;
  const fin = document.getElementById('fecha-hora-fin').value;
  const cuerpo = {
    cliente: { id: Number(document.getElementById('cliente-id').value) },
    barbero: { id: Number(document.getElementById('barbero-id').value) },
    servicio: { id: Number(document.getElementById('servicio-id').value) },
    corte: { id: Number(document.getElementById('corte-id').value) },
    fechaHoraInicio: inicio ? inicio + ':00' : null,
    fechaHoraFin: fin ? fin + ':00' : null,
    notas: document.getElementById('notas-cita').value || null,
  };
  try {
    const resp = await fetch(API_BASE + '/citas', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(cuerpo),
    });
    const datos = await resp.json().catch(function () { return {}; });
    if (!resp.ok) {
      mostrarMensaje('mensaje-cita', mensajeBackend(datos, 'No se pudo agendar') + ' (HTTP ' + resp.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-cita', 'Cita agendada. ID: ' + (datos.id || '(sin id en la respuesta)') + ' · Estado: ' + (datos.estado || 'PENDIENTE'), false);
    listarMisCitas();
  } catch (e) {
    mostrarMensaje('mensaje-cita', 'No se pudo conectar con el backend en localhost:8081.', true);
  }
}
async function listarMisCitas() {
  const clienteId = document.getElementById('filtro-cliente-id');
  const estado = document.getElementById('filtro-estado');
  const lista = document.getElementById('lista-citas');
  if (!lista || !clienteId || !clienteId.value) return;
  try {
    const resp = await fetch(API_BASE + '/clientes/' + clienteId.value + '/citas');
    if (!resp.ok) throw new Error('HTTP ' + resp.status);
    let citas = await resp.json();
    if (estado && estado.value) citas = citas.filter(function (c) { return c.estado === estado.value; });
    pintarCitas(citas);
    mostrarMensaje('mensaje-citas', citas.length + ' cita(s).', false);
  } catch (e) {
    mostrarMensaje('mensaje-citas', 'No se pudieron cargar las citas: ' + e.message + '. ¿Está corriendo el backend en localhost:8081?', true);
  }
}

function pintarCitas(citas) {
  const lista = document.getElementById('lista-citas');
  if (!citas.length) {
    lista.innerHTML = '<li>No hay citas con ese filtro.</li>';
    return;
  }
  lista.innerHTML = citas.map(function (c) {
    const inicio = c.fechaHoraInicio ? String(c.fechaHoraInicio).replace('T', ' ').slice(0, 16) : '';
    const fin = c.fechaHoraFin ? String(c.fechaHoraFin).replace('T', ' ').slice(11, 16) : '';
    const puedeEditar = c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA';
    return '<li><strong>Cita #' + c.id + '</strong> · ' + (c.estado || '') +
      '<p>' + inicio + (fin ? ' – ' + fin : '') + (c.notas ? ' · ' + c.notas : '') + '</p>' +
      (puedeEditar
        ? '<div class="acciones">' +
          '<button type="button" class="btn btn-pequeno" onclick="pedirReprogramar(' + c.id + ')">Reprogramar</button> ' +
          '<button type="button" class="btn btn-pequeno" onclick="pedirCambiarEstadoCita(' + c.id + ', \'' + (c.estado || '') + '\')">Estado</button> ' +
          '<button type="button" class="btn btn-pequeno btn-peligro" onclick="cancelarCita(' + c.id + ')">Cancelar</button>' +
          '</div>'
        : '<small>Esta cita ya no se puede reprogramar ni cancelar (' + (c.estado || '') + ').</small>') +
      '</li>';
  }).join('');
}

//Reprograma fecha y notas.
function pedirReprogramar(citaId) {
  document.getElementById('reprogramar-cita-id').value = citaId;
  document.getElementById('form-reprogramar').style.display = 'block';
  document.getElementById('form-reprogramar').scrollIntoView({ behavior: 'smooth' });
}


async function reprogramarCita(citaId, inicio, fin, notas) {
  try {
    const resp = await fetch(API_BASE + '/citas/' + citaId, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fechaHoraInicio: inicio, fechaHoraFin: fin, notas: notas || null }),
    });
    const datos = await resp.json().catch(function () { return {}; });
    if (!resp.ok) {
      mostrarMensaje('mensaje-citas', mensajeBackend(datos, 'No se pudo reprogramar') + ' (HTTP ' + resp.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-citas', 'Cita #' + citaId + ' reprogramada.', false);
    listarMisCitas();
  } catch (e) {
    mostrarMensaje('mensaje-citas', 'No se pudo conectar con el backend en localhost:8081.', true);
  }
}

async function guardarReprogramacion(event) {
  event.preventDefault();
  const citaId = document.getElementById('reprogramar-cita-id').value;
  const inicio = document.getElementById('reprogramar-inicio').value;
  const fin = document.getElementById('reprogramar-fin').value;
  const notas = document.getElementById('reprogramar-notas').value || null;
  try {
    const resp = await fetch(API_BASE + '/citas/' + citaId, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fechaHoraInicio: inicio ? inicio + ':00' : null, fechaHoraFin: fin ? fin + ':00' : null, notas: notas }),
    });
    const datos = await resp.json().catch(function () { return {}; });
    if (!resp.ok) {
      mostrarMensaje('mensaje-reprogramar', (datos.mensaje || datos.error || 'No se pudo reprogramar') + ' (HTTP ' + resp.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-reprogramar', 'Cita #' + citaId + ' reprogramada.', false);
    document.getElementById('form-reprogramar').style.display = 'none';
    listarMisCitas();
  } catch (e) {
    mostrarMensaje('mensaje-reprogramar', 'No se pudo conectar con el backend en localhost:8081.', true);
  }
}


//cancela la cita
async function cancelarCita(citaId) {
  if (!confirm('¿Cancelar la cita #' + citaId + '?')) return;
  try {
    const resp = await fetch(API_BASE + '/citas/' + citaId, { method: 'DELETE' });
    if (resp.status === 204) {
      mostrarMensaje('mensaje-citas', 'Cita #' + citaId + ' cancelada.', false);
      listarMisCitas();
      return;
    }
    const datos = await resp.json().catch(function () { return {}; });
    mostrarMensaje('mensaje-citas', mensajeBackend(datos, 'No se pudo cancelar') + ' (HTTP ' + resp.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-citas', 'No se pudo conectar con el backend en localhost:8081.', true);
  }
}

// ---- Reseñas (resenas.html) — MÓDULO DE EMMANUEL, E4: en TABLA ----
async function cargarResenas(barberoId) {
  const url = barberoId ? '/resenas?barberoId=' + barberoId : '/resenas';
  const r = await apiFetch(url);
  if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
  return r.datos;
}

// E4: las reseñas se pintan en tabla (Cita | Calificación | Comentario | Fecha | Acciones).
function pintarResenas(resenas) {
  const cuerpo = document.getElementById('cuerpo-resenas');
  if (!resenas.length) {
    cuerpo.innerHTML = '<tr><td colspan="5">No hay reseñas todavía.</td></tr>';
    return;
  }
  cuerpo.innerHTML = resenas.map(function (r) {
    const fecha = r.fechaCreacion ? String(r.fechaCreacion).replace('T', ' ').slice(0, 16) : '';
    return '<tr><td>#' + (r.citaId || '?') + '</td>' +
      '<td><span class="estrellas">' + estrellas(r.calificacion) + '</span> ' + (r.calificacion || '') + '/5</td>' +
      '<td>' + (r.comentario || '(sin comentario)') + '</td>' +
      '<td>' + fecha + '</td>' +
      '<td><div class="acciones">' +
      '<button type="button" class="btn btn-pequeno" onclick="pedirEditarResena(' + r.id + ')">Editar</button> ' +
      '<button type="button" class="btn btn-pequeno btn-peligro" onclick="eliminarResena(' + r.id + ')">Eliminar</button>' +
      '</div></td></tr>';
  }).join('');
}

let RESENAS_CACHE = [];
async function listarResenas(barberoId) {
  try {
    const resenas = await cargarResenas(barberoId);
    RESENAS_CACHE = resenas;
    pintarResenas(resenas);
    mostrarMensaje('mensaje-lista', resenas.length + ' reseña(s).', false);
    if (barberoId) cargarResumen(barberoId);
  } catch (e) {
    mostrarMensaje('mensaje-lista', 'No se pudieron cargar las reseñas: ' + e.message, true);
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

async function cargarResumen(barberoId) {
  const el = document.getElementById('resumen-barbero');
  if (!el) return;
  try {
    const r = await apiFetch('/resenas/resumen?barberoId=' + barberoId);
    if (!r.ok) {
      el.textContent = 'Resumen no disponible: ' + mensajeBackend(r.datos, 'HTTP ' + r.status);
      return;
    }
    el.textContent = 'Promedio: ' + estrellas(r.datos.promedio) + ' ' + r.datos.promedio + '/5 · ' + r.datos.total + ' reseña(s).';
  } catch (e) {
    el.textContent = 'Resumen no disponible: ' + e.message;
  }
}
async function enviarResena(event) {
  event.preventDefault();
  const usuarioId = leerUsuarioId();
  const headers = {};
  if (usuarioId) headers['X-Usuario-Id'] = usuarioId;
  const cuerpo = {
    citaId: Number(document.getElementById('cita-id').value),
    calificacion: Number(document.getElementById('calificacion').value),
    comentario: document.getElementById('comentario').value,
  };
  try {
    const r = await apiFetch('/resenas', { method: 'POST', headers: headers, body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-resena', mensajeBackend(r.datos, 'No se pudo enviar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-resena', 'Reseña guardada. ID: ' + (r.datos.id || '(sin id en la respuesta)'), false);
    document.getElementById('form-resena').reset();
    listarTodasResenas();
  } catch (e) {
    mostrarMensaje('mensaje-resena', e.message, true);
  }
}

// Solo el cliente de la cita puede editarla: se manda X-Usuario-Id.
function pedirEditarResena(id) {
  const r = RESENAS_CACHE.find(function (x) { return x.id === id; });
  if (!r) return;
  document.getElementById('editar-resena-id').value = r.id;
  document.getElementById('editar-calificacion').value = r.calificacion;
  document.getElementById('editar-comentario').value = r.comentario || '';
  document.getElementById('form-editar-resena').style.display = 'block';
  document.getElementById('form-editar-resena').scrollIntoView({ behavior: 'smooth' });
}


async function guardarEdicionResena(event) {
  event.preventDefault();
  const id = document.getElementById('editar-resena-id').value;
  const usuarioId = leerUsuarioId();
  const headers = {};
  if (usuarioId) headers['X-Usuario-Id'] = usuarioId;
  try {
    const r = await apiFetch('/resenas/' + id, {
      method: 'PUT',
      headers: headers,
      body: JSON.stringify({
        calificacion: Number(document.getElementById('editar-calificacion').value),
        comentario: document.getElementById('editar-comentario').value,
      }),
    });
    if (!r.ok) {
      mostrarMensaje('mensaje-editar', mensajeBackend(r.datos, 'No se pudo editar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-editar', 'Reseña #' + id + ' actualizada.', false);
    document.getElementById('form-editar-resena').style.display = 'none';
    listarTodasResenas();
  } catch (e) {
    mostrarMensaje('mensaje-editar', e.message, true);
  }
}
// Eliminar reseña
async function eliminarResena(id) {
  if (!confirm('¿Eliminar la reseña #' + id + '?')) return;
  const usuarioId = leerUsuarioId();
  const headers = {};
  if (usuarioId) headers['X-Usuario-Id'] = usuarioId;
  try {
    const r = await apiFetch('/resenas/' + id, { method: 'DELETE', headers: headers });
    if (r.status === 204) {
      mostrarMensaje('mensaje-lista', 'Reseña #' + id + ' eliminada.', false);
      listarTodasResenas();
      return;
    }
    mostrarMensaje('mensaje-lista', mensajeBackend(r.datos, 'No se pudo eliminar') + ' (HTTP ' + r.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-lista', e.message, true);
  }
}
