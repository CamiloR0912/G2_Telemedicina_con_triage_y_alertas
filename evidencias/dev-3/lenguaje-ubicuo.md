# Dev 3 · Historia clínica — Lenguaje Ubicuo y límite del Agregado

## Paso 1 · Lenguaje Ubicuo (auditoría contra Requisitos_G2)

| Término del negocio (requisitos) | Origen | Nombre en el código |
|---|---|---|
| Historia clínica | HU-01, HU-06, entidades clave | `HistoriaClinica` (raíz del Agregado) |
| Historia clínica **básica** (al registrarse) | HU-01 | `grupoSanguineo`, `alergias`, `antecedentes` creados por `HistoriaClinicaFactory.crear(...)` |
| Diagnóstico, notas y receta simplificada | HU-05 | `RegistroConsulta.diagnostico`, `.notas`, `.recetaSimplificada` |
| Registrar (la consulta) | HU-05 | `ControlAccesoHistoriaService.registrarConsulta(...)` → `HistoriaClinica.registrarConsulta(...)` |
| Consultar el historial clínico | HU-06 | `ControlAccesoHistoriaService.consultarHistoria(...)` |
| Acceso registrado con fecha, hora y médico | HU-06 (criterio de aceptación) | `RegistroAcceso(medicoId, fechaHora)` |
| Cita activa o histórica | Regla de negocio / HU-06 | `HistorialCitas.existeCitaActivaOHistorica(medicoId, pacienteId)` |
| Paciente, Médico, Cita, Consulta | Entidades clave (otros subdominios) | Solo ids: `pacienteId`, `medicoId`, `consultaId` |

Términos descartados: no se usan "expediente", "record", "log" ni "auditoría" como nombres de clase,
porque los requisitos dicen "historia clínica" y "acceso registrado".

## Paso 4 · Límite del Agregado

- **Raíz:** `HistoriaClinica`. Es la única puerta de entrada para modificar el agregado.
- **Dentro del agregado:**
  - `RegistroConsulta`: entidad interna (tiene identidad propia por `consultaId`). Su constructor es package-private y solo se crea con `HistoriaClinica.registrarConsulta(...)`.
  - `RegistroAcceso`: Value Object (record inmutable, sin identidad).
- **Invariantes que protege la raíz:**
  - Una consulta solo puede tener un registro clínico (`consultaId` no se repite).
  - El diagnóstico es obligatorio.
  - Las listas `getRegistrosAcceso()`, `getRegistrosConsulta()` y `getAlergias()` son de solo lectura desde afuera.
  - `registrarAcceso` y `registrarConsulta` son package-private: desde fuera del subdominio solo se llega a ellos pasando por `ControlAccesoHistoriaService`, que primero verifica la cita.
- **Referencias a otros subdominios (solo por id, nunca por objeto):**
  - Paciente → `pacienteId`
  - Médico → `medicoId`
  - Consulta → `consultaId`
  - Cita (Agenda, Dev 1) → no se guarda; se pregunta a través de la interfaz `HistorialCitas` usando `medicoId` + `pacienteId`.
    Cuando Agenda tenga su implementación, se conecta con un adaptador que implemente `HistorialCitas`, sin que Historia clínica conozca la clase `Cita`.
