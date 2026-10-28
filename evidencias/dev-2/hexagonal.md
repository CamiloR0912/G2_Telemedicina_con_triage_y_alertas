# Dev 2 · Subdominio Triage y alertas — Arquitectura Hexagonal (Lección 4)

Se aplican al agregado `CuestionarioTriage` los mismos movimientos del taller sobre `rica-api`: puerto primario explícito, puerto secundario mínimo con adaptador separado, paquetes que reflejan el rol, y un test del núcleo con un Fake hecho a mano.

## Paso 1 · Auditoría de puertos y adaptadores existentes (antes de tocar código)

A diferencia de `rica-api`, al terminar DDD el subdominio Triage y alertas **no tenía controlador, servicio de aplicación ni repositorio**: solo el modelo de dominio. Lo único que ya cumplía un rol hexagonal era `NotificadorEnfermeria`.

| Pregunta del taller | Respuesta en Triage y alertas |
|---|---|
| ¿El repositorio es puerto primario o secundario? | No existía. Hacía falta un **puerto secundario**: la llamada la inicia el núcleo (el caso de uso necesita guardar y leer cuestionarios), no un actor externo. Se crea `RepositorioCuestionariosTriage`. |
| `NotificadorEnfermeria`, ¿qué rol cumple? | Ya era un **puerto secundario** sin que nadie lo llamara así: una interfaz que el núcleo llama para avisar a enfermería (HU-04), sin saber si es WebSocket, correo o SMS. Pero vivía en el dominio y la llamaba `ClasificacionTriageService` (Servicio de Dominio). Se mueve a `aplicacion` y la llama el caso de uso. |
| ¿El controlador es adaptador primario o secundario? | No existía. Se crea `CuestionarioTriageController`, **adaptador primario**: envuelve HTTP/Spring MVC y es quien inicia la llamada hacia el núcleo. |
| ¿Qué pieza falta para un puerto primario explícito? | Una interfaz que diga qué promete el caso de uso, separada de quién lo cumple. Se crea `CuestionarioTriageUseCase` (puerto) y `CuestionarioTriageService` lo implementa. El controlador depende de `CuestionarioTriageUseCase`, nunca de `CuestionarioTriageService`. |
| `CuestionarioTriageFactory`, ¿núcleo o adaptador? | **Núcleo.** Sus `import` son solo `java.time.*` y `java.util.*`. No consulta ningún repositorio, por eso se queda en `dominio` junto a `CuestionarioTriage`: es la única clase que puede llamar a su constructor de paquete. |
| `ClasificacionTriageService`, ¿núcleo o adaptador? | **Núcleo** (Servicio de Dominio). Recibe el cuestionario como parámetro y solo usa `java.*`. Por eso puede quedarse en `dominio` y `CuestionarioTriage.clasificar` / `generarAlerta` siguen siendo de paquete. |

## Equivalencia con el taller de `rica-api`

| Taller (`investigadores`) | Proyecto (`triagealertas`) | Rol |
|---|---|---|
| `InvestigadorUseCase` | `CuestionarioTriageUseCase` | Puerto primario |
| `InvestigadorService` | `CuestionarioTriageService` | Caso de uso (implementa el puerto primario) |
| `RepositorioInvestigadores` | `RepositorioCuestionariosTriage` | Puerto secundario mínimo |
| — | `NotificadorEnfermeria` | Segundo puerto secundario (HU-04) |
| `InvestigadorRepository` | `CuestionarioTriageJpaRepository` | Detalle técnico de Spring Data JPA |
| `InvestigadorRepositoryJpaAdapter` | `CuestionarioTriageRepositoryJpaAdapter` | Adaptador secundario (persistencia) |
| `ClienteReportesMinCiencias` (reto 7-A) | `NotificadorEnfermeriaLog` | Adaptador secundario simulado (notificación) |
| `InvestigadorController` / `Request` / `Response` / `Mapper` | `CuestionarioTriageController` / `ResponderTriageRequest`, `AtenderAlertaRequest` / `CuestionarioTriageResponse`, `AlertaActivaResponse`, `TriageCompletadoResponse` / `CuestionarioTriageMapper` | Adaptador primario |
| `RepositorioInvestigadoresFalso` | `RepositorioCuestionariosTriageFalso`, `NotificadorEnfermeriaFalso` | Test doubles hechos a mano |
| `InvestigadorServiceConFalsoTest` | `CuestionarioTriageServiceConFalsoTest` | Test del núcleo sin Spring ni Mockito |

