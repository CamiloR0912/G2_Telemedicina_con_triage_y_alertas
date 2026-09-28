# Event Storming informal — G2 · Telemedicina con triage y alertas clínicas

## 1. Lista de eventos de dominio (en pasado, orden cronológico)

| # | Evento de dominio | Origen (HU / regla) |
|---|---|---|
| 1 | Paciente se registró con su historia clínica básica | HU-01 |
| 2 | Cita de telemedicina fue agendada | HU-02 |
| 3 | Cita fue cancelada o reprogramada | HU-07 |
| 4 | Cupo de la cita cancelada fue liberado para otro paciente | Regla de negocio |
| 5 | Cuestionario de triage fue respondido por el paciente | HU-03 |
| 6 | Nivel de urgencia fue clasificado (bajo / medio / alto) | HU-03 (criterio de aceptación) |
| 7 | Alerta clínica fue generada (cuando la urgencia es alta) | HU-03 (criterio de aceptación) |
| 8 | Personal de enfermería fue notificado en tiempo real de la alerta | HU-04 |
| 9 | Panel de alertas activas fue consultado por enfermería | HU-09 |
| 10 | Consulta fue iniciada (solo si el triage ya estaba completado) | Regla de negocio |
| 11 | Historia clínica del paciente fue consultada por el médico | HU-06 |
| 12 | Acceso a la historia clínica fue registrado (fecha, hora, médico) | HU-06 (criterio de aceptación) |
| 13 | Diagnóstico, notas y receta simplificada fueron registrados | HU-05 |
| 14 | Agenda del día fue consultada por el médico | HU-08 |

## 2. Eventos pivote

Se marcan como pivote porque, en cada caso, (a) requieren información de más de una entidad, o (b) el responsable de lo que sigue cambia.

- **Cita de telemedicina fue agendada.**
  Pivote porque arranca un ciclo de vida propio (agendamiento) que, según el requisito no funcional, debe seguir funcionando incluso si el módulo de alertas está caído — es decir, no puede depender de las otras costuras.

- **Nivel de urgencia fue clasificado como alto → Alerta clínica fue generada.**
  Pivote porque aquí cambia el responsable: pasa de ser un evento centrado en el paciente/cuestionario a un evento que activa a otro actor (enfermería) y otra entidad (Alerta clínica). No lo puede resolver el Cuestionario de triage por sí solo.

- **Historia clínica del paciente fue consultada por el médico.**
  Pivote porque cruza hacia un subdominio con reglas propias y más estrictas (acceso restringido, auditado por rol) — necesita validar contra la Cita del médico y, además, dispara un evento adicional de auditoría (②).

Estos tres pivotes son, a primera vista, la costura natural entre **Agenda**, **Triage y alertas**, e **Historia clínica** — pero eso lo confirman formalmente en la sección 3, no aquí.

## 3. Bounded Contexts candidatos
 
- **Agenda**
  Se origina en el evento pivote *"Cita de telemedicina fue agendada"*. Dentro de este contexto, "estado" siempre significa el estado de la Cita (agendada / cancelada / reprogramada), y el ciclo de vida es independiente: según el requisito no funcional, el agendamiento debe seguir funcionando aunque el módulo de alertas esté caído, así que no puede depender de Triage ni de Historia clínica para operar.
- **Triage y alertas**
  Se origina en la pareja de eventos pivote *"Nivel de urgencia fue clasificado"* → *"Alerta clínica fue generada"*. Aquí "urgencia" tiene un único significado (bajo/medio/alto según el cuestionario), y el contexto puede evolucionar solo — por ejemplo, cambiar la lógica de clasificación — sin tocar Agenda ni Historia clínica.
- **Historia clínica**
  Se origina en el evento pivote *"Historia clínica del paciente fue consultada por el médico"* (que a su vez dispara *"Acceso a la historia clínica fue registrado"*). Es el contexto con reglas propias más estrictas — acceso restringido y auditado por rol — y su corte no sigue una capa técnica ni una pantalla del boceto, sino la regla de negocio real ("un médico solo puede ver la historia clínica de pacientes con cita activa o histórica con él").

## 4. Asignación de Subdominios
* **Dev 1 - Maria José Espinosa**: Subdominio de Agenda
* **Dev 2 - Anderson Carvajal**: Subdominio de Triage y alertas
* **Dev 3 - Camilo Ramirez**: Subdominio de Historia clínica

## 5. Repositorio de GitHub
* **URL**: https://github.com/CamiloR0912/G2_Telemedicina_con_triage_y_alertas