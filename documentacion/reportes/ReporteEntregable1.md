# Reporte de Avance del Proyecto

**Equipo:** José Emmanuel Martínez Galicia (@tachi), Erick Iray Gutiérrez del Ángel, José Luis Lucas Landa
**Fecha:** 11/11/2026
**Número de reporte:** 1
**Repositorio GitHub:** https://github.com/twvwrigth/desarrollo-web

---

# 1. Actividades realizadas

- Creación del repositorio `desarrollo-web` con README y webhook.
- Definición de requerimientos funcionales (RF-01 a RF-24), no funcionales (RNF-01 a RNF-08) y reglas de negocio (RN-01 a RN-09).
- Diseño del modelo de datos con diagrama entidad-relación en PlantUML (8 entidades).
- Script `esquema.sql` para MySQL, generado a partir del diagrama.
- Definición de endpoints REST (módulos de usuarios, servicios, cortes, horarios, citas, cotizaciones y reseñas), con códigos HTTP y ejemplos de JSON.
- Distribución de responsabilidades por integrante y por entregable.

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
|------------|------------------------|-----------------|
| Diagrama ER en PlantUML (`diagrama.png`) | José Emmanuel | `23084e5` |
| Script `esquema.sql` | José Emmanuel | `805f40f` |
| Requerimientos (`requerimientos.md`) | Erick Iray | `https://github.com/<usuario>/desarrollo-web/commit/<hash>` |
| Endpoints REST y reparto (`endpoints.md`) | José Luis | `9b12c58` |

---

# 3. Problemas encontrados

ninguno grais dios

---

# 4. Avance funcional

- **Operativo:** ninguna funcionalidad ejecutable aún. Este reporte corresponde a la fase de diseño.
- **Diseño completo:** requerimientos, modelo de datos, script SQL, endpoints y distribución de responsabilidades.
- **Pendiente:** todo el backend y el frontend.

---

# 5. Uso de IA generativa

*(Completar con el uso real del equipo. Registrar cada herramienta, actividad y forma de verificación. Si no se usó IA, indicarlo expresamente.)*

- **Herramienta utilizada:**
- **Actividad para la que se utilizó:**
- **Código o contenido generado:**
- **Modificaciones realizadas por el equipo:**
- **Forma en que se verificó el resultado:**

---

# 6. Participación individual

- **José Emmanuel:** modelo de datos en PlantUML, `esquema.sql`.
- **Erick Iray:** `requerimientos.md`.
- **José Luis:** `endpoints.md` y `responsabilidades.md`.

---

# 7. Próximas actividades

- **Jose Luis** proyecto Spring Boot con conexión a MySQL; POST y GET de usuarios, horarios; pruebas con cURL (E2).
- **Erick Iray:** entidades y repositorios de servicios, cortes y cotizaciones; POST y GET de citas; pruebas con cURL (E2).
- **José Emmanuel:** POST y GET de reseñas; pruebas con cURL; esqueleto HTML/CSS del frontend.
- **Equipo:** crear el release V1 "Diseño inicial" con el enlace en la actividad; confirmar con el docente la autenticación y las fechas de entrega.

---

## Observaciones adicionales
- El reporte cubre solo la fase de diseño; las evidencias de código comenzarán en el E2.