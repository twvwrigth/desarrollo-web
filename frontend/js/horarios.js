
var DIA_SEMANA = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];


async function cargarBarberosHorarios() {
  var select = document.getElementById('horario-barbero-id');
  if (!select) return;
  try {
    var r = await apiFetch('/usuarios?rol=BARBERO');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    if (!r.datos.length) {
      select.innerHTML = '<option value="">No hay barberos activos</option>';
      return;
    }
    select.innerHTML = r.datos.map(function (b) {
      return '<option value="' + b.id + '">' + (b.nombre || ('Barbero #' + b.id)) + ' (#' + b.id + ')</option>';
    }).join('');
  } catch (e) {
    mostrarMensaje('mensaje-lista-horarios', 'No se pudieron cargar los barberos: ' + e.message, true);
  }
}


var HORARIOS_CACHE = [];

async function cargarHorarios() {
  var barberoId = document.getElementById('horario-barbero-id').value;
  var cuerpo = document.getElementById('cuerpo-horarios');
  if (!cuerpo || !barberoId) return;
  try {
    var r = await apiFetch('/barberos/' + barberoId + '/horarios');
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    HORARIOS_CACHE = r.datos;
    pintarHorarios(r.datos);
    mostrarMensaje('mensaje-lista-horarios', r.datos.length + ' horario(s).', false);
  } catch (e) {
    mostrarMensaje('mensaje-lista-horarios', 'No se pudieron cargar los horarios: ' + e.message, true);
  }
}

function pintarHorarios(horarios) {
  var cuerpo = document.getElementById('cuerpo-horarios');
  if (!horarios.length) {
    cuerpo.innerHTML = '<tr><td colspan="4">Este barbero aún no tiene horarios.</td></tr>';
    return;
  }
  cuerpo.innerHTML = horarios.map(function (h) {
    var dia = DIA_SEMANA[h.diaSemana] || ('Día ' + h.diaSemana);
    var inicio = h.horaInicio ? String(h.horaInicio).slice(0, 5) : '';
    var fin = h.horaFin ? String(h.horaFin).slice(0, 5) : '';
    return '<tr><td>' + dia + '</td><td>' + inicio + '</td><td>' + fin + '</td>' +
      '<td><div class="acciones">' +
      '<button type="button" class="btn btn-pequeno" onclick="pedirEditarHorario(' + h.id + ')">Editar</button> ' +
      '<button type="button" class="btn btn-pequeno btn-peligro" onclick="eliminarHorario(' + h.id + ')">Eliminar</button>' +
      '</div></td></tr>';
  }).join('');
}

async function crearHorario(event) {
  event.preventDefault();
  var barberoId = document.getElementById('horario-barbero-id').value;
  if (!barberoId) {
    mostrarMensaje('mensaje-horario', 'Elige un barbero primero.', true);
    return;
  }
  var cuerpo = {
    diaSemana: Number(document.getElementById('horario-dia').value),
    horaInicio: document.getElementById('horario-inicio').value,
    horaFin: document.getElementById('horario-fin').value
  };
  try {
    var r = await apiFetch('/barberos/' + barberoId + '/horarios', { method: 'POST', body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-horario', mensajeBackend(r.datos, 'No se pudo crear el horario') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-horario', 'Horario creado. ID: ' + (r.datos.id || '(sin id en la respuesta)'), false);
    document.getElementById('form-horario').reset();
    cargarHorarios();
  } catch (e) {
    mostrarMensaje('mensaje-horario', e.message, true);
  }
}

function pedirEditarHorario(id) {
  var h = HORARIOS_CACHE.find(function (x) { return x.id === id; });
  if (!h) return;
  document.getElementById('editar-horario-id').value = h.id;
  document.getElementById('editar-horario-dia').value = h.diaSemana;
  document.getElementById('editar-horario-inicio').value = h.horaInicio ? String(h.horaInicio).slice(0, 5) : '';
  document.getElementById('editar-horario-fin').value = h.horaFin ? String(h.horaFin).slice(0, 5) : '';
  document.getElementById('seccion-editar-horario').style.display = 'block';
  document.getElementById('seccion-editar-horario').scrollIntoView({ behavior: 'smooth' });
}

async function guardarEdicionHorario(event) {
  event.preventDefault();
  var id = document.getElementById('editar-horario-id').value;
  var cuerpo = {
    diaSemana: Number(document.getElementById('editar-horario-dia').value),
    horaInicio: document.getElementById('editar-horario-inicio').value,
    horaFin: document.getElementById('editar-horario-fin').value
  };
  try {
    var r = await apiFetch('/horarios/' + id, { method: 'PUT', body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-editar-horario', mensajeBackend(r.datos, 'No se pudo editar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-editar-horario', 'Horario #' + id + ' actualizado.', false);
    document.getElementById('seccion-editar-horario').style.display = 'none';
    cargarHorarios();
  } catch (e) {
    mostrarMensaje('mensaje-editar-horario', e.message, true);
  }
}

async function eliminarHorario(id) {
  if (!confirm('¿Eliminar el horario #' + id + '?')) return;
  try {
    var r = await apiFetch('/horarios/' + id, { method: 'DELETE' });
    if (r.status === 204) {
      mostrarMensaje('mensaje-lista-horarios', 'Horario #' + id + ' eliminado.', false);
      cargarHorarios();
      return;
    }
    mostrarMensaje('mensaje-lista-horarios', mensajeBackend(r.datos, 'No se pudo eliminar') + ' (HTTP ' + r.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-lista-horarios', e.message, true);
  }
}