## Paso 4 · Paquetes por rol hexagonal

```
triagealertas/
├── dominio/                      ← núcleo: solo importa java.*
│   ├── CuestionarioTriage        (raíz del agregado)
│   ├── AlertaClinica             (entidad interna)
│   ├── NivelUrgencia, RespuestaTriage   (Value Objects)
│   ├── ClasificacionTriageService, CuestionarioTriageFactory
│   └── CuestionarioTriageNoEncontradoException
├── aplicacion/                   ← núcleo: puertos + caso de uso (sin Spring)
│   ├── CuestionarioTriageUseCase        (puerto primario)
│   ├── RepositorioCuestionariosTriage   (puerto secundario)
│   ├── NotificadorEnfermeria            (puerto secundario)
│   └── CuestionarioTriageService
└── infraestructura/
    ├── entrada/web/              ← adaptador primario (HTTP)
    │   ├── CuestionarioTriageController, CuestionarioTriageMapper, TriageAlertasExceptionHandler
    │   └── ResponderTriageRequest, AtenderAlertaRequest, CuestionarioTriageResponse,
    │       AlertaActivaResponse, TriageCompletadoResponse
    ├── salida/persistencia/      ← adaptador secundario (JPA + H2)
    │   ├── CuestionarioTriageRepositoryJpaAdapter
    │   ├── CuestionarioTriageJpaRepository
    │   └── CuestionarioTriageEntity, RespuestaTriageEmbeddable
    ├── salida/notificacion/      ← adaptador secundario (simulado, log)
    │   └── NotificadorEnfermeriaLog
    └── configuracion/
        └── TriageAlertasConfig   (registra Factory, Servicio de Dominio y caso de uso como beans)
```

Los tests siguen la misma estructura: `dominio/` (tests del modelo) y `aplicacion/` (Fakes + test del caso de uso).

Regla de dependencia: `infraestructura → aplicacion → dominio`, nunca al revés. Se comprobó revisando los `import`: `dominio` solo importa `java.*`, y `aplicacion` solo importa `dominio` y `java.*` (ni siquiera `@Service`).

## Decisiones propias de este proyecto

