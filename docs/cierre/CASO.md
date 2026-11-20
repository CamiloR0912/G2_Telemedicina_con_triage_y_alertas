# Caso de estudio · Telemedicina con triage y alertas clínicas

## Problema

Una red de atención ofrece consultas médicas por videollamada. Antes, los pacientes agendaban sin
ninguna valoración previa: un paciente con síntomas graves podía esperar días por una cita, y el
personal de enfermería se enteraba tarde. Además, cualquier médico podía abrir cualquier historia
clínica sin dejar registro, lo que incumple la reserva de los datos de salud.

## Actores

- **Paciente:** agenda, cancela o reprograma su cita y responde el cuestionario de triage.
- **Médico:** consulta su agenda del día, abre la historia clínica de sus pacientes y registra la consulta.
- **Personal de enfermería:** recibe las alertas de urgencia alta y las atiende desde el panel.

## Historias de usuario

HU-01 registrar la historia clínica básica · HU-02 agendar una cita · HU-03 responder el triage y
clasificar la urgencia (baja, media o alta) · HU-04 notificar a enfermería en tiempo real ·
HU-05 registrar diagnóstico, notas y receta · HU-06 consultar la historia con acceso auditado ·
HU-07 cancelar o reprogramar una cita · HU-08 consultar la agenda del día · HU-09 ver el panel de alertas activas.

## Reglas de negocio clave

1. Un médico solo ve la historia de un paciente con quien tiene una cita activa o histórica, y cada acceso
   queda registrado (fecha, hora y médico).
2. No se puede iniciar una consulta si el triage de esa cita no está completado.
3. Una urgencia alta genera una alerta clínica y prioriza la cita.
4. Una cita con una alerta urgente sin atender no se puede cancelar.
5. Al cancelar una cita, su cupo se libera para otro paciente.

## Requisitos no funcionales

- **Disponibilidad:** el agendamiento debe seguir funcionando aunque el módulo de alertas esté caído.
- **Oportunidad:** las alertas de urgencia alta deben llegar en segundos.
- **Confidencialidad:** acceso a la historia clínica restringido y auditado por rol.

## Alcance construido

Hasta la Lección 6 (comunicación entre microservicios): tres microservicios con arquitectura hexagonal,
gRPC, Kafka con outbox y saga, RabbitMQ, API Gateway y Eureka, todo desplegable con Docker Compose.
Lo posterior (observabilidad, DLT, CQRS, CI) se documenta como **Propuesta** en los ADR.
