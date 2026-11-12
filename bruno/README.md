# Pruebas de los microservicios con Bruno

## Arranque con Docker

Desde la raiz del repositorio:

```bash
docker compose up -d --build --wait --wait-timeout 240
docker compose ps
```

Docker compila los servicios, Eureka y el Gateway con Maven y Java 25; no necesitas
instalarlos en tu equipo. La primera compilacion descarga las imagenes y dependencias.

| Servicio | Direccion local |
|---|---|
| **API Gateway (entrada unica)** | http://localhost:8000 |
| Eureka (panel del registro) | http://localhost:8761 |
| Agenda HTTP | http://localhost:8081 |
| Agenda gRPC | localhost:9090 |
| Triage y alertas | http://localhost:8082 |
| Historia clinica | http://localhost:8083 |
| Kafka | localhost:9092 |
| RabbitMQ AMQP | localhost:5672 |
| RabbitMQ administracion | http://localhost:15672 |

RabbitMQ usa `telemedicina` / `telemedicina-local` (credenciales de desarrollo).
Dentro de Docker, los servicios se conectan usando los nombres de Compose.
Kafka anuncia una direccion interna y otra para clientes del equipo anfitrion.
Los healthchecks verifican los brokers y los puertos de los servicios; las pruebas Bruno
validan que las operaciones y la comunicacion entre servicios realmente funcionen.

Para detener: `docker compose down`. Las bases H2 y los brokers conservan sus datos en
volumenes separados. `docker compose down -v` elimina TODOS esos datos de desarrollo.
Si vienes de una version anterior a la Saga, ejecuta una vez `docker compose down -v`: la
tabla `citas` antigua no conoce el estado `EN_CANCELACION` y H2 rechazaria las cancelaciones.
Las carpetas `data/` existentes fuera de Docker no se importan ni se modifican.
No arranques simultaneamente servicios locales que ocupen los mismos puertos.

## Ejecutar las pruebas

En Bruno abre esta carpeta como coleccion, selecciona el entorno **local** y ejecuta
**Run** sobre la coleccion o una carpeta completa. Respeta el orden `seq`: las peticiones
comparten identificadores creados durante la ejecucion.

Con Node.js y npm:

```bash
cd bruno
npx --yes @usebruno/cli run --env local
npx --yes @usebruno/cli run agenda --env local
npx --yes @usebruno/cli run historia-clinica --env local
npx --yes @usebruno/cli run triage-alertas --env local
npx --yes @usebruno/cli run eureka-gateway --env local
npx --yes @usebruno/cli run saga-cancelacion --env local
```

Todas las peticiones entran por el Gateway: `agendaUrl`, `triageUrl` e `historiaUrl` apuntan a
http://localhost:8000 y el Gateway decide el servicio por la ruta (`lb://` resuelto en Eureka).
Para probar un servicio directo, cambia esa variable por su puerto (8081, 8082 u 8083).
Justo despues de arrancar, Eureka puede tardar unos segundos en mostrar un servicio; si el
Gateway responde 503, espera y repite.
La coleccion corresponde a los microservicios, no a la aplicacion antigua `telemedicina/`.

## Casos y precondiciones

- Agenda: creacion, solapamientos, franja contigua, validaciones, consulta, agenda del dia,
  reprogramacion y cancelacion. Cancelar responde 202 (`EN_CANCELACION`) y la peticion espera
  hasta 30 s a que la Saga termine en `CANCELADA`. La cita contigua se cancela antes de continuar.
  La agenda del dia usa `/api/medicos/{medicoId}/agenda?dia=...`.
- Historia: genera medico y paciente nuevos, agenda su cita, registra la historia,
  comprueba acceso y auditoria por gRPC, rechaza una consulta sin triage y completa
  el triage de esa misma cita antes de probar consultas y sus errores.
- Kafka: antes de registrar la consulta, se sondea Historia hasta 30 veces, con un segundo
  entre intentos. El sondeo usa diagnostico vacio para no guardar consultas; espera el
  cambio de 409 por triage pendiente a 400 por diagnostico obligatorio. Cualquier otro
  error falla inmediatamente. Consultar triage en Triage no prueba que Historia recibio el evento.
- Triage y alertas: clasificacion baja/media/alta, validaciones, consulta, panel y atencion.
  Las pruebas de `notificada: true` necesitan RabbitMQ disponible.
- Eureka y Gateway: los cuatro clientes aparecen UP en Eureka y una peticion al puerto 8000
  llega al servicio correcto.
- Saga de cancelacion (Kafka + outbox en ambos lados):

  ```
  POST /api/citas/{id}/cancelar  ->  Agenda: EN_CANCELACION + evento en outbox
     --(citas.en-cancelacion)-->  Triage: ¿la cita tiene una alerta urgente sin atender?
     <--(citas.cancelacion-confirmada)--  no: Agenda pasa la cita a CANCELADA y libera el cupo
     <--(citas.cancelacion-rechazada)---  si: Agenda COMPENSA y la cita vuelve a su estado anterior
  ```

  La carpeta agenda una cita, responde un triage de urgencia alta, intenta cancelar
  (rechazada y compensada a `AGENDADA`), enfermeria atiende la alerta y la cancelacion
  final se confirma. Si Triage esta caido, el evento espera en Kafka y la cita sigue
  `EN_CANCELACION` ocupando su cupo; Agenda sigue agendando con normalidad.

Los datos son persistentes. Cada corrida genera identificadores nuevos para evitar
conflictos entre ejecuciones; los datos de prueba se acumulan en los volumenes.
La coleccion no simula caidas de brokers ni comprueba por si sola la entrega a un consumidor
externo de enfermeria.
