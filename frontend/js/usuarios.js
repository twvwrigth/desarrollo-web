function guardarSesion(datos) {
  try {
    sessionStorage.setItem('usuarioId', datos.id);
    sessionStorage.setItem('usuarioNombre', datos.nombre || '');
    sessionStorage.setItem('usuarioRol', datos.rol || '');
  } catch (e) { /* sin sessionStorage no hay sesión */ }
}

function leerSesion() {
  try {
    var id = sessionStorage.getItem('usuarioId');
    if (!id) return null;
    return {
      id: Number(id),
      nombre: sessionStorage.getItem('usuarioNombre') || '',
      rol: sessionStorage.getItem('usuarioRol') || ''
    };
  } catch (e) { return null; }
}


// E4-permisos: lo que se ve segun el rol (la seguridad real esta en el backend).
// Usuarios: solo ADMIN. Horarios: todos. Login: solo sin sesion.
// Registro: sin sesion (registro publico) o ADMIN (el admin crea usuarios y
// barberos desde el menu); un CLIENTE o BARBERO con sesion no lo ve.
function aplicarPermisosNav() {
  var s = leerSesion();
  var esAdmin = !!(s && s.rol === 'ADMIN');
  var links = {
    'nav-login': !s,
    'nav-registro': !s || esAdmin,
    'nav-usuarios': esAdmin
  };
  Object.keys(links).forEach(function (id) {
    var a = document.getElementById(id);
    if (a) a.style.display = links[id] ? '' : 'none';
  });
}

// Pagina solo para ADMIN (usuarios.html): sin sesion -> login, sin ser admin -> index.
function exigirAdmin() {
  var s = leerSesion();
  if (!s) { window.location.href = 'login.html'; return false; }
  if (s.rol !== 'ADMIN') { window.location.href = 'index.html'; return false; }
  return true;
}

// Registro: sin ser ADMIN solo se puede crear CLIENTE (el backend ya da 403
// si intentas crear BARBERO o ADMIN, pero la UI no lo ofrece).
function ajustarRolesRegistro() {
  var select = document.getElementById('registro-rol');
  if (!select) return;
  var s = leerSesion();
  if (s && s.rol === 'ADMIN') return;
  Array.prototype.slice.call(select.options).forEach(function (op) {
    if (op.value !== 'CLIENTE') select.removeChild(op);
  });
}

function pintarSesion() {
  var el = document.getElementById('sesion-info');
  if (!el) return;
  var s = leerSesion();
  if (s && s.nombre) {
    el.textContent = 'Sesión: ' + s.nombre + (s.rol ? ' (' + s.rol + ')' : '');
  } else {
    mostrarSesion();
  }
  mostrarBotonSesion();
}

function mostrarBotonSesion() {
  var btn = document.getElementById('btn-cerrar-sesion');
  var actual = document.getElementById('sesion-actual');
  var s = leerSesion();
  if (btn) btn.style.display = s ? 'inline-block' : 'none';
  if (actual) {
    actual.textContent = s
      ? ('Sesión activa: ' + (s.nombre || ('usuario #' + s.id)) + (s.rol ? ' (' + s.rol + ')' : ''))
      : 'No hay sesión activa.';
  }
}

function cerrarSesion() {
  try {
    sessionStorage.removeItem('usuarioId');
    sessionStorage.removeItem('usuarioNombre');
    sessionStorage.removeItem('usuarioRol');
  } catch (e) { /* nada */ }
  pintarSesion();
  aplicarPermisosNav();
  mostrarMensaje('mensaje-login', 'Sesión cerrada.', false);
}

