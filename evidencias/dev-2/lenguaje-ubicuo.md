# Dev 2 · Triage y alertas — Lenguaje Ubicuo y límite del Agregado

## Paso 1 · Lenguaje Ubicuo (auditoría contra Requisitos_G2)

| Término del negocio (requisitos) | Origen | Nombre en el código |
|---|---|---|
| Cuestionario de triage | HU-03, entidades clave | `CuestionarioTriage` (raíz del Agregado) |
| Responder el cuestionario (síntomas) | HU-03, narrativa ("cuestionario de síntomas") | `RespuestaTriage(sintoma, intensidad)` creadas por `CuestionarioTriageFactory.crear(...)` |
| Nivel de urgencia (bajo, medio, alto) | HU-03 (criterio de aceptación) | `NivelUrgencia` (`BAJO`, `MEDIO`, `ALTO`) |
| Clasificar automáticamente | HU-03 (criterio de aceptación) | `ClasificacionTriageService.clasificar(...)` → `CuestionarioTriage.clasificar(...)` |
| Alerta clínica | HU-03, HU-09, entidades clave | `AlertaClinica` (entidad interna del agregado) |
| Generar una alerta (si el nivel es alto) | HU-03, regla de negocio | `CuestionarioTriage.generarAlerta(...)` |
| Notificación en tiempo real a enfermería | HU-04, requisito no funcional | `NotificadorEnfermeria.notificar(...)`, `AlertaClinica.isNotificada()` |
| Alertas clínicas activas / atenderlas | HU-09 | `AlertaClinica.estaActiva()`, `CuestionarioTriage.atenderAlerta(enfermeriaId, fecha)` |
| Por orden de urgencia | HU-09 | `NivelUrgencia.prioridad()` |
| Triage completado | Regla "No puede iniciarse una consulta sin un triage completado" | `CuestionarioTriage.estaCompletado()` |
| Paciente, Cita | Entidades clave (otros subdominios) | Solo ids: `pacienteId`, `citaId` |
| Personal de enfermería | Actor | Solo id: `enfermeriaId` |

Términos descartados: no se usan "encuesta", "formulario", "prioridad" (como entidad), "notificación"
(como entidad) ni "evento", porque los requisitos dicen "cuestionario de triage", "nivel de urgencia"
y "alerta clínica". "Prioridad" solo aparece como método de `NivelUrgencia` para ordenar el panel.

## Paso 4 · Límite del Agregado

- **Raíz:** `CuestionarioTriage`. Es la única puerta de entrada para modificar el agregado.
- **Dentro del agregado:**
  - `RespuestaTriage`: Value Object (record inmutable). La lista se copia con `List.copyOf` y no se puede modificar desde afuera.
  - `NivelUrgencia`: Value Object (record con validación bajo/medio/alto).
  - `AlertaClinica`: entidad interna (tiene identidad propia por `id` y ciclo de vida activa → atendida). Su constructor y sus métodos de cambio son package-private; solo se crea con `CuestionarioTriage.generarAlerta(...)`.
- **Invariantes que protege la raíz:**
  - Un cuestionario se clasifica una sola vez.
  - Solo un triage con nivel **alto** genera alerta clínica, y como máximo una.
  - Una alerta ya atendida no se puede volver a atender.
  - `clasificar`, `generarAlerta` y `marcarAlertaNotificada` son package-private: desde fuera del subdominio solo se llega a ellos pasando por `ClasificacionTriageService`, que garantiza que la alerta se genera en la misma operación que la clasificación (regla: "menos de 1 minuto").
- **Referencias a otros subdominios (solo por id, nunca por objeto):**
  - Paciente → `pacienteId`
  - Cita (Agenda, Dev 1) → `citaId`
  - Personal de enfermería → `enfermeriaId`
  - El canal de notificación se modela con la interfaz `NotificadorEnfermeria`; cuando exista la
    infraestructura (WebSocket, correo…), se conecta con un adaptador sin que el dominio cambie.
- **Relación con los otros subdominios:**
  - Agenda no depende de Triage y alertas (requisito no funcional: el agendamiento sigue funcionando
    aunque el módulo de alertas esté caído). Si `NotificadorEnfermeria` falla, la alerta no se pierde:
    queda activa con `notificada = false`.
  - Para iniciar una consulta, quien la inicie puede preguntar `estaCompletado()` usando el `citaId`.
