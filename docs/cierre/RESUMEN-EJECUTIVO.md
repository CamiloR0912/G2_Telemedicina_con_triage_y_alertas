# Resumen ejecutivo · G2 Telemedicina con triage y alertas

**Qué resuelve.** Una plataforma de telemedicina en la que el paciente agenda su cita, responde un
triage antes de la consulta y, si la urgencia es alta, el personal de enfermería recibe una alerta en
segundos y la cita se prioriza. Solo el médico tratante puede abrir la historia clínica, y cada acceso
queda auditado.

**Cómo está construido.** Tres microservicios Spring Boot con arquitectura hexagonal, uno por bounded
context (Agenda, Triage y alertas, Historia clínica), cada uno con su propia base de datos. Un API
Gateway (puerto 8000) es la única entrada y encuentra los servicios en Eureka. La comunicación es
**síncrona** solo donde la regla de seguridad lo exige (gRPC de Historia a Agenda, con Circuit Breaker)
y **asíncrona** en el resto: Kafka con Transactional Outbox para el triage completado y la saga de
cancelación, y RabbitMQ para las alertas urgentes. Todo se levanta con `docker compose up`.

**Resultados.** El agendamiento sigue funcionando con el módulo de alertas caído; ningún evento se pierde
si Kafka falla; los cinco módulos desplegados suman 108 pruebas automáticas y una colección de Bruno cubre los
flujos que cruzan servicios.

**Riesgos principales.** La historia clínica depende de que Agenda esté disponible; las alertas no se
reintentan si RabbitMQ está caído; no hay Dead Letter Topic ni trazabilidad centralizada.

**Siguientes pasos (Propuesta).** Observabilidad con Correlation-Id (ADR-006), DLT con reintentos
(ADR-007), consumidor de alertas para enfermería e integración continua.
