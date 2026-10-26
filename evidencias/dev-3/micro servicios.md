# Dev 3 · Historia clínica → `historia-clinica-service`

**Rol en la comunicación:** cliente **gRPC** de Agenda (con **Circuit Breaker**) y consumidor **Kafka**
de `triage.completados`.

Puerto REST: `8083`. Paquete raíz: `com.telemedicina.historiaclinicaservice`.

## Paso 1 · Crear el proyecto y mover el subdominio

1. Proyecto Maven nuevo `historia-clinica-service`. Dependencias en
   [pom.xml](historia-clinica-service/pom.xml): `webmvc`, `data-jpa`, `h2`, **`grpc-client`**, **`kafka`**,
   **`spring-cloud-starter-circuitbreaker-resilience4j`** (con el BOM de Spring Cloud `2025.1.3`) y el
   plugin `protobuf-maven-plugin`.
2. Se copió el paquete `historiaclinica` completo (con sus tests) de la rama `hexagonal`, cambiando
   `com.telemedicina.telemedicina.historiaclinica` → `com.telemedicina.historiaclinicaservice.historiaclinica`.
3. [application.yml](historia-clinica-service/src/main/resources/application.yml): puerto 8083, H2 en archivo
   (`./data/historia-clinica`), canal gRPC `agenda` → `static://localhost:9090`, consumidor Kafka con
   `group-id: historia-clinica-service` y `auto-offset-reset: earliest`. Se eliminó la configuración
   `historia-clinica.agenda-simulada`.

## Paso 2 · Preguntarle a Agenda por gRPC (HU-06)

El puerto `HistorialCitas` ya existía y **no cambió**. Solo cambió su adaptador, igual que en el Taller 6
se reemplazó `VerificadorInvestigadorPendiente` por el adaptador gRPC.

- Contrato: [src/main/proto/agenda.proto](historia-clinica-service/src/main/proto/agenda.proto), copia del de
  agenda-service cambiando solo `java_package`.
- `HistoriaClinicaServiceApplication` registra el stub: `@ImportGrpcClients(target = "agenda", types = AgendaGrpcBlockingStub.class)`.
- Se borró `HistorialCitasAgendaSimuladaAdapter` y se creó `infraestructura/salida/agenda/HistorialCitasGrpcAdapter`:
  llamada con plazo de 5 s envuelta en un **Circuit Breaker** (ventana 10, mínimo 5 llamadas, 50 % de
  fallos, 10 s abierto).
- `aplicacion/ServicioNoDisponibleException` → **503** en `HistoriaClinicaExceptionHandler`. "No pude
  averiguar si hay cita" (503) no es lo mismo que "no hay cita" (403).

Verificado con Agenda detenida: 5 respuestas 503 `UNAVAILABLE`, luego `CLOSED to OPEN` en el log y 503
`circuito abierto` inmediato, sin intentar la red.

## Paso 3 · "No puede iniciarse una consulta sin un triage completado" (Kafka)

Esta regla de negocio no estaba aplicada en ninguna versión anterior. Historia clínica no le pregunta a
Triage: guarda **su propia copia** (proyección) de las citas con triage completado, alimentada por Kafka.
Así la regla funciona aunque Triage esté caído.

- Dominio: `TriageNoCompletadoException` → **409**.
- Puerto secundario nuevo: `aplicacion/TriagesCompletados` (`registrar(citaId)`, `estaCompletado(citaId)`).
- Adaptador: `salida/persistencia/TriagesCompletadosJpaAdapter` + `TriageCompletadoJpaEntity` +
  `TriageCompletadoRepository` (tabla `triages_completados`). `registrar` es idempotente: si Kafka entrega
  el mismo evento dos veces no pasa nada.
- Caso de uso (`HistoriaClinicaUseCase` / `HistoriaClinicaService`):
  - `registrarConsulta(...)` ahora recibe **`citaId`** y falla con 409 si esa cita no tiene triage completado
    (400 si no viene `citaId`). No se guarda nada si falla.
  - `registrarTriageCompletado(citaId)`: lo llama el listener.
- Web: `RegistroConsultaRequest` tiene el campo `citaId`.
- Adaptador primario nuevo: `infraestructura/entrada/mensajeria/TriageCompletadoListener` (`@KafkaListener`
  sobre `triage.completados`).

## Paso 4 · Tests

- `TriagesCompletadosFalso` nuevo (proyección en memoria).
- `HistoriaClinicaServiceTest`: tests nuevos `sinTriageCompletadoNoSeRegistraLaConsultaNiSeGuardaNada` y
  `registrarConsultaSinCitaEsDatoInvalido`; los existentes pasan `citaId`.
- `HistoriaClinicaServiceConFalsoTest`: registra el triage de la cita antes de la consulta.
- `.\mvnw.cmd test` → 23 tests en verde.

## Archivos

| Estado | Archivo |
|---|---|
| Nuevo | `pom.xml`, `application.yml`, `HistoriaClinicaServiceApplication`, `src/main/proto/agenda.proto` |
| Nuevo | `infraestructura/salida/agenda/HistorialCitasGrpcAdapter`, `aplicacion/ServicioNoDisponibleException` |
| Nuevo | `dominio/TriageNoCompletadoException`, `aplicacion/TriagesCompletados` |
| Nuevo | `salida/persistencia/TriagesCompletadosJpaAdapter`, `TriageCompletadoJpaEntity`, `TriageCompletadoRepository` |
| Nuevo | `infraestructura/entrada/mensajeria/TriageCompletadoListener` |
| Modificado | `aplicacion/HistoriaClinicaUseCase`, `aplicacion/HistoriaClinicaService`, `entrada/web/RegistroConsultaRequest`, `entrada/web/HistoriaClinicaController`, `entrada/web/HistoriaClinicaExceptionHandler`, `package-info` |
| Borrado | `infraestructura/salida/agenda/HistorialCitasAgendaSimuladaAdapter` |
| Modificado | `test/.../HistoriaClinicaServiceTest`, `test/.../HistoriaClinicaServiceConFalsoTest`; nuevo `TriagesCompletadosFalso` |

Commits sugeridos: `feat(historia-clinica): microservicio historia-clinica-service`,
`feat(historia-clinica): adaptador gRPC a Agenda con Circuit Breaker`,
`feat(historia-clinica): regla de triage completado con proyección desde Kafka`.