- **La notificación a enfermería sale del Servicio de Dominio.** En DDD, `ClasificacionTriageService` recibía `NotificadorEnfermeria` y avisaba él mismo. Hablar con el mundo exterior es tarea del caso de uso, no del dominio; además, si el puerto se quedaba en `dominio`, el núcleo de dominio conocería un canal externo. Ahora `ClasificacionTriageService` solo clasifica y genera la alerta, y `CuestionarioTriageService` notifica a través del puerto. La regla "la alerta se genera en menos de 1 minuto" se mantiene: clasificar, generar la alerta y notificar ocurren en la misma llamada a `responder(...)`.
- **`CuestionarioTriage.marcarAlertaNotificada()` pasa a ser público**, porque ahora lo llama el caso de uso (otro paquete). Sigue protegiendo su invariante: falla si el cuestionario no tiene alerta. `clasificar` y `generarAlerta` siguen siendo de paquete.
- **La alerta no se pierde si el canal falla.** El caso de uso guarda el cuestionario *antes* de notificar; si `NotificadorEnfermeria` lanza una excepción, la alerta queda guardada activa con `notificada = false` y aparece en el panel (HU-09). Si la notificación funciona, se vuelve a guardar con `notificada = true`.
- **Entidad JPA separada (`CuestionarioTriageEntity`).** `CuestionarioTriage` no tiene ninguna anotación de `jakarta.persistence`: sus campos de identidad son `final`, su constructor es de paquete y sus Value Objects son `record`. El adaptador traduce `CuestionarioTriage ↔ CuestionarioTriageEntity`. Las respuestas van en una tabla aparte (`respuestas_triage`, `@ElementCollection`) y la alerta clínica (máximo una por cuestionario) en columnas de la misma tabla.
- **`CuestionarioTriage.reconstituir(...)` y `AlertaClinica.reconstituir(...)`.** El adaptador necesita reconstruir un cuestionario leído de la base de datos con su id, su nivel y su alerta. Eso no es "responder el cuestionario": no genera id nuevo ni vuelve a validar. Uno nuevo se sigue creando solo con `CuestionarioTriageFactory.crear`.
- **Núcleo sin anotaciones de Spring.** `CuestionarioTriageFactory`, `ClasificacionTriageService` y `CuestionarioTriageService` se registran en `TriageAlertasConfig`. Así el test del núcleo los crea con `new` y un reloj fijo. El `Clock` no se publica como bean para no chocar con el de otros subdominios al integrar en `main`.
- **Base de datos H2 en memoria**, igual que Agenda (Dev 1): arranca sin Docker. Para cambiar a PostgreSQL solo se modifica `application.yaml` y el driver; el núcleo no se entera.
- **Errores del dominio → HTTP** en `TriageAlertasExceptionHandler`, limitado a `CuestionarioTriageController` para no afectar los controladores de otros subdominios.

## Endpoints

| HU | Método y ruta | Respuesta |
|---|---|---|
| HU-03, HU-04 | `POST /api/triage` `{pacienteId, citaId, respuestas: [{sintoma, intensidad}]}` | 201 con el nivel de urgencia y, si es alto, la alerta clínica ya notificada; 400 si faltan datos, no hay respuestas, hay síntomas repetidos o la intensidad no está entre 0 y 10 |
| — | `GET /api/triage/{id}` | 200; 404 si no existe |
| Regla de negocio | `GET /api/triage/completado?citaId=cita-1` | `{citaId, completado}`: si la cita ya tiene un triage completado |
| HU-09 | `GET /api/alertas/activas` | Alertas sin atender, de mayor a menor urgencia y de la más antigua a la más reciente |
| HU-09 | `POST /api/triage/{id}/alerta/atender` `{enfermeriaId}` | 200; 409 si la alerta ya fue atendida o el cuestionario no tiene alerta; 404 si no existe |

Ejemplo:

```bash
curl -X POST localhost:8080/api/triage -H "Content-Type: application/json" \
  -d '{"pacienteId":"pac-1","citaId":"cita-1","respuestas":[{"sintoma":"Dolor de pecho","intensidad":7}]}'
```

## Paso 8 · Checklist para el proyecto de grupo

- [x] Identificado el puerto secundario que ya existía (`NotificadorEnfermeria`), el que faltaba (se creó `RepositorioCuestionariosTriage`) y el puerto primario (se creó `CuestionarioTriageUseCase`).
- [x] Puerto secundario mínimo (4 métodos que `CuestionarioTriageService` usa) y adaptador explícito `CuestionarioTriageRepositoryJpaAdapter` que envuelve el repositorio de Spring Data.
- [x] Test sin Spring ni Mockito con Fakes hechos a mano: `CuestionarioTriageServiceConFalsoTest` + `RepositorioCuestionariosTriageFalso` + `NotificadorEnfermeriaFalso` (7 tests).
- [x] Ningún caso de uso de Triage y alertas importa clases de `infraestructura` de otro subdominio. Triage tampoco importa nada de Agenda ni de Historia clínica: Paciente y Cita se siguen referenciando solo por id.
- [x] (Reto 7, opción A) Segundo adaptador secundario real: `NotificadorEnfermeriaLog`, pendiente de una integración real (WebSocket).
