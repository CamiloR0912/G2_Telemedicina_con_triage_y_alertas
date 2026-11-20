# ADR-001 · Descomponer en tres microservicios por bounded context, con base de datos propia

- **Estado:** Aceptada · implementada
- **Fecha:** 2026-11-11
- **Evidencia en el código:** `agenda-service/`, `triage-alertas-service/`, `historia-clinica-service/`; cada `application.yml` declara su propio puerto (8081, 8082, 8083) y su propia base H2 en archivo.

## Contexto

El Event Storming (`Notas_equipo.md`) identificó tres eventos pivote que separan tres bounded contexts:
Agenda, Triage y alertas, e Historia clínica. Cada uno tiene su propio lenguaje ("estado" de la cita,
"urgencia" del triage, "acceso auditado" a la historia) y reglas que cambian por razones distintas.
El requisito no funcional exige que **el agendamiento siga funcionando aunque el módulo de alertas esté
caído**, algo que un único proceso no puede garantizar.

## Decisión

Extraer el monolito modular `telemedicina/` en tres servicios Spring Boot independientes, uno por
bounded context, cada uno con su puerto, su proceso y su base de datos H2 en archivo (Database per
Service). Ningún servicio lee tablas de otro: la información cruza solo por gRPC o por eventos.

## Alternativas consideradas

1. **Mantener el monolito modular** (`telemedicina/`), con los tres módulos en un solo despliegue. Es más
   simple de operar y de probar, pero una caída del módulo de alertas tumba también el agendamiento.
2. **Un servicio por entidad** (Cita, Cuestionario, Alerta, Historia, Consulta). Daría servicios
   demasiado pequeños que necesitarían hablar entre sí para cada regla de negocio.
3. **Base de datos compartida** entre los tres servicios. Evita la consistencia eventual, pero acopla los
   esquemas y anula la independencia de despliegue.

## Consecuencias

- **Positivas:** Agenda sigue respondiendo si Triage o Historia están caídos; cada integrante evoluciona
  su servicio sin romper a los demás; cada servicio se prueba de forma aislada.
- **Negativas:**
  - Aparece la **consistencia eventual**: Historia puede tardar unos segundos en saber que un triage terminó.
  - El contrato gRPC (`agenda.proto`) está **duplicado** en `agenda-service` e `historia-clinica-service`;
    un cambio exige editar ambas copias.
  - Hay que operar cinco procesos y dos brokers en lugar de uno; arrancar el sistema localmente requiere
    Docker y unos minutos de compilación.
