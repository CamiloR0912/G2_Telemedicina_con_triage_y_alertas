# Matriz de trazabilidad · requisito → servicio → mecanismo → evidencia

Relaciona cada historia de usuario y requisito no funcional con el servicio que lo cumple, el mecanismo
arquitectónico usado y dónde verificarlo en el repositorio. El estado indica si está **Construido** o
solo es una **Propuesta**.

| Requisito | Servicio dueño | Mecanismo | Evidencia en el repositorio | Prueba | Estado |
|---|---|---|---|---|---|
| HU-01 Registrar historia clínica básica | `historia-clinica-service` | REST `POST /api/historias-clinicas` vía Gateway | `HistoriaClinicaController` | `bruno/historia-clinica/01-*`, `02-*` | Construido |
| HU-02 Agendar cita de telemedicina | `agenda-service` | REST `POST /api/citas`; reglas de solapamiento en el dominio | `CitaController`, `CitaService` | `bruno/agenda/01-*` a `08-*` | Construido |
| HU-03 Clasificar urgencia y generar alerta | `triage-alertas-service` | Dominio `ClasificacionTriageService`; RabbitMQ `alertas.exchange` | `NotificadorEnfermeriaRabbit`, `AlertaUrgenteListener` (Agenda) | `NivelUrgenciaTest`, `bruno/triage-alertas/` | Construido |
| HU-04 Notificar a enfermería en tiempo real | `triage-alertas-service` | RabbitMQ (envío directo) | `NotificadorEnfermeriaRabbit` | `bruno/triage-alertas/` (`notificada: true`) | Construido (consumidor propio de enfermería: Propuesta) |
| HU-05 Registrar diagnóstico, notas y receta | `historia-clinica-service` | REST `POST /{pacienteId}/consultas`; exige triage completado (Kafka `triage.completados`) | `TriageCompletadoListener`, `TriagesCompletadosJpaAdapter` | `bruno/historia-clinica/08b-*`, `09-*` | Construido |
| HU-06 Consultar historia con acceso auditado | `historia-clinica-service` → `agenda-service` | gRPC `ExisteCitaEntre` con Circuit Breaker | `HistorialCitasGrpcAdapter`, `AgendaGrpcService`, `agenda.proto` | `bruno/historia-clinica/05-*`, `06-*` | Construido |
| HU-07 Cancelar o reprogramar cita | `agenda-service` ⇄ `triage-alertas-service` | Saga coreografiada en Kafka con outbox y compensación | `OutboxPoller`, `CitaEnCancelacionListener`, `RespuestaCancelacionListener` | `bruno/saga-cancelacion/` | Construido |
| HU-08 Agenda del día del médico | `agenda-service` | REST `GET /api/medicos/{id}/agenda?dia=` | `CitaController` | `bruno/agenda/11-*`, `20-*` | Construido |
| HU-09 Panel de alertas activas | `triage-alertas-service` | REST `GET /api/alertas/activas` | Controlador de triage | `bruno/triage-alertas/` | Construido |
| RNF Agenda funciona con alertas caídas | `agenda-service` | Database per Service; sin llamadas síncronas a Triage | ADR-001, ADR-003 | Detener `triage-alertas-service` y agendar | Construido |
| RNF Entrada única y descubrimiento | `gateway`, `eureka-server` | Spring Cloud Gateway con `lb://` y Eureka | `gateway/src/main/resources/application.yml` | `GatewayRutasTest`, `bruno/eureka-gateway/` | Construido |
| RNF Trazabilidad entre servicios | Todos | Actuator y Correlation-Id | ADR-006 | — | Propuesta |
| RNF Mensajes fallidos recuperables | Consumidores Kafka | DLT y backoff | ADR-007 | — | Propuesta |
