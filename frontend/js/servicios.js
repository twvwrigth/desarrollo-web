// Frontend E4 — Servicios y cortes
// Lo usa servicios.html

var SERVICIOS_CACHE = [];
var CORTES_CACHE = [];
var CORTE_SERVICIO_ID = null;

function sesionServicios() {
  try {
    var id = sessionStorage.getItem('usuarioId');
    if (!id) return null;
    return { id: Number(id), rol: sessionStorage.getItem('usuarioRol') || '' };
  } catch (e) { return null; }
}

function esAdminServicios() {
  var s = sesionServicios();
  return !!(s && s.rol === 'ADMIN');
}

// La sesion manda: si hay sesion, el ID que se usa es el de la sesion y el
// campo "ID de administrador" ya no se puede cambiar. Sin sesion se sigue
// usando lo que se escriba en el campo (flujo manual del equipo).
function leerAdminId() {
  var s = sesionServicios();
  if (s) return s.id;
  var el = document.getElementById('admin-id');
  if (el && el.value) return Number(el.value);
  return null;
}

// Con sesion de un rol que no es ADMIN no se muestra nada de
// crear/editar/eliminar. Sin sesion se deja todo visible (flujo manual).
function aplicarPermisosServicios() {
  var s = sesionServicios();
  var adminId = document.getElementById('admin-id');
  if (s && adminId) {
    adminId.value = s.id;
    adminId.readOnly = true;
  }
  if (s && s.rol !== 'ADMIN') {
    ['form-servicio', 'form-corte'].forEach(function (id) {
      var f = document.getElementById(id);
      if (f && f.closest('section')) f.closest('section').style.display = 'none';
    });
  }
}

// Guarda de las acciones: con sesion de un rol que no es ADMIN no se
// intenta nada (el backend ya daria 403, pero la UI no ofrece el intento).
function exigirAdminServicios(mensajeId) {
  var s = sesionServicios();
  if (s && s.rol !== 'ADMIN') {
    mostrarMensaje(mensajeId, 'Solo un administrador puede crear, editar o eliminar servicios y cortes.', true);
    return false;
  }
  return true;
}

function adminHeaders(mensajeId) {
  var id = leerAdminId();
  if (!id) {
    mostrarMensaje(mensajeId, 'Pon tu ID de administrador arriba: solo un admin puede crear, editar o eliminar servicios y cortes.', true);
    return null;
  }
  return { 'X-Usuario-Id': id };
}

// Servicios crear/editar/eliminar
async function cargarServiciosAdmin() {
  var cuerpo = document.getElementById('cuerpo-servicios-admin');
  if (!cuerpo) return;
  try {
    var r = await apiFetch('/servicios');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    SERVICIOS_CACHE = r.datos;
    pintarServiciosAdmin(r.datos);
    mostrarMensaje('mensaje-servicios', r.datos.length + ' servicio(s).', false);
  } catch (e) {
    cuerpo.innerHTML = '<tr><td colspan="5">No se pudieron cargar los servicios: ' + e.message + '</td></tr>';
    mostrarMensaje('mensaje-servicios', 'No se pudieron cargar los servicios: ' + e.message, true);
  }
}

function pintarServiciosAdmin(servicios) {
  var cuerpo = document.getElementById('cuerpo-servicios-admin');
  if (!servicios.length) {
    cuerpo.innerHTML = '<tr><td colspan="5">No hay servicios todavía.</td></tr>';
    return;
  }
  // Sin sesion se muestran (flujo manual del equipo); con sesion de un rol
  // que no es ADMIN se ocultan.
  var mostrarAcciones = !sesionServicios() || esAdminServicios();
  cuerpo.innerHTML = servicios.map(function (s) {
    var dur = s.duracionMin != null ? s.duracionMin + ' min' : '';
    var precio = s.precio != null ? '$' + Number(s.precio).toFixed(2) : '';
    var acciones = mostrarAcciones
      ? '<div class="acciones">' +
        '<button type="button" class="btn btn-pequeno" onclick="pedirEditarServicio(' + s.id + ')">Editar</button> ' +
        '<button type="button" class="btn btn-pequeno btn-peligro" onclick="eliminarServicio(' + s.id + ')">Eliminar</button>' +
        '</div>'
      : '';
    return '<tr><td>' + (s.nombre || 'Servicio') + '</td><td>' + (s.descripcion || '') + '</td>' +
      '<td>' + dur + '</td><td>' + precio + '</td>' +
      '<td>' + acciones + '</td></tr>';
  }).join('');
}

