# Requerimientos — Sistema Gestor de Citas para Barbería

**Proyecto final:** Desarrollo Web (LISTI, UV) · Agosto 2026 - Enero 2027
**Integrantes:** José Emmanuel Marínez Galicia, Erick Iray Erick Iray Gutiérrez del Ángel, José Luis Lucas Landa
**Docente:** José Rafael Rojano Caceres
**Repositorio:** desarrollo-web

## 1. Descripción
Sistema web para gestionar las citas de una barbería. Permite a los clientes consultar servicios y cortes, cotizar, agendar citas con el barbero y dejar reseñas. El administrador y los barberos gestionan servicios, horarios y agenda. Como módulo adicional, el cliente puede "probarse" un corte desde la cámara, que superpone el corte elegido sobre su rostro.

## 2. Problema que resuelve
Las barberías suelen agendar por mensaje o llamada, lo que genera citas traslapadas, olvidos, falta de control de horarios y poca información para el cliente (precios, cortes disponibles, opiniones). Además, el cliente decide su corte sin poder visualizarlo.

## 3. Justificación
Centralizar citas, catálogo y reseñas en un solo sistema reduce errores de agenda, da transparencia en precios y mejora la experiencia del cliente. El proyecto integra frontend, backend REST y base de datos relacional

## 4. Alcance
**Base** usuarios y roles, servicios, catálogo de cortes, horarios y disponibilidad, citas, cotizaciones y reseñas.
**Extra:** probador virtual con cámara

## 5. Actores
| Cliente | Se registra, cotiza, agenda citas y deja reseñas. |
| Barbero | Define su horario, consulta su agenda y actualiza el estado de las citas. |
| Administrador | Gestiona barberos, servicios y catálogo de cortes. |

## 6. Requerimientos funcionales

| ID / Categoría | Descripción | Rol | Tipo |
|---|---|---|---|
| **Usuarios y acceso** | | | |
| RF-01 | Registrarse como cliente (nombre, email, contraseña, teléfono). | Cliente | Base |
| RF-02 | Iniciar y cerrar sesión. | Todo tipo de usuario | Base |
| RF-03 | Consultar y editar su perfil. | Todo tipo de usuario | Base |
| RF-04 | Dar de alta, edición y baja de barberos. | Administrador | Base |
| **Servicios** | | | |
| RF-05 | Consultar el catálogo de servicios con duración y precio. | Todo tipo de usuario | Base |
| RF-06 | Alta, edición y baja de servicios. | Administrador | Base |
| **Catálogo de cortes** | | | |
| RF-07 | Consultar cortes (imagen y descripción), con filtro por servicio o nombre. | Todo tipo de usuario | Base |
| RF-08 | Dar de alta, edición y baja de cortes. | Administrador | Base |
| **Horarios y disponibilidad** | | | |
| RF-09 | Definir el horario semanal de trabajo. | Barbero, Administrador | Base |
| RF-10 | Consultar horarios libres por barbero, servicio y fecha. | Cliente | Base |
| **Citas** | | | |
| RF-11 | Agendar cita (barbero, servicio, fecha y hora, corte opcional, notas). | Cliente | Base |
| RF-12 | Consultar sus citas. | Cliente | Base |
| RF-13 | Consultar la agenda por día. | Barbero, Administrador | Base |
| RF-14 | Reprogramar una cita. | Cliente | Base |
| RF-15 | Cancelar una cita. | Cliente, Administrador | Base |
| RF-16 | Cambiar el estado de una cita (confirmar, completar). | Barbero, Administrador | Base |
| **Cotizaciones** | | | |
| RF-17 | Generar una cotización eligiendo servicios y cantidades (el total se calcula solo). | Cliente | Base |
| RF-18 | Consultar sus cotizaciones. | Cliente | Base |
| RF-19 | Aceptar, rechazar o eliminar una cotización pendiente. | Cliente | Base |
| **Reseñas** | | | |
| RF-20 | Reseñar una cita completada (calificación 1-5 y comentario). | Cliente | Base |
| RF-21 | Consultar reseñas y calificación promedio por barbero. | Todo tipo de usuario | Base |
| RF-22 | Editar o eliminar su propia reseña. | Cliente | Base |
| **Probador virtual** | | | |
| RF-23 | Activar la cámara, detectar el rostro y superponer el corte elegido del catálogo; permitir capturar la imagen. | Cliente | Extra |
| RF-24 | Agendar cita desde el probador con el corte preseleccionado. | Cliente | Extra |

## 7. Reglas de negocio

| ID | Regla |
|---|---|
| RN-01 | Una cita no puede traslaparse con otra del mismo barbero. |
| RN-02 | Solo se agenda dentro del horario del barbero y en fecha futura. |
| RN-03 | La hora de fin de la cita es la de inicio más la duración del servicio. |
| RN-04 | Estados de cita: PENDIENTE → CONFIRMADA → COMPLETADA. Se puede pasar a CANCELADA desde PENDIENTE o CONFIRMADA. Una cita COMPLETADA o CANCELADA ya no se modifica. |
| RN-05 | Solo se reseña una cita COMPLETADA, una reseña por cita, y solo el cliente de esa cita. |
| RN-06 | Total de cotización = suma de (cantidad × precio unitario). El precio unitario se guarda al momento de cotizar. |
| RN-07 | Usuarios, servicios y cortes se dan de baja de forma lógica (`activo = false`) para conservar el historial. |
| RN-08 | El email es único por usuario. |
| RN-09 | Las contraseñas se guardan con hash (BCrypt), nunca en texto plano. |

## 8. Requerimientos no funcionales

| ID | Requerimiento |
|---|---|
| RNF-01 | El proyecto se desarrolla y ejecuta desde WSL y se puede clonar y ejecutar en otro entorno WSL siguiendo el README. |
| RNF-02 | Arquitectura de 3 capas: frontend (HTML, CSS, JS con API Fetch), backend (Java + Spring Boot, API REST en JSON) y base de datos. |
| RNF-03 | Persistencia con Hibernate/JPA sobre MySQL; el esquema está en `database/esquema.sql`. |
| RNF-04 | Respuestas con códigos HTTP correctos y formato de error consistente. |
| RNF-05 | Validación de datos en frontend y en backend. |
| RNF-06 | Interfaz responsiva básica (celular y escritorio). |
| RNF-07 | Cada integrante crea las pruebas de sus módulos (cURL y pruebas automatizadas). |
| RNF-08 | Trazabilidad: commits desde la cuenta de cada integrante y un release de GitHub por hito. |

## 9. Tecnologías

HTML, CSS, JavaScript (Fetch) · Java + Spring Boot · Hibernate/JPA · MySQL · WSL, Git y GitHub · VS Code.
