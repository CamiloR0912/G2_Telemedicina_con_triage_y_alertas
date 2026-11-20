# ATAM-lite · G2 Telemedicina con triage y alertas

Evaluación ligera de la arquitectura **tal como está construida** (ver [ARQUITECTURA.md](ARQUITECTURA.md)).
Se priorizan cuatro atributos de calidad y, para cada escenario, se identifican puntos de sensibilidad,
compromisos (*trade-offs*), riesgos y no-riesgos.

## 1. Árbol de utilidad (escenarios priorizados)

| ID | Atributo | Escenario | Importancia / dificultad |
|---|---|---|---|
| E1 | Disponibilidad | Con `triage-alertas-service` caído, un paciente agenda una cita y Agenda responde 201 en menos de 2 s | Alta / Media |
| E2 | Desempeño | Un triage de urgencia alta prioriza la cita en Agenda en menos de 5 s | Alta / Media |
| E3 | Seguridad | Un médico sin cita con el paciente intenta abrir su historia clínica y recibe 403; el acceso queda auditado | Alta / Baja |
| E4 | Consistencia | Se cancela una cita con una alerta urgente sin atender y la cancelación se rechaza y compensa | Media / Alta |
| E5 | Modificabilidad | Se agrega un consumidor de alertas para enfermería sin modificar `triage-alertas-service` | Media / Baja |

## 2. Análisis por escenario

### E1 · Agenda funciona con Triage caído
- **Decisiones:** ADR-001 (servicios y bases separadas), ADR-003 (saga asíncrona).
- **Punto de sensibilidad:** que Agenda no haga ninguna llamada síncrona a Triage.
- **No-riesgo:** agendar no toca Kafka ni RabbitMQ de forma síncrona; el escenario se cumple.
- **Riesgo R1:** al cancelar con Triage caído, la cita queda en `EN_CANCELACION` **ocupando su cupo**
  hasta que Triage vuelva. Un paciente nuevo no puede tomar ese horario.

### E2 · La alerta urgente llega en segundos
- **Decisiones:** ADR-004 (RabbitMQ, envío directo sin outbox).
- **Trade-off:** se gana latencia (sin poller) a cambio de garantía de entrega.
- **Riesgo R2:** si RabbitMQ está caído en el momento del triage, la alerta **no se reenvía nunca**: la cita
  no se prioriza y la alerta queda solo en el panel con `notificada: false`.

### E3 · Acceso restringido a la historia clínica
- **Decisiones:** ADR-002 (gRPC síncrono con Circuit Breaker).
- **Punto de sensibilidad:** el deadline de 5 s y el umbral del 50 % del Circuit Breaker.
- **No-riesgo:** ante la duda el sistema **niega** el acceso (503 o 403); nunca lo concede sin confirmar la cita.
- **Riesgo R3:** con Agenda caída, **ningún médico puede abrir historias clínicas**, aunque la consulta
  sea legítima. La disponibilidad de Historia depende de la de Agenda.

### E4 · Cancelación con alerta pendiente
- **Decisiones:** ADR-003 (outbox y saga coreografiada con compensación).
- **Trade-off:** consistencia eventual a cambio de independencia entre Agenda y Triage.
- **Riesgo R4:** no existe Dead Letter Topic. Un evento de la saga que falla siempre se descarta tras los
  reintentos por defecto, y la cita puede quedar en `EN_CANCELACION` sin que nadie lo note (Propuesta ADR-007).

### E5 · Nuevo consumidor de alertas
- **Decisiones:** ADR-004 (topic exchange; cada consumidor declara su cola).
- **No-riesgo:** basta con declarar una cola enlazada a `alertas.exchange` con la clave `alerta.urgencia.alta`.

## 3. Riesgos transversales

- **Riesgo R5:** el contrato `agenda.proto` está duplicado en dos servicios; un cambio en uno sin el otro
  rompe la comunicación en tiempo de ejecución, no en compilación.
- **Riesgo R6:** el Gateway es un punto único de fallo y no hay healthchecks de aplicación (solo de puerto).
- **Riesgo R7:** la base H2 en archivo no está pensada para producción ni para varias instancias del mismo
  servicio.

## 4. Temas de riesgo (resumen)

| Tema | Riesgos | Acción propuesta |
|---|---|---|
| Dependencia síncrona Historia → Agenda | R3, R5 | Mantener el Circuit Breaker; generar el `.proto` desde un único módulo compartido (Propuesta) |
| Entrega de mensajes sin red de seguridad | R2, R4 | DLT y backoff (ADR-007, Propuesta); outbox también para alertas si la garantía pesa más que la latencia |
| Operación y diagnóstico | R6, R7 | Actuator y Correlation-Id (ADR-006, Propuesta); base de datos de servidor por servicio para producción |
| Consistencia eventual visible | R1 | Plazo máximo para `EN_CANCELACION` con liberación automática del cupo (Propuesta) |
