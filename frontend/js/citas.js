// Frontend E4 — Citas: cambiar estado.
// Lo usa agendar.html (botón "Estado" de "Mis citas").
// ---- Citas: cambiar estado solo el cliente, el barbero o un admins
var ESTADO_CITA_ID = null;

function pedirCambiarEstadoCita(citaId, estadoActual) {
  ESTADO_CITA_ID = citaId;
  document.getElementById('estado-cita-id').value = citaId;
  var sel = document.getElementById('estado-nuevo');
  var opciones = [];
  if (estadoActual === 'PENDIENTE') opciones = ['CONFIRMADA', 'CANCELADA'];
  else if (estadoActual === 'CONFIRMADA') opciones = ['COMPLETADA', 'CANCELADA'];
  sel.innerHTML = opciones.map(function (e) { return '<option value="' + e + '">' + e + '</option>'; }).join('');
  document.getElementById('titulo-estado-cita').textContent = 'Cambiar estado de la cita #' + citaId + ' (' + estadoActual + ')';
  document.getElementById('form-cambiar-estado').style.display = 'block';
  document.getElementById('form-cambiar-estado').scrollIntoView({ behavior: 'smooth' });
}

async function guardarCambioEstadoCita(event) {
  event.preventDefault();
  var citaId = ESTADO_CITA_ID || document.getElementById('estado-cita-id').value;
  var nuevo = document.getElementById('estado-nuevo').value;
  var clienteId = document.getElementById('filtro-cliente-id').value;
  if (!clienteId) {
    mostrarMensaje('mensaje-estado', 'Pon tu ID de cliente en el filtro de "Mis citas": el cambio de estado lo hace el cliente, el barbero o un admin.', true);
    return;
  }
  try {
    var r = await apiFetch('/citas/' + citaId + '/estado', {
      method: 'PUT',
      headers: { 'X-Usuario-Id': Number(clienteId) },
      body: JSON.stringify({ estado: nuevo }),
    });
    if (!r.ok) {
      mostrarMensaje('mensaje-estado', mensajeBackend(r.datos, 'No se pudo cambiar el estado') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-estado', 'Cita #' + citaId + ' ahora está ' + (r.datos.estado || nuevo) + '.', false);
    document.getElementById('form-cambiar-estado').style.display = 'none';
    listarMisCitas();
  } catch (e) {
    mostrarMensaje('mensaje-estado', e.message, true);
  }
}

// ---- Agendar: selects de barbero/corte y fin calculado (E4).
// Estas funciones las pedía el agendar.html pero no existían: por eso la
// consola marcaba "recalcularFinAgendar is not defined" y el select de
// barbero se quedaba en "Cargando barberos...".

var DURACIONES_SERVICIO = null; // id -> duracionMin, cargado de /api/servicios

async function cargarDuracionesServicios() {
  if (DURACIONES_SERVICIO) return DURACIONES_SERVICIO;
  try {
    const r = await apiFetch('/servicios');
    if (!r.ok) throw new Error('HTTP ' + r.status);
    DURACIONES_SERVICIO = {};
    (r.datos || []).forEach(function (s) {
      DURACIONES_SERVICIO[s.id] = s.duracionMin;
    });
  } catch (e) {
    DURACIONES_SERVICIO = {};
  }
  return DURACIONES_SERVICIO;
}

function duracionDelServicioSeleccionado() {
  const sel = document.getElementById('servicio-id');
  if (!sel || !sel.value) return null;
  const id = Number(sel.value);
  if (DURACIONES_SERVICIO && DURACIONES_SERVICIO[id]) return DURACIONES_SERVICIO[id];
  // Respaldo: la duración viene en el texto de la opción ("Corte clásico — 30 min · $150").
  const texto = sel.options[sel.selectedIndex] ? sel.options[sel.selectedIndex].text : '';
  const m = texto.match(/(\d+)\s*min/);
  if (m) return Number(m[1]);
  const fijos = { 1: 30, 2: 45, 3: 20 };
  return fijos[id] || null;
}

async function recalcularFinAgendar() {
  const inicioEl = document.getElementById('fecha-hora');
  const finEl = document.getElementById('fecha-hora-fin');
  if (!inicioEl || !finEl || !inicioEl.value) return;
  await cargarDuracionesServicios();
  const duracion = duracionDelServicioSeleccionado();
  if (!duracion) return;
  const inicio = new Date(inicioEl.value);
  if (isNaN(inicio.getTime())) return;
  const fin = new Date(inicio.getTime() + duracion * 60000);
  const pad = function (n) { return String(n).padStart(2, '0'); };
  finEl.value = fin.getFullYear() + '-' + pad(fin.getMonth() + 1) + '-' + pad(fin.getDate()) +
    'T' + pad(fin.getHours()) + ':' + pad(fin.getMinutes());
}

async function cargarBarberosAgendar() {
  const sel = document.getElementById('barbero-id');
  if (!sel) return;
  try {
    const r = await apiFetch('/usuarios?rol=BARBERO');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    if (!r.datos.length) {
      sel.innerHTML = '<option value="">No hay barberos activos</option>';
      return;
    }
    sel.innerHTML = '<option value="">Elige…</option>' + r.datos.map(function (b) {
      return '<option value="' + b.id + '">' + (b.nombre || ('Barbero #' + b.id)) + '</option>';
    }).join('');
  } catch (e) {
    sel.innerHTML = '<option value="">No se pudieron cargar los barberos</option>';
  }
}

async function cargarCortesAgendar(servicioId) {
  const sel = document.getElementById('corte-id');
  if (!sel) return;
  if (!servicioId) {
    sel.innerHTML = '<option value="">Elige primero un servicio</option>';
    return;
  }
  try {
    const r = await apiFetch('/servicios/' + servicioId + '/cortes');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    if (!r.datos.length) {
      sel.innerHTML = '<option value="">Este servicio aún no tiene cortes</option>';
      return;
    }
    sel.innerHTML = '<option value="">Elige…</option>' + r.datos.map(function (c) {
      return '<option value="' + c.id + '">' + (c.nombre || ('Corte #' + c.id)) + '</option>';
    }).join('');
  } catch (e) {
    sel.innerHTML = '<option value="">No se pudieron cargar los cortes</option>';
  }
}
