# Gobernanza técnica del equipo

Reglas que el equipo sigue para que tres personas trabajen en tres servicios sin romperse entre sí.

## 1. Propiedad de los servicios

| Servicio | Responsable principal | Debe aprobar cambios en |
|---|---|---|
| `agenda-service` | Maria Jose Espinosa | su dominio, `agenda.proto`, topics `citas.*` |
| `triage-alertas-service` | Anderson Carvajal | su dominio, `alertas.exchange`, topic `triage.completados` |
| `historia-clinica-service` | Camilo Ramirez | su dominio y la copia de `agenda.proto` |
| `gateway`, `eureka-server`, `docker-compose.yml` | Camilo Ramirez (integración) | rutas y puertos |

Quien cambia un contrato compartido avisa al dueño del otro lado **antes** de integrar.

## 2. Contratos entre servicios

- **gRPC:** `agenda.proto` vive en `agenda-service`; la copia de `historia-clinica-service` solo cambia
  `java_package`. Todo cambio se aplica en las dos copias en el mismo commit.
- **Eventos:** los topics se nombran `<contexto>.<hecho-en-pasado>` (`triage.completados`,
  `citas.en-cancelacion`). Los campos de un evento solo se agregan; nunca se quitan ni se renombran.
- **Rutas HTTP:** todas cuelgan de `/api/<recurso>` y se registran en el Gateway.

## 3. Reglas de arquitectura

1. Database per Service: ningún servicio lee la base de datos de otro.
2. El paquete `dominio/` no importa Spring ni clases de infraestructura.
3. Todo evento que acompaña un cambio de estado se publica mediante la outbox.
4. Toda llamada síncrona entre servicios lleva deadline y Circuit Breaker.

## 4. Flujo de trabajo

- Una rama por integrante y por etapa (`dev-1-micro-servicios`, `dev2-micro-servicios`,
  `dev-3-micro-servicios`), integradas en `microservicios` y luego en `main`.
- Mensajes de commit con tipo y alcance: `feat(agenda): ...`, `docs(...)`, `test(...)`.
- **Definición de terminado:** `mvnw test` pasa en el servicio, la colección de Bruno de esa carpeta
  pasa contra `docker compose`, y la decisión relevante tiene ADR.

## 5. Decisiones

Toda decisión que afecte a más de un servicio se registra como ADR en `docs/adr/`, con alternativas y
consecuencias negativas. Lo que no está implementado se marca con estado **Propuesta**.
**Propuesta:** un pipeline de integración continua que ejecute `mvnw test` en cada servicio en cada push.
