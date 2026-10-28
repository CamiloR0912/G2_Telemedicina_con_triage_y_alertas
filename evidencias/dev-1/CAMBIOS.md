# Dev 1 · Agenda → `agenda-service`

**Rol en la comunicación:** servidor **gRPC** (Historia clínica le pregunta si hay cita) y consumidor
**RabbitMQ** (las alertas de urgencia alta priorizan la cita).

Puertos: REST `8081`, gRPC `9090`. Paquete raíz: `com.telemedicina.agendaservice`.

## Paso 1 · Crear el proyecto y mover el subdominio

1. Proyecto Maven nuevo `agenda-service` (Spring Boot 4.1.1, Java 25). Dependencias en
   [pom.xml](agenda-service/pom.xml): `webmvc`, `data-jpa`, `h2`, **`grpc-server`**, `grpc-services`,
   **`amqp`** y el plugin `protobuf-maven-plugin`.
2. Se copió el paquete `agenda` completo (dominio, aplicación, infraestructura y sus tests) de la rama
   `hexagonal`, cambiando `com.telemedicina.telemedicina.agenda` → `com.telemedicina.agendaservice.agenda`.
3. Clase principal propia: `AgendaServiceApplication`.
4. [application.yml](agenda-service/src/main/resources/application.yml): puerto 8081, H2 en archivo
   (`./data/agenda`), gRPC en 9090, RabbitMQ en localhost:5672 con `default-requeue-rejected: false`
   (un mensaje que falla no vuelve a la cola en bucle infinito).

## Paso 2 · Servidor gRPC para Historia clínica (HU-06)

En la versión hexagonal, Historia clínica usaba un adaptador **simulado** con parejas `med:pac` en su
configuración, porque Agenda no exponía sus citas. Ahora Agenda las expone por gRPC.

- Contrato: [src/main/proto/agenda.proto](agenda-service/src/main/proto/agenda.proto) →
  `AgendaGrpc.ExisteCitaEntre(medicoId, pacienteId) → existe`. Solo responde sí/no: Historia clínica
  nunca recibe la Cita.
- Caso de uso nuevo en el puerto primario `CitaUseCase`: `existeCitaEntre(medicoId, pacienteId)`.
  Cuenta cualquier cita **no cancelada**, futura (activa) o pasada (histórica). Implementado en `CitaService`.
- Adaptador primario nuevo: `infraestructura/entrada/grpc/AgendaGrpcService` (`@GrpcService`), hermano del
  `CitaController`: traduce gRPC al mismo puerto `CitaUseCase`.

Probarlo solo (con el servicio corriendo):

```powershell
docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list
$json = '{"medicoId":"med-1","pacienteId":"pac-1"}'
$json | docker run --rm -i fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 AgendaGrpc/ExisteCitaEntre
```

## Paso 3 · Priorizar la cita cuando llega una alerta (HU-03, RabbitMQ)

HU-03 dice "para que el sistema **priorice mi atención** si es urgente". Cuando Triage detecta urgencia
alta publica la alerta en RabbitMQ y Agenda marca la cita como prioritaria.

- Dominio: `Cita` tiene el campo `prioritaria` y el método `priorizar()` (una cita cancelada no se puede
  priorizar). `Cita.reconstituir(...)` recibe el nuevo campo.
- Persistencia: columna `prioritaria` en `CitaEntity`. Web: `CitaResponse` expone `prioritaria`.
- Caso de uso: `CitaUseCase.priorizar(UUID id)`.
- [RabbitConfig](agenda-service/src/main/java/com/telemedicina/agendaservice/agenda/infraestructura/configuracion/RabbitConfig.java):
  declara la cola **propia** `agenda.alertas-urgentes.queue` y la engancha a `alertas.exchange` con la
  ruta `alerta.urgencia.alta`. Declarar el exchange aquí también permite arrancar aunque Triage nunca
  haya arrancado.
- Adaptador primario nuevo: `infraestructura/entrada/mensajeria/AlertaUrgenteListener` (`@RabbitListener`).
  Si la cita no existe o está cancelada, registra un WARN y descarta el mensaje: reintentarlo no lo arregla.

**Requisito no funcional:** Agenda nunca llama a Triage. Si Triage cae, Agenda sigue agendando; si
Agenda cae, las alertas esperan en su cola hasta que vuelva.

## Paso 4 · Tests

`CitaServiceConFalsoTest` tiene dos tests nuevos: `laAlertaDeUrgenciaAltaPriorizaLaCitaPeroNoUnaCancelada`
y `existeCitaEntreMedicoYPacienteSoloSiNoEstaCancelada`. `.\mvnw.cmd test` → 32 tests en verde.

## Archivos

| Estado | Archivo |
|---|---|
| Nuevo | `pom.xml`, `application.yml`, `AgendaServiceApplication`, `src/main/proto/agenda.proto` |
| Nuevo | `infraestructura/entrada/grpc/AgendaGrpcService` |
| Nuevo | `infraestructura/entrada/mensajeria/AlertaUrgenteListener`, `infraestructura/configuracion/RabbitConfig` |
| Modificado | `dominio/Cita` (prioritaria, priorizar), `aplicacion/CitaUseCase`, `aplicacion/CitaService` |
| Modificado | `salida/persistencia/CitaEntity`, `entrada/web/CitaResponse`, `entrada/web/CitaMapper` |
| Modificado | `test/.../CitaServiceConFalsoTest` |

Commits sugeridos (uno por paso): `feat(agenda): microservicio agenda-service`,
`feat(agenda): servidor gRPC ExisteCitaEntre`, `feat(agenda): priorizar cita con alertas de RabbitMQ`.
