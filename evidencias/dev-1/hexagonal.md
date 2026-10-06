# Dev 1 · Subdominio Agenda — Arquitectura Hexagonal (Lección 4)

Se aplican al agregado `Cita` los mismos movimientos del taller sobre `rica-api`: puerto primario explícito, puerto secundario mínimo con adaptador separado, paquetes que reflejan el rol, y un test del núcleo con un Fake hecho a mano.

## Paso 1 · Auditoría de puertos y adaptadores existentes (antes de tocar código)

A diferencia de `rica-api`, al terminar DDD el subdominio Agenda **no tenía controlador, servicio de aplicación ni repositorio**: solo el modelo de dominio. El hexágono no existía "a medias", así que en este taller hubo que crear esas piezas además de nombrarlas.

| Pregunta del taller | Respuesta en Agenda |
|---|---|
| ¿El repositorio es puerto primario o secundario? | No existía. Hacía falta un **puerto secundario**: la llamada la inicia el núcleo (el caso de uso necesita leer y guardar citas), no un actor externo. Se crea `RepositorioCitas`. |
| ¿El controlador es adaptador primario o secundario? | No existía. Se crea `CitaController`, **adaptador primario**: envuelve HTTP/Spring MVC y es quien inicia la llamada hacia el núcleo. |
| ¿Qué pieza falta para un puerto primario explícito? | Una interfaz que diga qué promete el caso de uso, separada de quién lo cumple. Se crea `CitaUseCase` (puerto) y `CitaService` lo implementa. El controlador depende de `CitaUseCase`, nunca de `CitaService`. |
| `CitaFactory`, ¿núcleo o adaptador? | **Núcleo.** Sus `import` son solo `java.time.*` y `java.util.UUID`. No depende del repositorio (a diferencia de `InvestigadorFactory`, que consulta si el correo existe), por eso se queda en `dominio` junto a `Cita`: es la única clase que puede llamar a su constructor de paquete. |
| `DisponibilidadService`, ¿núcleo o adaptador? | **Núcleo** (Servicio de Dominio). Recibe las citas como parámetro; no sabe de dónde salen. Por eso puede quedarse en `dominio` y `Cita.reprogramar` sigue siendo de paquete. |

## Equivalencia con el taller de `rica-api`

| Taller (`investigadores`) | Proyecto (`agenda`) | Rol |
|---|---|---|
| `InvestigadorUseCase` | `CitaUseCase` | Puerto primario |
| `InvestigadorService` | `CitaService` | Caso de uso (implementa el puerto primario) |
| `RepositorioInvestigadores` | `RepositorioCitas` | Puerto secundario mínimo |
| `InvestigadorRepository` | `CitaJpaRepository` | Detalle técnico de Spring Data JPA |
| `InvestigadorRepositoryJpaAdapter` | `CitaRepositoryJpaAdapter` | Adaptador secundario |
| `InvestigadorController` / `Request` / `Response` / `Mapper` | `CitaController` / `AgendarCitaRequest`, `ReprogramarCitaRequest` / `CitaResponse` / `CitaMapper` | Adaptador primario |
| `RepositorioInvestigadoresFalso` | `RepositorioCitasFalso` | Test double hecho a mano |
| `InvestigadorServiceConFalsoTest` | `CitaServiceConFalsoTest` | Test del núcleo sin Spring ni Mockito |

## Paso 4 · Paquetes por rol hexagonal

```
agenda/
├── dominio/                      ← núcleo: solo importa java.*
│   ├── Cita, EstadoCita, FranjaHoraria
│   ├── CitaFactory, DisponibilidadService
│   └── CitaNoEncontradaException, FranjaNoDisponibleException
├── aplicacion/                   ← núcleo: puertos + caso de uso
│   ├── CitaUseCase               (puerto primario)
│   ├── RepositorioCitas          (puerto secundario)
│   └── CitaService
└── infraestructura/
    ├── entrada/web/              ← adaptador primario (HTTP)
    │   ├── CitaController, CitaMapper, AgendaExceptionHandler
    │   └── AgendarCitaRequest, ReprogramarCitaRequest, CitaResponse
    ├── salida/persistencia/      ← adaptador secundario (JPA + H2)
    │   ├── CitaRepositoryJpaAdapter
    │   ├── CitaJpaRepository
    │   └── CitaEntity
    └── configuracion/
        └── AgendaConfig          (registra CitaFactory y DisponibilidadService como beans)
```

Regla de dependencia: `infraestructura → aplicacion → dominio`, nunca al revés. Se comprobó revisando los `import`: `dominio` solo importa `java.*`, y `aplicacion` solo importa `dominio` y `@Service`.

## Decisiones propias de este proyecto

- **Entidad JPA separada (`CitaEntity`).** En el taller `Investigador` lleva las anotaciones JPA. Aquí `Cita` no tiene ninguna anotación de `jakarta.persistence`, porque sus campos son `final`, su constructor es de paquete y `FranjaHoraria` es un `record`. El adaptador traduce `Cita ↔ CitaEntity`.
- **`Cita.reconstituir(...)`.** El adaptador necesita reconstruir una cita leída de la base de datos con su id y su estado. Eso no es "agendar": no valida que la franja esté en el futuro ni genera un id nuevo. Una cita nueva se sigue creando solo con `CitaFactory.agendar`.
- **Dominio sin anotaciones de Spring.** `CitaFactory` y `DisponibilidadService` se registran en `AgendaConfig`, así el test del núcleo los crea con `new`.
- **Base de datos H2 en memoria.** El proyecto no tenía base de datos; con H2 arranca sin Docker. Para cambiar a PostgreSQL solo se modifica `application.yaml` y la dependencia del driver: el núcleo no se entera.
- **Errores del dominio → HTTP** en `AgendaExceptionHandler`, limitado a `CitaController` para no afectar los controladores de otros subdominios.

## Endpoints

| HU | Método y ruta | Respuesta |
|---|---|---|
| HU-02 | `POST /api/citas` `{pacienteId, medicoId, especialidad, inicio, fin}` | 201; 409 si la franja está ocupada; 400 si los datos son inválidos o la fecha está en el pasado |
| — | `GET /api/citas/{id}` | 200; 404 si no existe |
| HU-07 | `POST /api/citas/{id}/cancelar` | 200; 409 si ya estaba cancelada |
| HU-07 | `POST /api/citas/{id}/reprogramar` `{inicio, fin}` | 200; 409 si la nueva franja está ocupada o la cita está cancelada |
| HU-08 | `GET /api/medicos/{medicoId}/agenda?dia=2027-03-10` | Citas activas del día, ordenadas por hora |

## Paso 8 · Checklist para el proyecto de grupo

- [x] Identificado el puerto secundario (no existía: se creó `RepositorioCitas`) y el puerto primario (se creó `CitaUseCase`).
- [x] Puerto secundario mínimo (4 métodos que `CitaService` usa) y adaptador explícito `CitaRepositoryJpaAdapter` que envuelve el repositorio de Spring Data.
- [x] Test sin Spring ni Mockito con un Fake hecho a mano: `CitaServiceConFalsoTest` + `RepositorioCitasFalso` (6 tests).
- [x] Ningún caso de uso de Agenda importa clases de `infraestructura` de otro subdominio. Agenda tampoco importa nada de Triage, Alertas ni Historia clínica.
