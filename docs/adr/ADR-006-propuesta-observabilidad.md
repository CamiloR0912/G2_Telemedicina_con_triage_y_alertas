# ADR-006 · Observabilidad con Actuator y Correlation-Id

- **Estado:** **Propuesta** · NO implementada en el commit del corte
- **Fecha:** 2026-11-20
- **Evidencia en el código:** ninguna. Ningún `pom.xml` incluye `spring-boot-starter-actuator` y no existe un filtro `X-Correlation-Id`.

## Contexto

Una petición de la demo cruza Gateway → Historia → Agenda (gRPC), y un triage de urgencia alta viaja por
Kafka y RabbitMQ. Hoy, para seguir un caso, hay que revisar los logs de cada contenedor y relacionar los
mensajes a mano por `citaId`. No hay *health checks* de aplicación: `docker-compose.yml` solo comprueba
que el puerto esté abierto.

## Decisión propuesta

1. Agregar `spring-boot-starter-actuator` a los cinco módulos y usar `/actuator/health` en los healthchecks
   de Docker Compose.
2. Un filtro en el Gateway genera `X-Correlation-Id` si no viene; cada servicio lo pone en el MDC y en el
   patrón de log.
3. El id viaja también en los metadatos gRPC y en las cabeceras de los mensajes de Kafka y RabbitMQ.

## Alternativas consideradas

1. **Trazas distribuidas con Micrometer Tracing y Zipkin:** visualización completa de los spans, pero
   requiere otro contenedor y más configuración de la que justifica el alcance actual.
2. **Seguir correlacionando por `citaId`:** sin trabajo extra, pero no sirve para peticiones que fallan
   antes de tener una cita, y obliga a revisar log por log.

## Consecuencias (si se implementa)

- **Positivas:** un solo id para buscar un caso en todos los logs; healthchecks que reflejan el estado
  real de la aplicación, no solo el puerto.
- **Negativas:** hay que propagar el id a mano en tres tecnologías (HTTP, gRPC y dos brokers), y un
  olvido corta la trazabilidad sin que nada falle; además, Actuator expone endpoints que hay que restringir.
