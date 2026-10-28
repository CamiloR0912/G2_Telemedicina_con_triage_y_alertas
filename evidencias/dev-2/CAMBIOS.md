# Dev 2 · Triage y alertas → `triage-alertas-service`

**Rol en la comunicación:** productor **RabbitMQ** (alerta de urgencia alta a enfermería y a Agenda) y
productor **Kafka** con **Transactional Outbox** (evento `triage.completados` para Historia clínica).

Puerto REST: `8082`. Paquete raíz: `com.telemedicina.triagealertasservice`.

## Paso 1 · Crear el proyecto y mover el subdominio

1. Proyecto Maven nuevo `triage-alertas-service`. Dependencias en
   [pom.xml](triage-alertas-service/pom.xml): `webmvc`, `data-jpa`, `h2`, **`kafka`**, **`amqp`**.
2. Se copió el paquete `triagealertas` completo (con sus tests) de la rama `hexagonal`, cambiando
   `com.telemedicina.telemedicina.triagealertas` → `com.telemedicina.triagealertasservice.triagealertas`.
3. Clase principal `TriageAlertasServiceApplication` con `@EnableScheduling` (lo necesita el poller del outbox).
4. [application.yml](triage-alertas-service/src/main/resources/application.yml): puerto 8082, H2 en
   archivo (`./data/triage-alertas`, la tabla outbox debe sobrevivir reinicios), Kafka en localhost:9092
   con `max.block.ms: 5000` (con Kafka caído el poller no se bloquea 60 s), RabbitMQ en localhost:5672.

## Paso 2 · Notificar a enfermería por RabbitMQ (HU-04)

El puerto `NotificadorEnfermeria` ya existía en la versión hexagonal, con un adaptador que solo escribía
en el log. Ahora tiene un adaptador real. **El núcleo casi no cambió**: esa es la ganancia del hexágono.

- Puerto: `notificar(...)` ahora también recibe el `citaId`, porque Agenda lo necesita para priorizar la cita.
- Se borró `NotificadorEnfermeriaLog` y se creó `infraestructura/salida/notificacion/NotificadorEnfermeriaRabbit`:
  publica un JSON (`alertaId`, `cuestionarioId`, `pacienteId`, `citaId`, `nivelUrgencia`, `fechaGeneracion`)
  en `alertas.exchange` con la ruta `alerta.urgencia.alta`.
- [RabbitConfig](triage-alertas-service/src/main/java/com/telemedicina/triagealertasservice/triagealertas/infraestructura/configuracion/RabbitConfig.java):
  exchange de tipo **topic** y la cola `enfermeria.alertas.queue` (ruta `alerta.#`). Agenda engancha su
  propia cola al mismo exchange: **un envío, dos colas**.
- Envío **directo, sin outbox**: la alerta debe llegar "en segundos". Si RabbitMQ está caído,
  `CuestionarioTriageService` ya capturaba el fallo: la alerta queda activa y `notificada: false`, visible en
  `GET /api/alertas/activas` (HU-09).

## Paso 3 · Evento `triage.completados` con Kafka y Transactional Outbox

Historia clínica necesita saber qué citas tienen triage completado (regla "no puede iniciarse una consulta
sin un triage completado"). Ese dato no se puede perder, así que se usa outbox.

- Puerto secundario nuevo: `aplicacion/PublicadorEventosTriage.triageCompletado(cuestionario)`.
- `CuestionarioTriageService.responder(...)`:
  - llama al puerto justo después de guardar el cuestionario;
  - lleva `@jakarta.transaction.Transactional`: el cuestionario y su evento se confirman juntos o ninguno.
    Es la anotación estándar de Jakarta, no la de Spring, así el paquete `aplicacion` sigue sin importar Spring.
- `TriageAlertasConfig` pasa el nuevo puerto al constructor del servicio.
- Paquete nuevo `infraestructura/salida/mensajeria/`:
  - `OutboxEvent` + `OutboxEventRepository`: tabla `outbox_event` (tópico, clave, payload, publicado).
  - `OutboxPublicadorEventosTriage`: implementa el puerto **escribiendo en la tabla**, no en Kafka.
    Payload: `cuestionarioId`, `citaId`, `pacienteId`, `nivelUrgencia`. Clave = `citaId`.
  - `OutboxPoller`: cada 2 s envía los pendientes a Kafka y los marca publicados; si Kafka falla, se
    detiene y reintenta en la siguiente vuelta (conserva el orden).

Prueba que justifica el patrón (verificada):

```powershell
docker stop telemedicina-kafka
# responder un triage → 201; el log dice "no se pudo publicar el evento N (se reintentará)"
docker start telemedicina-kafka
# segundos después: "Outbox: evento N publicado en triage.completados"
```

## Paso 4 · Tests

- `NotificadorEnfermeriaFalso` actualizado a la nueva firma.
- `CuestionarioTriageServiceConFalsoTest`: el publicador de eventos es una lambda que guarda los `citaId`;
  test nuevo `cadaTriageRespondidoPublicaElEventoDeTriageCompletado`.
- `.\mvnw.cmd test` → 40 tests en verde.

## Archivos

| Estado | Archivo |
|---|---|
| Nuevo | `pom.xml`, `application.yml`, `TriageAlertasServiceApplication` |
| Nuevo | `aplicacion/PublicadorEventosTriage` |
| Nuevo | `infraestructura/configuracion/RabbitConfig`, `infraestructura/salida/notificacion/NotificadorEnfermeriaRabbit` |
| Nuevo | `infraestructura/salida/mensajeria/OutboxEvent`, `OutboxEventRepository`, `OutboxPublicadorEventosTriage`, `OutboxPoller` |
| Modificado | `aplicacion/NotificadorEnfermeria` (+ citaId), `aplicacion/CuestionarioTriageService`, `infraestructura/configuracion/TriageAlertasConfig` |
| Borrado | `infraestructura/salida/notificacion/NotificadorEnfermeriaLog` |
| Modificado | `test/.../NotificadorEnfermeriaFalso`, `test/.../CuestionarioTriageServiceConFalsoTest` |

Commits sugeridos: `feat(triage): microservicio triage-alertas-service`,
`feat(triage): notificar alertas a enfermería por RabbitMQ`,
`feat(triage): evento triage.completados con Kafka y Transactional Outbox`.
