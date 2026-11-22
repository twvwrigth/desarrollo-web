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