async function iniciarSesion(event) {
  event.preventDefault();
  var cuerpo = {
    email: document.getElementById('login-email').value,
    password: document.getElementById('login-password').value
  };
  try {
    var r = await apiFetch('/auth/login', { method: 'POST', body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-login', mensajeBackend(r.datos, 'No se pudo iniciar sesión') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    guardarSesion(r.datos);
    pintarSesion();
    aplicarPermisosNav();
    mostrarMensaje('mensaje-login', 'Sesión iniciada como ' + (r.datos.nombre || ('usuario #' + r.datos.id)) + ' (' + (r.datos.rol || '') + ').', false);
  } catch (e) {
    mostrarMensaje('mensaje-login', e.message, true);
  }
}

async function registrarUsuario(event) {
  event.preventDefault();
  var telefono = document.getElementById('registro-telefono').value;
  var cuerpo = {
    nombre: document.getElementById('registro-nombre').value,
    email: document.getElementById('registro-email').value,
    password: document.getElementById('registro-password').value,
    telefono: telefono || null,
    rol: document.getElementById('registro-rol').value
  };
  try {
    var r = await apiFetch('/usuarios', { method: 'POST', body: JSON.stringify(cuerpo) });
    if (!r.ok) {
      mostrarMensaje('mensaje-registro', mensajeBackend(r.datos, 'No se pudo crear el usuario') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-registro', 'Usuario creado. ID: ' + (r.datos.id || '(sin id en la respuesta)') + ' · Rol: ' + (r.datos.rol || ''), false);
    document.getElementById('form-registro').reset();
  } catch (e) {
    mostrarMensaje('mensaje-registro', e.message, true);
  }
}

var USUARIOS_CACHE = [];

async function cargarUsuarios() {
  var cuerpo = document.getElementById('cuerpo-usuarios');
  if (!cuerpo) return;
  var rol = document.getElementById('filtro-rol').value;
  var inactivos = document.getElementById('mostrar-inactivos').checked;
  var url = '/usuarios';
  var params = [];
  if (rol) params.push('rol=' + rol);
  if (inactivos) params.push('activos=false');
  if (params.length) url += '?' + params.join('&');
  try {
    var r = await apiFetch(url);
    if (!r.ok) throw new Error(mensajeBackend(r.datos, 'HTTP ' + r.status));
    USUARIOS_CACHE = r.datos;
    pintarUsuarios(r.datos);
    mostrarMensaje('mensaje-usuarios', r.datos.length + ' usuario(s).', false);
  } catch (e) {
    mostrarMensaje('mensaje-usuarios', 'No se pudieron cargar los usuarios: ' + e.message, true);
  }
}

function pintarUsuarios(usuarios) {
  var cuerpo = document.getElementById('cuerpo-usuarios');
  if (!usuarios.length) {
    cuerpo.innerHTML = '<tr><td colspan="7">No hay usuarios con ese filtro.</td></tr>';
    return;
  }
  cuerpo.innerHTML = usuarios.map(function (u) {
    var estado = u.activo ? 'Activo' : 'Inactivo';
    var acciones = '<div class="acciones">' +
      (u.activo
        ? '<button type="button" class="btn btn-pequeno" onclick="pedirEditarUsuario(' + u.id + ')">Editar</button> ' +
          '<button type="button" class="btn btn-pequeno btn-peligro" onclick="desactivarUsuario(' + u.id + ')">Desactivar</button>'
        : '<button type="button" class="btn btn-pequeno" onclick="reactivarUsuario(' + u.id + ')">Reactivar</button>') +
      '</div>';
    return '<tr><td>' + u.id + '</td><td>' + (u.nombre || '') + '</td><td>' + (u.email || '') + '</td>' +
      '<td>' + (u.telefono || '') + '</td><td>' + (u.rol || '') + '</td><td>' + estado + '</td><td>' + acciones + '</td></tr>';
  }).join('');
}

function pedirEditarUsuario(id) {
  var u = USUARIOS_CACHE.find(function (x) { return x.id === id; });
  if (!u) return;
  document.getElementById('editar-usuario-id').value = u.id;
  document.getElementById('editar-usuario-nombre').value = u.nombre || '';
  document.getElementById('editar-usuario-email').value = u.email || '';
  document.getElementById('editar-usuario-telefono').value = u.telefono || '';
  document.getElementById('editar-usuario-password').value = '';
  document.getElementById('seccion-editar-usuario').style.display = 'block';
  document.getElementById('seccion-editar-usuario').scrollIntoView({ behavior: 'smooth' });
}

async function guardarEdicionUsuario(event) {
  event.preventDefault();
  var id = document.getElementById('editar-usuario-id').value;
  var cambios = {};
  var nombre = document.getElementById('editar-usuario-nombre').value;
  var email = document.getElementById('editar-usuario-email').value;
  var telefono = document.getElementById('editar-usuario-telefono').value;
  var password = document.getElementById('editar-usuario-password').value;
  if (nombre) cambios.nombre = nombre;
  if (email) cambios.email = email;
  if (telefono) cambios.telefono = telefono;
  if (password) cambios.password = password;
  if (!Object.keys(cambios).length) {
    mostrarMensaje('mensaje-editar-usuario', 'Nada que cambiar.', true);
    return;
  }
  try {
    var r = await apiFetch('/usuarios/' + id, { method: 'PUT', body: JSON.stringify(cambios) });
    if (!r.ok) {
      mostrarMensaje('mensaje-editar-usuario', mensajeBackend(r.datos, 'No se pudo editar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-editar-usuario', 'Usuario #' + id + ' actualizado.', false);
    document.getElementById('seccion-editar-usuario').style.display = 'none';
    cargarUsuarios();
  } catch (e) {
    mostrarMensaje('mensaje-editar-usuario', e.message, true);
  }
}

async function desactivarUsuario(id) {
  if (!confirm('¿Desactivar el usuario #' + id + '?')) return;
  try {
    var r = await apiFetch('/usuarios/' + id, { method: 'DELETE' });
    if (r.status === 204) {
      mostrarMensaje('mensaje-usuarios', 'Usuario #' + id + ' desactivado.', false);
      cargarUsuarios();
      return;
    }
    mostrarMensaje('mensaje-usuarios', mensajeBackend(r.datos, 'No se pudo desactivar') + ' (HTTP ' + r.status + ')', true);
  } catch (e) {
    mostrarMensaje('mensaje-usuarios', e.message, true);
  }
}

async function reactivarUsuario(id) {
  try {
    var r = await apiFetch('/usuarios/' + id + '/estado', { method: 'PUT', body: JSON.stringify({ activo: true }) });
    if (!r.ok) {
      mostrarMensaje('mensaje-usuarios', mensajeBackend(r.datos, 'No se pudo reactivar') + ' (HTTP ' + r.status + ')', true);
      return;
    }
    mostrarMensaje('mensaje-usuarios', 'Usuario #' + id + ' reactivado.', false);
    cargarUsuarios();
  } catch (e) {
    mostrarMensaje('mensaje-usuarios', e.message, true);
  }
}

// El nav se ajusta al rol en cuanto carga cualquiera de mis paginas, y se
// muestra el boton de cerrar sesion si hay sesion (tambien se conecta su clic).
aplicarPermisosNav();
mostrarBotonSesion();
var btnCerrar = document.getElementById('btn-cerrar-sesion');
if (btnCerrar) btnCerrar.addEventListener('click', cerrarSesion);
