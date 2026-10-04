# Dev 3 · Historia clínica — Arquitectura Hexagonal (Taller Lección 4)

## Punto de partida

A diferencia de `rica-api`, el subdominio Historia clínica salió del taller de DDD **solo con el núcleo de
dominio** (`HistoriaClinica`, `RegistroConsulta`, `RegistroAcceso`, `ControlAccesoHistoriaService`,
`HistoriaClinicaFactory`, `HistorialCitas`): no había controller, ni servicio de aplicación, ni persistencia.
Por eso, en cada paso del taller, además de nombrar los puertos y adaptadores, hay que **crear** las piezas
que en `rica-api` ya existían.

## Paso 1 · Auditoría de puertos y adaptadores existentes

**¿Existe hoy un repositorio de Spring Data que cumpla el rol de puerto secundario?**
No. Historia clínica no tiene persistencia todavía: las historias solo viven en memoria durante los tests.
Falta un puerto secundario de persistencia, y debe diseñarse según lo que el caso de uso necesita
(buscar por paciente, saber si existe, guardar), no según los ~30 métodos de `JpaRepository`.

**`HistorialCitas`: ¿puerto primario o secundario?**
Secundario. Quien inicia la llamada es el núcleo (`ControlAccesoHistoriaService` pregunta a la Agenda si hay
cita); la Agenda (Dev 1) es algo externo que responde. Ya es un puerto explícito: una interfaz definida por el
núcleo, con un único método, en el lenguaje del dominio y sin ninguna tecnología. Lo que le falta es un
adaptador que la implemente fuera del núcleo.

**¿Hay un adaptador primario (controller)?**
No. Ningún actor externo (HTTP) puede usar hoy el subdominio. Hay que crear `HistoriaClinicaController`
como adaptador primario sobre Spring MVC / HTTP.

**¿Existe un puerto primario explícito?**
No. `ControlAccesoHistoriaService` es un servicio de **dominio**: recibe una `HistoriaClinica` ya cargada y no
sabe cargarla ni guardarla. Falta (a) un contrato `HistoriaClinicaUseCase` con lo que el subdominio promete
(HU-01, HU-05, HU-06) y (b) un servicio de aplicación `HistoriaClinicaService` que lo implemente
orquestando repositorio + servicio de dominio + factory.

**`HistoriaClinicaFactory`: ¿núcleo o adaptador?**
Núcleo. Sus `import` son solo `java.util.*`: no depende de Spring, JPA ni HTTP. Además, a diferencia de
`InvestigadorFactory`, no consulta ningún repositorio, así que pertenece al **dominio** (no a aplicación):
crea la raíz usando su constructor package-private, que es parte del límite del agregado.

| Pieza | Rol hexagonal | Estado antes del taller |
|---|---|---|
| `HistoriaClinicaUseCase` | Puerto primario | **Falta** |
| `HistoriaClinicaService` | Caso de uso (implementa el puerto primario) | **Falta** |
| `HistoriaClinicaController` | Adaptador primario (HTTP / Spring MVC) | **Falta** |
| `RepositorioHistoriasClinicas` | Puerto secundario (persistencia) | **Falta** |
| `HistoriaClinicaRepositoryJpaAdapter` | Adaptador secundario (Spring Data JPA) | **Falta** |
| `HistorialCitas` | Puerto secundario (hacia Agenda) | Ya existe |
| Implementación de `HistorialCitas` | Adaptador secundario (Agenda) | **Falta** (Agenda es de Dev 1) |
| `ControlAccesoHistoriaService`, `HistoriaClinicaFactory`, `HistoriaClinica`, `RegistroConsulta`, `RegistroAcceso` | Núcleo (dominio) | Ya existe |

## Paso 2 · Puerto primario `HistoriaClinicaUseCase`

- `HistoriaClinicaUseCase` declara lo que el subdominio promete: `registrar` (HU-01), `consultar` (HU-06) y
  `registrarConsulta` (HU-05).
- `HistoriaClinicaService` lo implementa: carga la historia, delega las reglas en `ControlAccesoHistoriaService` /
  `HistoriaClinicaFactory` y la guarda (así el registro de acceso de HU-06 queda persistido).
- `HistoriaClinicaController` depende del puerto (`HistoriaClinicaUseCase`), nunca de la clase concreta.
- Se agregó `HistorialCitasAgendaSimuladaAdapter`: adaptador secundario simulado de `HistorialCitas` que lee las
  parejas `medico:paciente` de `application.yaml` mientras Agenda (Dev 1) no exponga sus citas.