function pedirEditarServicio(id) {
  if (!exigirAdminServicios('mensaje-servicios')) return;
  var s = SERVICIOS_CACHE.find(function (x) { return x.id === id; });
  if (!s) return;
  document.getElementById('servicio-id').value = s.id;
  document.getElementById('servicio-nombre').value = s.nombre || '';
  document.getElementById('servicio-descripcion').value = s.descripcion || '';
  document.getElementById('servicio-duracion').value = s.duracionMin != null ? s.duracionMin : '';
  document.getElementById('servicio-precio').value = s.precio != null ? s.precio : '';
  document.getElementById('titulo-form-servicio').textContent = 'Editar servicio #' + s.id;
  document.getElementById('btn-cancelar-servicio').style.display = 'inline-block';
  document.getElementById('form-servicio').scrollIntoView({ behavior: 'smooth' });
}

function cancelarEdicionServicio() {
  document.getElementById('form-servicio').reset();
  document.getElementById('servicio-id').value = '';
  document.getElementById('titulo-form-servicio').textContent = 'Nuevo servicio';
  document.getElementById('btn-cancelar-servicio').style.display = 'none';
}

async function guardarServicio(event) {
  event.preventDefault();
  if (!exigirAdminServicios('mensaje-servicio')) return;
  var headers = adminHeaders('mensaje-servicio');
  if (!headers) return;
  var id = document.getElementById('servicio-id').value;
  var cuerpo = {
    nombre: document.getElementById('servicio-nombre').value,
    descripcion: document.getElementById('servicio-descripcion').value || null,
    duracionMin: Number(document.getElementById('servicio-duracion').value),
    precio: Number(document.getElementById('servicio-precio').value),
  };
  try {
    var r = id
      ? await apiFetch('/servicios/' + id, { method: 'PUT', headers: headers, body: JSON.stringify(cuerpo) })
      : await apiFetch('/servicios', { method: 'POST', headers: headers, body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-servicio', mensajeBackend(r.datos, 'No se pudo guardar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-servicio', (id ? 'Servicio #' + id + ' actualizado.' : 'Servicio creado. ID: ' + (r.datos.id || '(sin id en la respuesta)')), false);
    cancelarEdicionServicio();
    cargarServiciosAdmin();
    cargarServiciosEnSelect('corte-servicio-id');
    cargarServiciosEnSelect('renglon-servicio-id');
  } catch (e) {
    mostrarMensaje('mensaje-servicio', e.message, true);
  }
}
async function eliminarServicio(id) {
  if (!exigirAdminServicios('mensaje-servicios')) return;
  if (!confirm('¿Eliminar el servicio #' + id + '? Es una baja lógica: queda inactivo y ya no aparece en el catálogo.')) return;
  var headers = adminHeaders('mensaje-servicios');
  if (!headers) return;
  try {
    var r = await apiFetch('/servicios/' + id, { method: 'DELETE', headers: headers });
    if (r.status === 204) {
      mostrarMensaje('mensaje-servicios', 'Servicio #' + id + ' eliminado (baja lógica).', false);
      cargarServiciosAdmin();
      return;
    }
    mostrarMensaje('mensaje-servicios', mensajeBackend(r.datos, 'No se pudo eliminar') + ' (HTTP ' + r.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-servicios', e.message, true);
  }
}

async function cargarServiciosEnSelect(selectId) {
  var sel = document.getElementById(selectId);
  if (!sel) return;
  try {
    var r = await apiFetch('/servicios');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    SERVICIOS_CACHE = r.datos;
    sel.innerHTML = r.datos.map(function (s) {
      var dur = s.duracionMin != null ? s.duracionMin + ' min' : '';
      var precio = s.precio != null ? ' · $' + Number(s.precio).toFixed(2) : '';
      return '<option value="' + s.id + '">' + (s.nombre || ('Servicio #' + s.id)) + ' — ' + dur + precio + '</option>';
    }).join('');
  } catch (e) {
    // Si el backend no responde, se quedan las opciones que trae el HTML.
  }
}

//Cortes por servicio, crear/editar/eliminar
async function cargarCortesAdmin() {
  var sel = document.getElementById('corte-servicio-id');
  var cuerpo = document.getElementById('cuerpo-cortes-admin');
  var titulo = document.getElementById('titulo-cortes-admin');
  if (!sel || !cuerpo) return;
  var servicioId = sel.value;
  if (!servicioId) {
    cuerpo.innerHTML = '<tr><td colspan="3">Elige un servicio para ver sus cortes.</td></tr>';
    return;
  }
  CORTE_SERVICIO_ID = Number(servicioId);
  if (titulo) titulo.textContent = 'Cortes del servicio #' + servicioId;
  try {
    var r = await apiFetch('/servicios/' + servicioId + '/cortes');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    CORTES_CACHE = r.datos;
    pintarCortesAdmin(r.datos);
    mostrarMensaje('mensaje-cortes', r.datos.length + ' corte(s).', false);
  } catch (e) {
    cuerpo.innerHTML = '<tr><td colspan="3">No se pudieron cargar los cortes: ' + e.message + '</td></tr>';
    mostrarMensaje('mensaje-cortes', 'No se pudieron cargar los cortes: ' + e.message, true);
  }
}

function pintarCortesAdmin(cortes) {
  var cuerpo = document.getElementById('cuerpo-cortes-admin');
  if (!cortes.length) {
    cuerpo.innerHTML = '<tr><td colspan="3">Este servicio aún no tiene cortes.</td></tr>';
    return;
  }
  var mostrarAcciones = !sesionServicios() || esAdminServicios();
  cuerpo.innerHTML = cortes.map(function (c) {
    var acciones = mostrarAcciones
      ? '<div class="acciones">' +
        '<button type="button" class="btn btn-pequeno" onclick="pedirEditarCorte(' + c.id + ')">Editar</button> ' +
        '<button type="button" class="btn btn-pequeno btn-peligro" onclick="eliminarCorte(' + c.id + ')">Eliminar</button>' +
        '</div>'
      : '';
    return '<tr><td>' + (c.nombre || 'Corte') + '</td><td>' + (c.descripcion || '') + '</td>' +
      '<td>' + acciones + '</td></tr>';
  }).join('');
}

function pedirEditarCorte(id) {
  if (!exigirAdminServicios('mensaje-cortes')) return;
  var c = CORTES_CACHE.find(function (x) { return x.id === id; });
  if (!c) return;
  document.getElementById('corte-id').value = c.id;
  document.getElementById('corte-nombre').value = c.nombre || '';
  document.getElementById('corte-descripcion').value = c.descripcion || '';
  document.getElementById('corte-imagen').value = c.imagenUrl || '';
  document.getElementById('corte-overlay').value = c.overlayUrl || '';
  document.getElementById('titulo-form-corte').textContent = 'Editar corte #' + c.id;
  document.getElementById('btn-cancelar-corte').style.display = 'inline-block';
  document.getElementById('form-corte').scrollIntoView({ behavior: 'smooth' });
}

function cancelarEdicionCorte() {
  document.getElementById('form-corte').reset();
  document.getElementById('corte-id').value = '';
  document.getElementById('titulo-form-corte').textContent = 'Nuevo corte';
  document.getElementById('btn-cancelar-corte').style.display = 'none';
}

async function guardarCorte(event) {
  event.preventDefault();
  if (!exigirAdminServicios('mensaje-corte')) return;
  var headers = adminHeaders('mensaje-corte');
  if (!headers) return;
  var id = document.getElementById('corte-id').value;
  var servicioId = CORTE_SERVICIO_ID || Number(document.getElementById('corte-servicio-id').value);
  if (!servicioId) {
    mostrarMensaje('mensaje-corte', 'Elige primero el servicio del corte.', true);
    return;
  }
  var cuerpo = {
    nombre: document.getElementById('corte-nombre').value,
    descripcion: document.getElementById('corte-descripcion').value || null,
    imagenUrl: document.getElementById('corte-imagen').value || null,
    overlayUrl: document.getElementById('corte-overlay').value || null,
    servicio: { id: servicioId },
  };
  try {
    var r = id
      ? await apiFetch('/cortes/' + id, { method: 'PUT', headers: headers, body: JSON.stringify(cuerpo) })
      : await apiFetch('/cortes', { method: 'POST', headers: headers, body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-corte', mensajeBackend(r.datos, 'No se pudo guardar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-corte', (id ? 'Corte #' + id + ' actualizado.' : 'Corte creado. ID: ' + (r.datos.id || '(sin id en la respuesta)')), false);
    cancelarEdicionCorte();
    cargarCortesAdmin();
  } catch (e) {
    mostrarMensaje('mensaje-corte', e.message, true);
  }
}

async function eliminarCorte(id) {
  if (!exigirAdminServicios('mensaje-cortes')) return;
  if (!confirm('¿Eliminar el corte #' + id + '? Es una baja lógica: queda inactivo.')) return;
  var headers = adminHeaders('mensaje-cortes');
  if (!headers) return;
  try {
    var r = await apiFetch('/cortes/' + id, { method: 'DELETE', headers: headers });
    if (r.status === 204) {
      mostrarMensaje('mensaje-cortes', 'Corte #' + id + ' eliminado (baja lógica).', false);
      cargarCortesAdmin();
      return;
    }
    mostrarMensaje('mensaje-cortes', mensajeBackend(r.datos, 'No se pudo eliminar') + ' (HTTP ' + r.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-cortes', e.message, true);
  }
}
