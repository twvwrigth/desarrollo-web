# Respuesta de la IA — Uso_01

- Qué es una API
Es un programa que se queda escuchando en un puerto (como el 8080 o el 8081) y responde peticiones HTTP. Otro programa (el navegador, curl o el JavaScript de tu frontend) le manda una petición con un verbo y una ruta, y la API le devuelve datos, normalmente en JSON. No hay pantallas: solo peticiones y respuestas.
DemoApplication.java — el arranque. @SpringBootApplication y el main prenden el servidor (Tomcat) y escanean el paquete buscando controladores, entidades y repositorios.
SaludarControlador.java — la puerta de entrada. @RestController dice "esto responde datos, no páginas". Cada método es un endpoint: un verbo + una ruta. @GetMapping("/saludos") responde a GET /saludos, @PostMapping("/nombramientos") a POST /nombramientos, y así con PUT y DELETE.
Saludador.java — la entidad. @Entity dice que es una tabla de la base de datos, @Id con @GeneratedValue es el id automático. Cuando el controlador devuelve un Saludador, Spring lo convierte solo a JSON: {"id": 1, "contenido": "Erick"}.
ISaludador.java — el repositorio. Extiende CrudRepository<Saludador, Integer> y sin escribir ni una línea de SQL ya tienes save(), findAll(), findById() y deleteById(). Spring Data JPA lo implementa por ti.
plantillaControlador.java — el contraste. Este usa @Controller (sin Rest) y devuelve el nombre de una vista ("pagina"), o sea una página HTML del servidor, no datos. Eso ya no es API REST, es la forma vieja de hacer páginas.
Cómo funciona una petición, paso a paso



curl o fetch  -- GET /saludar/Erick -->  Tomcat  -->  SaludarControlador.met3("Erick")
                                                          | crea Saludador("Erick")
                                                          | isaludador.save(entidad) --> MySQL
                                          <-- JSON {"id":1,"contenido":"Erick"} --
Tres formas de mandar datos que tu ejemplo ya muestra:

En la ruta: GET /saludar/Erick — {nombre} se captura con @PathVariable.
En la query: GET /query1?nombre=Erick&paterno=Gutiérrez — se capturan con @RequestParam.
En el cuerpo: POST /crearSaludador con JSON {"contenido":"Hola"} — llega como objeto con @RequestBody.
Y el estado de tu ejemplo: GET /saludos devuelve "Hola mundo!" + nombre. Al inicio nombre es null, así que sale "Hola mundo!null". Si haces POST /nombramientos, el campo queda en "Erick"; con PUT queda "Fulgencio" y con DELETE vuelve a null. Es un campo del controlador guardado en memoria: sirve para la clase, pero se pierde al reiniciar y no es seguro con varios usuarios a la vez. Por eso en la barbería los datos van en MySQL, no en campos.

Cómo hacer una tú

Crea el proyecto en Spring Initializr con las dependencias Spring Web, Spring Data JPA y el driver de tu base (MySQL o H2 para pruebas).
Escribe la entidad (@Entity, con su @Id).
Escribe el repositorio (interface X extends CrudRepository<Entidad, Integer>).
Escribe el controlador (@RestController + @GetMapping/@PostMapping/etc.), inyectando el repositorio con @Autowired.
En application.properties pon el puerto y la conexión a la base (server.port, spring.datasource.url...).
Corre con ./mvnw spring-boot:run y prueba con curl o el navegador: GET http://localhost:8080/saludos.
- Reglas: nombre de servicio único (409), cita siempre PENDIENTE al crear, 409 por traslape del mismo barbero, total de cotización calculado en el servidor.
- Guía y script de cURL (`PRUEBAS-CURL-Erick-E2.md`, `pruebas-curl-erick.sh`).
- Verificación: `mvn test` BUILD SUCCESS, 2 pruebas, 2026-11-13 22:36.