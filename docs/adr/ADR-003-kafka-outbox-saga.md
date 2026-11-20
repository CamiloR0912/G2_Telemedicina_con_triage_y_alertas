# ADR-003 · Eventos en Kafka publicados con Transactional Outbox y Saga coreografiada de cancelación

- **Estado:** Aceptada · implementada
- **Fecha:** 2026-11-11
- **Evidencia en el código:** `OutboxEvent`, `OutboxPoller` (`@Scheduled(fixedDelay = 2000)`) en `agenda-service` y `triage-alertas-service`; `CitaEnCancelacionListener` (Triage), `RespuestaCancelacionListener` (Agenda), `TriageCompletadoListener` y `TriagesCompletadosJpaAdapter` (Historia).

## Contexto

Dos flujos cruzan servicios sin necesitar respuesta inmediata:

1. **Triage → Historia:** la regla "no hay consulta sin triage completado" requiere que Historia sepa qué
   citas ya tienen triage. Historia no debe dejar de funcionar si Triage está caído.
2. **Cancelación de cita:** una cita con una alerta urgente sin atender no se puede cancelar. Agenda es
   dueña de la cita, pero la alerta vive en Triage.

Publicar directamente en Kafka después de guardar en la base de datos puede perder el evento (se guarda
y Kafka falla) o enviarlo sin cambio real (Kafka recibe y la transacción falla).

## Decisión

- Cada servicio que publica guarda el evento en una **tabla outbox en la misma transacción** que el
  cambio de estado; un `OutboxPoller` lo envía a Kafka cada 2 s y lo marca como publicado.
- **Topics:** `triage.completados`, `citas.en-cancelacion`, `citas.cancelacion-confirmada`,
  `citas.cancelacion-rechazada`.
- **Saga coreografiada:** `POST /api/citas/{id}/cancelar` responde 202 y deja la cita en
  `EN_CANCELACION`; Triage revisa si hay alerta urgente sin atender y responde confirmando o rechazando;
  si rechaza, Agenda **compensa** devolviendo la cita a su estado anterior.
- El consumidor de `triage.completados` es **idempotente**: un evento repetido no duplica la proyección.

**Por qué asíncrona:** ninguno de los dos flujos necesita la respuesta dentro de la misma petición HTTP
y ambos deben tolerar que el otro servicio esté caído.

## Alternativas consideradas

1. **Llamada REST síncrona de Agenda a Triage al cancelar:** más simple de seguir, pero la cancelación
   fallaría si Triage está caído, y eso contradice el requisito de que Agenda funcione sola.
2. **Transacción distribuida (2PC/XA):** daría consistencia inmediata, pero Kafka y H2 no la soportan
   juntos de forma práctica, y bloquea recursos.
3. **Saga orquestada** con un orquestador central: el flujo quedaría explícito en un solo lugar, pero con
   un único paso de ida y vuelta añade un componente más sin beneficio claro.
4. **Publicar directo en Kafka sin outbox:** menos código, pero pierde eventos cuando Kafka falla
   justo después del commit.

## Consecuencias

- **Positivas:** ningún evento se pierde si Kafka cae (queda en la outbox); Agenda sigue agendando con
  Triage caído; la compensación deja la cita en un estado válido.
- **Negativas:**
  - **Consistencia eventual visible para el usuario:** mientras Triage está caído, la cita queda en
    `EN_CANCELACION` **ocupando su cupo** indefinidamente.
  - Entrega **al menos una vez**: los consumidores deben ser idempotentes; hoy solo el de
    `triage.completados` lo garantiza de forma explícita.
  - El poller añade hasta 2 s de latencia y consultas periódicas a la base de datos aunque no haya eventos.
  - No hay Dead Letter Topic: un mensaje que siempre falla se reintenta según el comportamiento por
    defecto del contenedor (ver la **Propuesta** ADR-007).
