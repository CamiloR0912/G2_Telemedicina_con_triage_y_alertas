# Reparto del trabajo · G2 Telemedicina con triage y alertas

Los nombres coinciden con las identidades unificadas en `.mailmap` (`git shortlog -sn --all`).

## Integrantes y responsabilidades

| Integrante | Rol | Subdominio / servicio | Carpetas donde está su trabajo |
|---|---|---|---|
| **Maria Jose Espinosa** (Dev 1) | Desarrollo del contexto Agenda | Agenda → `agenda-service` | `dev-1-micro-servicios/` (rama `dev-1-micro-servicios`, integrada después en `agenda-service/`), `telemedicina/.../agenda/`, `evidencias/dev-1/` |
| **Anderson Carvajal** (Dev 2) | Desarrollo del contexto Triage y alertas | Triage y alertas → `triage-alertas-service` | `dev-2-micro-servicios/` (rama `dev2-micro-servicios`, integrada después en `triage-alertas-service/`), `telemedicina/.../triagealertas/`, `evidencias/dev-2/` |
| **Camilo Ramirez** (Dev 3) | Desarrollo del contexto Historia clínica e integración | Historia clínica → `historia-clinica-service`; además `gateway`, `eureka-server`, `docker-compose.yml`, `bruno/` | `historia-clinica-service/`, `gateway/`, `eureka-server/`, `bruno/`, `telemedicina/.../historiaclinica/`, `evidencias/dev-3/` |

## Evidencia en el historial

Un commit propio de cada integrante en su carpeta (`git log --all --use-mailmap -i --author="<nombre>" -- <carpeta>`):

| Integrante | Carpeta | Commit | Mensaje |
|---|---|---|---|
| Maria Jose Espinosa | `dev-1-micro-servicios/` | `9a677e5` | feat(agenda): priorizar cita con alertas de RabbitMQ |
| Maria Jose Espinosa | `telemedicina/.../agenda/` | `4248201` | refactor(agenda): paquetes por rol hexagonal |
| Anderson Carvajal | `dev-2-micro-servicios/` | `f3747c2` | Add files via upload |
| Anderson Carvajal | `telemedicina/.../triagealertas/` | `b9ebbde` | Test: triage fake |
| Camilo Ramirez | `historia-clinica-service/` | `4af8af1` | feat: created historia-clinica-service and docker compose |
| Camilo Ramirez | `gateway/`, `eureka-server/` | `0d82989` | feat: added gateway and eureka :) |

## Documentación de cierre

| Documento | Responsable |
|---|---|
| `ARQUITECTURA.md`, ADR-002, ADR-005 | Camilo Ramirez |
| ADR-001, ADR-003, `MATRIZ.md` | Maria Jose Espinosa |
| ADR-004, ADR-006, ADR-007, `ATAM-LITE.md` | Anderson Carvajal |
| `CASO.md`, `GOBERNANZA.md`, `RESUMEN-EJECUTIVO.md`, `README.md` | Todo el equipo |

## Reparto de la socialización (15 minutos)

| Minutos | Contenido | Presenta |
|---|---|---|
| 0 a 1 | Quiénes somos y el problema del dominio | Maria Jose Espinosa |
| 1 a 4 | Arquitectura construida: diagrama y servicios | Camilo Ramirez |
| 4 a 8 | Demo: petición que cruza Historia → Agenda (gRPC) y saga de cancelación | Anderson Carvajal y Camilo Ramirez |
| 8 a 10 | Decisiones clave (ADR-002 y ADR-003) y propuestas (ADR-006 y ADR-007) | Maria Jose Espinosa y Anderson Carvajal |
| 10 a 13 | Preguntas | Todo el equipo |