- Como el servicio necesita persistencia para arrancar, este paso usó un adaptador **temporal en memoria**.

## Paso 3 · Puerto secundario mínimo + adaptador JPA explícito

- `RepositorioHistoriasClinicas` tiene solo los 3 métodos que el caso de uso llama
  (`buscarPorPacienteId`, `existePorPacienteId`, `guardar`), no los ~30 de `JpaRepository`.
- `HistoriaClinicaRepository` (Spring Data) sigue siendo el detalle técnico; `HistoriaClinicaRepositoryJpaAdapter`
  traduce entre los dos contratos.
- El dominio **no** lleva anotaciones JPA: el adaptador usa su propio `HistoriaClinicaJpaEntity` y reconstruye el
  agregado con `HistoriaClinica.reconstituir(...)`, que vuelve a pasar por las invariantes de la raíz.
- El adaptador en memoria del paso 2 se eliminó y se reemplazó por el JPA **sin cambiar una línea del núcleo**:
  prueba de que el adaptador es sustituible.

## Paso 4 · Paquetes por rol hexagonal

| Paquete | Clases |
|---|---|
| `historiaclinica.dominio` | `HistoriaClinica`, `RegistroConsulta`, `RegistroAcceso`, `ControlAccesoHistoriaService`, `HistoriaClinicaFactory`, `HistorialCitas`, excepciones |
| `historiaclinica.aplicacion` | `HistoriaClinicaUseCase`, `RepositorioHistoriasClinicas`, `HistoriaClinicaService` |
| `historiaclinica.infraestructura.entrada.web` | `HistoriaClinicaController`, `HistoriaClinicaRequest`, `RegistroConsultaRequest`, `HistoriaClinicaResponse`, `RegistroConsultaResponse`, `HistoriaClinicaMapper`, `HistoriaClinicaExceptionHandler` |
| `historiaclinica.infraestructura.salida.persistencia` | `HistoriaClinicaRepository`, `HistoriaClinicaRepositoryJpaAdapter`, `HistoriaClinicaJpaEntity` |
| `historiaclinica.infraestructura.salida.agenda` | `HistorialCitasAgendaSimuladaAdapter` |
| `historiaclinica.infraestructura.config` | `HistoriaClinicaConfig` |

Diferencias con `rica-api`, justificadas:
- `HistoriaClinicaFactory` y `ControlAccesoHistoriaService` quedan en **dominio** (no en aplicación): no dependen
  de ningún repositorio y usan los métodos package-private de la raíz, que protegen el límite del agregado.
- `HistorialCitas` queda en **dominio** porque lo usa el servicio de dominio; si se moviera a `aplicacion`,
  el dominio dependería de aplicación (dependencia hacia afuera).
- `HistoriaClinicaConfig` registra como beans las piezas del dominio, para que el núcleo no lleve `@Component`.

**Prueba del núcleo limpio:** los `import` de `dominio` son solo `java.*`; los de `aplicacion` son `java.*`,
`dominio.*` y `@Service`. Ninguna clase del núcleo importa `infraestructura`.

## Paso 5 · Probar el núcleo sin infraestructura

`RepositorioHistoriasClinicasFalso` (un `Map` en memoria) + `HistoriaClinicaServiceConFalsoTest`: registra una
historia, registra una consulta, la consulta con un médico con cita y verifica que el acceso quedó auditado y que un
médico sin cita es rechazado, sin `@SpringBootTest`, sin `@Mock` y sin base de datos. `HistorialCitas` también se
cumple con una lambda.

## Endpoints (adaptador primario HTTP)

| Método | Ruta | HU | Respuestas |
|---|---|---|---|
| `POST` | `/api/historias-clinicas` | HU-01 | 201, 400 (datos inválidos), 409 (ya existe) |
| `GET` | `/api/historias-clinicas/{pacienteId}` + header `X-Medico-Id` | HU-06 | 200, 403 (sin cita), 404 |
| `POST` | `/api/historias-clinicas/{pacienteId}/consultas` + header `X-Medico-Id` | HU-05 | 201, 403, 404, 409 (consulta repetida) |

Citas simuladas por defecto: `med-1:pac-1`, `med-2:pac-2` (`application.yaml`).
