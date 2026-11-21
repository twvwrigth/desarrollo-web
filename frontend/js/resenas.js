// Reseñas (resenas.html) — MÓDULO DE EMMANUEL, E4: en TABLA.
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

// Label con nombres de barberos para "Ver reseñas de un barbero".
async function cargarBarberosParaResenas() {
  const sel = document.getElementById('buscar-barbero-select');
  if (!sel) return;
  try {
    const r = await apiFetch('/usuarios?rol=BARBERO');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    sel.innerHTML = '<option value="">— todos —</option>' + r.datos.map(function (b) {
      return '<option value="' + b.id + '">' + (b.nombre || ('Barbero #' + b.id)) + '</option>';
    }).join('');
  } catch (e) {
    sel.innerHTML = '<option value="">No se pudieron cargar los barberos</option>';
  }
}

// Si el usuario está logueado, precarga su ID y el ID de su última cita COMPLETADA
// en el formulario de "Dejar una reseña".
async function precargarDatosResena() {
  const usuarioId = sesionUsuarioId();
  if (!usuarioId) return;
  const campoUsuario = document.getElementById('usuario-id');
  if (campoUsuario) {
    campoUsuario.value = usuarioId;
    campoUsuario.readOnly = true;
  }
  try {
    const r = await apiFetch('/clientes/' + usuarioId + '/citas');
    if (!r.ok) return;
    const completadas = (r.datos || []).filter(function (c) { return c.estado === 'COMPLETADA'; });
    if (!completadas.length) return;
    completadas.sort(function (a, b) {
      return new Date(b.fechaHoraInicio) - new Date(a.fechaHoraInicio);
    });
    const campoCita = document.getElementById('cita-id');
    if (campoCita) campoCita.value = completadas[0].id;
  } catch (e) {
    // sin conexión al backend: se deja el campo vacío
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