# Dev 1 · Subdominio Agenda — Lenguaje Ubicuo

Auditoría de los nombres del código contra `Requisitos_G2.docx` (HU-02, HU-07, HU-08 y la regla "una cita cancelada libera el cupo para otro paciente").

## Glosario

| Término del negocio | En el código | Significado dentro de Agenda | Origen |
|---|---|---|---|
| Cita de telemedicina | `Cita` | Reserva de un paciente con un médico, para una especialidad, en una franja horaria. Raíz del agregado. | HU-02, entidad clave |
| Agendar | `CitaFactory.agendar(...)` | Crear una cita nueva y válida. | HU-02 |
| Especialidad | `Cita.especialidad` | Especialidad médica por la que se agenda la cita. | HU-02 ("agendar … por especialidad") |
| Horario / franja | `FranjaHoraria` | Intervalo inicio–fin que ocupa una cita. | HU-02 ("horario disponible") |
| Disponible | `DisponibilidadService.estaDisponible(...)` | La franja no se solapa con otra cita activa del mismo médico ni del mismo paciente. | HU-02 |
| Cupo | `Cita.ocupaCupoEn(franja)` | Lugar que una cita activa ocupa en la agenda del médico. | Regla de negocio |
| Cancelar | `Cita.cancelar()` | La cita pasa a `CANCELADA` y libera su cupo. | HU-07 |
| Reprogramar | `DisponibilidadService.reprogramar(...)` | Mover una cita activa a otra franja libre; pasa a `REPROGRAMADA`. | HU-07 |
| Estado | `EstadoCita` (`AGENDADA`, `REPROGRAMADA`, `CANCELADA`) | En Agenda "estado" siempre es el estado de la Cita. | Notas de equipo §3 |
| Cita activa | `Cita.estaActiva()` / `EstadoCita.esActiva()` | Cita agendada o reprogramada (no cancelada). | Regla de negocio, HU-06 la usa desde Historia clínica |
| Paciente, Médico | `pacienteId`, `medicoId` | Referencias por id a entidades de otros subdominios. | Entidades clave |

## Decisiones de nombre

- Se usa **`agendar`** (verbo de HU-02) y no `crear`/`save`, para que el código hable como el paciente.
- Se usa **`FranjaHoraria`** y no `Horario` ni `RangoFechas`: "franja" deja claro que tiene inicio y fin.
- Se usa **`cupo`** solo para la idea de "lugar ocupado en la agenda", que es como lo dice la regla de negocio.
- No aparece "urgencia", "triage" ni "alerta": esos términos pertenecen a otro Bounded Context. Agenda debe funcionar aunque Alertas esté caído (RNF).
- Fuera de alcance de esta etapa: **HU-08 (agenda del día)** es una consulta; `FranjaHoraria.esDelDia(dia)` ya deja el término listo para cuando haya repositorio (arquitectura hexagonal).
