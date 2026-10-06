# Dev 1 · Subdominio Agenda — Límite del Agregado

## Raíz del agregado: `Cita`

```
┌──────────────── Agregado Cita ────────────────┐
│  Cita (raíz)                                  │
│   ├─ id : UUID                                │
│   ├─ pacienteId : String   -> (otro subdominio, solo id)
│   ├─ medicoId   : String   -> (otro subdominio, solo id)
│   ├─ especialidad : String                    │
│   ├─ franja : FranjaHoraria   (Value Object)  │
│   └─ estado : EstadoCita      (enum)          │
└───────────────────────────────────────────────┘
```

## Reglas que se respetan en el código

1. **Solo la raíz se modifica desde fuera.** `FranjaHoraria` es un `record` inmutable; para cambiar la franja hay que pasar por la Cita (`reprogramar`).
2. **Otros subdominios solo por id.** `Cita` guarda `pacienteId` y `medicoId`, nunca objetos `Paciente` o `Medico`. No importa nada de Triage, Alertas ni Historia clínica.
3. **Creación controlada.** El constructor de `Cita` es de paquete: fuera de `agenda.dominio` solo se puede obtener una Cita nueva mediante `CitaFactory.agendar(...)`. Desde la arquitectura hexagonal existe además `Cita.reconstituir(...)`, que solo usa el adaptador de persistencia para rehidratar una cita ya guardada. No crea citas nuevas.
4. **Invariantes de la propia Cita** (dentro de la raíz): no se cancela dos veces, no se reprograma una cita cancelada, no se reprograma a la misma franja.
5. **Invariantes entre varias Citas** (fuera de la raíz, en `DisponibilidadService`): un médico o un paciente no puede tener dos citas activas solapadas. Por eso `Cita.reprogramar(...)` es de paquete y el punto de entrada es `DisponibilidadService.reprogramar(...)`.
6. **Identidad.** Dos `Cita` son iguales si tienen el mismo `id` (`equals`/`hashCode`), sin importar su estado o franja.

## Cómo la usan otros subdominios

- **Historia clínica (Dev 3)** necesita saber si un médico tiene cita activa o histórica con un paciente. Debe preguntarlo por `medicoId` + `pacienteId`, sin recibir el objeto `Cita` completo.
- **Triage (Dev 2)** puede relacionar un cuestionario con una cita guardando solo el `citaId`.
