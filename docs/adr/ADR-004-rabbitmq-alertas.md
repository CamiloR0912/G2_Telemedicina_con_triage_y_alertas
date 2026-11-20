# ADR-004 · RabbitMQ para alertas de urgencia alta, con envío directo (sin outbox)

- **Estado:** Aceptada · implementada
- **Fecha:** 2026-11-11
- **Evidencia en el código:** `NotificadorEnfermeriaRabbit` (Triage publica en `alertas.exchange` con la clave `alerta.urgencia.alta`), `RabbitConfig` y `AlertaUrgenteListener` (Agenda consume `agenda.alertas-urgentes.queue` y prioriza la cita).

## Contexto

HU-03 y HU-04: cuando el triage clasifica la urgencia como alta, se genera una alerta clínica que debe
llegar **en segundos** y la cita del paciente se prioriza. Es una notificación: si se pierde, la alerta
sigue visible en el panel `GET /api/alertas/activas` (HU-09).

## Decisión

Triage publica la alerta **directamente** en un *topic exchange* de RabbitMQ. Cada interesado declara
su propia cola y la enlaza al exchange; hoy Agenda lo hace para priorizar la cita. Si RabbitMQ está
caído, el envío falla, la alerta queda activa con `notificada: false` y sigue en el panel. En Agenda,
un mensaje que no se puede procesar (cita inexistente o cancelada) se descarta y no se reencola
(`default-requeue-rejected: false`), para evitar un bucle infinito.

**Por qué asíncrona:** Triage no necesita la respuesta de Agenda para terminar el cuestionario, y
Agenda no debe depender de que Triage esté arriba.

## Alternativas consideradas

1. **Kafka con outbox (como en ADR-003):** garantiza la entrega, pero añade hasta 2 s de latencia por el
   poller, y las colas de trabajo con enrutamiento por clave son más naturales en RabbitMQ.
2. **Llamada REST de Triage a Agenda para priorizar:** inmediata, pero acopla Triage a la disponibilidad
   de Agenda y obliga a Triage a conocer a cada consumidor.
3. **Notificación por WebSocket o push al personal de enfermería:** fuera del alcance de las lecciones 3
   a 6 (queda como **Propuesta** en ARQUITECTURA.md).

## Consecuencias

- **Positivas:** latencia muy baja; agregar un nuevo consumidor (por ejemplo, enfermería) solo requiere
  enlazar una cola nueva, sin tocar Triage.
- **Negativas:**
  - **Si RabbitMQ está caído, la alerta no se notifica ni se reintenta**: queda solo en el panel, y la
    cita no se prioriza automáticamente.
  - El sistema opera **dos brokers** (Kafka y RabbitMQ), con más configuración, más memoria y más
    puntos de fallo en `docker-compose.yml`.
  - Un mensaje descartado en Agenda no queda en ninguna cola de errores para revisión posterior.
