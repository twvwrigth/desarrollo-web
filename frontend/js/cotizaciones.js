// Frontend — Cotizaciones.
// Lo usa cotizaciones.html.
var SERVICIOS_CACHE = [];
var COTIZACIONES_CACHE = [];
var RENGLONES = []; // { servicioId, nombre, precio, cantidad }
async function cargarServiciosParaCotizacion() {
  var sel = document.getElementById('renglon-servicio-id');
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

function agregarRenglon() {
  var sel = document.getElementById('renglon-servicio-id');
  var cantidadEl = document.getElementById('renglon-cantidad');
  if (!sel || !sel.value) return;
  var cantidad = Number(cantidadEl.value);
  if (!cantidad || cantidad < 1) {
    mostrarMensaje('mensaje-renglon', 'La cantidad debe ser 1 o más.', true);
    return;
  }
  var s = SERVICIOS_CACHE.find(function (x) { return x.id === Number(sel.value); });
  RENGLONES.push({
    servicioId: Number(sel.value),
    nombre: s ? (s.nombre || ('Servicio #' + sel.value)) : ('Servicio #' + sel.value),
    precio: s && s.precio != null ? Number(s.precio) : 0,
    cantidad: cantidad,
  });
  cantidadEl.value = 1;
  pintarRenglones();
  mostrarMensaje('mensaje-renglon', '', false);
}

function quitarRenglon(i) {
  RENGLONES.splice(i, 1);
  pintarRenglones();
}

function pintarRenglones() {
  var cuerpo = document.getElementById('cuerpo-renglones');
  var totalEl = document.getElementById('total-cotizacion');
  if (!cuerpo) return;
  if (!RENGLONES.length) {
    cuerpo.innerHTML = '<tr><td colspan="4">Sin renglones todavía. Agrega al menos un servicio.</td></tr>';
    if (totalEl) totalEl.textContent = 'Total estimado: $0.00 (lo calcula el servidor al crearla).';
    return;
  }
  var total = 0;
  cuerpo.innerHTML = RENGLONES.map(function (r, i) {
    var sub = r.precio * r.cantidad;
    total += sub;
    return '<tr><td>' + r.nombre + '</td><td>' + r.cantidad + '</td>' +
      '<td>$' + r.precio.toFixed(2) + '</td><td>$' + sub.toFixed(2) + '</td>' +
      '<td><button type="button" class="btn btn-pequeno btn-peligro" onclick="quitarRenglon(' + i + ')">Quitar</button></td></tr>';
  }).join('');
  if (totalEl) totalEl.textContent = 'Total estimado: $' + total.toFixed(2) + ' (lo calcula el servidor al crearla).';
}

async function crearCotizacion(event) {
  event.preventDefault();
  var clienteId = document.getElementById('cotizacion-cliente-id').value;
  if (!RENGLONES.length) {
    mostrarMensaje('mensaje-cotizacion', 'Agrega al menos un renglón (servicio + cantidad) antes de crear la cotización.', true);
    return;
  }
  var cuerpo = {
    cliente: { id: Number(clienteId) },
    detalle: RENGLONES.map(function (r) {
      return { servicio: { id: r.servicioId }, cantidad: r.cantidad, precioUnitario: null };
    }),
  };
  try {
    var r = await apiFetch('/cotizaciones', { method: 'POST', body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-cotizacion', mensajeBackend(r.datos, 'No se pudo crear') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-cotizacion', 'Cotización creada. ID: ' + (r.datos.id || '(sin id en la respuesta)') + ' · Total: $' + Number(r.datos.total || 0).toFixed(2) + ' · Estado: ' + (r.datos.estado || 'PENDIENTE'), false);
    RENGLONES = [];
    pintarRenglones();
    document.getElementById('form-cotizacion').reset();
    document.getElementById('cotizacion-cliente-id').value = clienteId;
    buscarCotizaciones();
  } catch (e) {
    mostrarMensaje('mensaje-cotizacion', e.message, true);
  }
}

async function buscarCotizaciones(event) {
  if (event) event.preventDefault();
  var clienteId = document.getElementById('buscar-cliente-id').value;
  var cuerpo = document.getElementById('cuerpo-cotizaciones');
  if (!cuerpo || !clienteId) return;
  try {
    var r = await apiFetch('/cotizaciones?clienteId=' + clienteId);
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    COTIZACIONES_CACHE = r.datos;
    pintarCotizaciones(r.datos);
    mostrarMensaje('mensaje-lista-cotizaciones', r.datos.length + ' cotización(es).', false);
  } catch (e) {
    cuerpo.innerHTML = '<tr><td colspan="5">No se pudieron cargar las cotizaciones: ' + e.message + '</td></tr>';
    mostrarMensaje('mensaje-lista-cotizaciones', 'No se pudieron cargar las cotizaciones: ' + e.message, true);
  }
}

function pintarCotizaciones(cotizaciones) {
  var cuerpo = document.getElementById('cuerpo-cotizaciones');
  if (!cotizaciones.length) {
    cuerpo.innerHTML = '<tr><td colspan="5">Este cliente aún no tiene cotizaciones.</td></tr>';
    return;
  }
  cuerpo.innerHTML = cotizaciones.map(function (c) {
    var fecha = c.fecha ? String(c.fecha).replace('T', ' ').slice(0, 16) : '';
    var pendiente = c.estado === 'PENDIENTE';
    return '<tr><td>#' + c.id + '</td><td>' + fecha + '</td>' +
      '<td>$' + Number(c.total || 0).toFixed(2) + '</td><td>' + (c.estado || '') + '</td>' +
      '<td><div class="acciones">' +
      '<button type="button" class="btn btn-pequeno" onclick="verDetalleCotizacion(' + c.id + ')">Ver detalle</button> ' +
      (pendiente
        ? '<button type="button" class="btn btn-pequeno" onclick="cambiarEstadoCotizacion(' + c.id + ', \'ACEPTADA\')">Aceptar</button> ' +
          '<button type="button" class="btn btn-pequeno" onclick="cambiarEstadoCotizacion(' + c.id + ', \'RECHAZADA\')">Rechazar</button> ' +
          '<button type="button" class="btn btn-pequeno btn-peligro" onclick="eliminarCotizacion(' + c.id + ')">Eliminar</button>'
        : '<small>Solo una cotización PENDIENTE puede cambiar de estado o eliminarse.</small>') +
      '</div></td></tr>';
  }).join('');
}

async function verDetalleCotizacion(id) {
  var seccion = document.getElementById('seccion-detalle');
  var cuerpo = document.getElementById('cuerpo-detalle');
  var titulo = document.getElementById('titulo-detalle');
  if (!seccion || !cuerpo) return;
  seccion.style.display = 'block';
  titulo.textContent = 'Detalle de la cotización #' + id;
  try {
    var r = await apiFetch('/cotizaciones/' + id);
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    var detalle = r.datos.detalle || [];
    if (!detalle.length) {
      cuerpo.innerHTML = '<tr><td colspan="4">Sin renglones.</td></tr>';
    } else {
      cuerpo.innerHTML = detalle.map(function (d) {
        var nombre = (d.servicio && d.servicio.nombre) ? d.servicio.nombre : ('Servicio #' + ((d.servicio && d.servicio.id) || '?'));
        var pu = d.precioUnitario != null ? Number(d.precioUnitario) : 0;
        return '<tr><td>' + nombre + '</td><td>' + d.cantidad + '</td>' +
          '<td>$' + pu.toFixed(2) + '</td><td>$' + (pu * d.cantidad).toFixed(2) + '</td></tr>';
      }).join('');
    }
    mostrarMensaje('mensaje-detalle', 'Total: $' + Number(r.datos.total || 0).toFixed(2) + ' · Estado: ' + (r.datos.estado || ''), false);
  } catch (e) {
    cuerpo.innerHTML = '';
    mostrarMensaje('mensaje-detalle', 'No se pudo cargar el detalle: ' + e.message, true);
  }
}

async function cambiarEstadoCotizacion(id, estado) {
  try {
    var r = await apiFetch('/cotizaciones/' + id + '/estado', {
      method: 'PUT',
      body: JSON.stringify({ estado: estado }),
    });
    if (!r.ok) {
      mostrarMensaje('mensaje-lista-cotizaciones', mensajeBackend(r.datos, 'No se pudo cambiar el estado') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-lista-cotizaciones', 'Cotización #' + id + ' ahora está ' + (r.datos.estado || estado) + '.', false);
    buscarCotizaciones();
  } catch (e) {
    mostrarMensaje('mensaje-lista-cotizaciones', e.message, true);
  }
}

async function eliminarCotizacion(id) {
  if (!confirm('¿Eliminar la cotización #' + id + '? Solo se puede eliminar si está PENDIENTE.')) return;
  try {
    var r = await apiFetch('/cotizaciones/' + id, { method: 'DELETE' });
    if (r.status === 204) {
      mostrarMensaje('mensaje-lista-cotizaciones', 'Cotización #' + id + ' eliminada.', false);
      buscarCotizaciones();
      return;
    }
    mostrarMensaje('mensaje-lista-cotizaciones', mensajeBackend(r.datos, 'No se pudo eliminar') + ' (HTTP ' + r.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-lista-cotizaciones', e.message, true);
  }
}
