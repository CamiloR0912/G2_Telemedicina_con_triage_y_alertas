# G2 · Telemedicina con triage y alertas clínicas

Plataforma de telemedicina construida con microservicios Spring Boot: el paciente agenda una cita,
responde un triage y, si la urgencia es alta, se genera una alerta que prioriza su cita. Solo el médico
con una cita con el paciente puede abrir su historia clínica.

| Módulo | Qué es | Puerto |
|---|---|---|
| `gateway/` | API Gateway, entrada única | 8000 |
| `eureka-server/` | Registro de servicios | 8761 |
| `agenda-service/` | Citas y agenda del médico (HTTP + servidor gRPC) | 8081 · gRPC 9090 |
| `triage-alertas-service/` | Triage y alertas clínicas | 8082 |
| `historia-clinica-service/` | Historia clínica y consultas | 8083 |
| `telemedicina/` | Monolito modular original (talleres DDD y hexagonal); no se despliega | — |

La arquitectura, el diagrama y las decisiones están en [docs/cierre/ARQUITECTURA.md](docs/cierre/ARQUITECTURA.md)
y [docs/adr/](docs/adr/).

## Requisitos

- **Docker Desktop** con Docker Compose v2. Docker compila los servicios con Maven y Java 25, así que no
  hace falta instalarlos para levantar el sistema.
- `curl` para la demo (viene incluido en Windows 10/11, macOS y Linux).
- Para ejecutar las pruebas fuera de Docker: **Java 25** (cada servicio trae su propio `mvnw`).

## 1. Levantar el sistema

Desde la raíz del repositorio:

```bash
docker compose up -d --build --wait --wait-timeout 300
docker compose ps
```

La primera vez tarda varios minutos (descarga imágenes y dependencias). Cuando termina, todos los
contenedores aparecen como `healthy`. En http://localhost:8761 deben verse `AGENDA-SERVICE`,
`TRIAGE-ALERTAS-SERVICE`, `HISTORIA-CLINICA-SERVICE` y `GATEWAY`.

> Justo después de arrancar, Eureka puede tardar unos segundos en publicar un servicio: si el Gateway
> responde **503**, espera 10 a 20 segundos y repite la petición.

## 2. Demo: una petición que cruza dos servicios

Todas las peticiones entran por el Gateway (puerto 8000). Al consultar una historia clínica,
`historia-clinica-service` le pregunta a `agenda-service` **por gRPC** si el médico tiene una cita con
ese paciente.

**Paso 1 · Agendar una cita** (Gateway → Agenda). Responde `201` con el `id` de la cita:

```bash
curl -i -X POST http://localhost:8000/api/citas -H "Content-Type: application/json" -d "{\"pacienteId\":\"pac-demo-1\",\"medicoId\":\"med-demo-1\",\"especialidad\":\"Medicina general\",\"inicio\":\"2027-06-01T08:00:00\",\"fin\":\"2027-06-01T08:30:00\"}"
```

**Paso 2 · Registrar la historia clínica del paciente** (Gateway → Historia). Responde `201`:

```bash
curl -i -X POST http://localhost:8000/api/historias-clinicas -H "Content-Type: application/json" -d "{\"pacienteId\":\"pac-demo-1\",\"grupoSanguineo\":\"O+\",\"alergias\":[\"penicilina\"],\"antecedentes\":\"Hipertension controlada\"}"
```

**Paso 3 · El médico con cita consulta la historia** (Gateway → Historia → **gRPC** → Agenda). Responde
`200` con la historia del paciente:

```bash
curl -i http://localhost:8000/api/historias-clinicas/pac-demo-1 -H "X-Medico-Id: med-demo-1"
```

**Paso 4 · Un médico sin cita intenta lo mismo**. Agenda responde por gRPC que no hay cita, e Historia
devuelve `403 Forbidden`:

```bash
curl -i http://localhost:8000/api/historias-clinicas/pac-demo-1 -H "X-Medico-Id: med-sin-cita"
```

Los datos se guardan en volúmenes de Docker. Para repetir la demo desde cero, ejecuta
`docker compose down -v` y vuelve al paso 1, o cambia `pac-demo-1` y `med-demo-1` por otros valores.

## 3. Pruebas

**Pruebas unitarias**, en cada servicio:

```bash
cd agenda-service        # o triage-alertas-service, historia-clinica-service, gateway, eureka-server
./mvnw test              # en Windows: mvnw.cmd test
```

**Pruebas de integración con Bruno** (sistema levantado con Docker): agenda, historia clínica, triage,
Eureka/Gateway y la saga de cancelación. Ver [bruno/README.md](bruno/README.md).

```bash
cd bruno
npx --yes @usebruno/cli run --env local
```

## 4. Detener

```bash
docker compose down        # conserva los datos
docker compose down -v     # borra también las bases H2 y los datos de los brokers
```

## Documentación de cierre

| Documento | Contenido |
|---|---|
| [ARQUITECTURA.md](docs/cierre/ARQUITECTURA.md) | Arquitectura construida, diagrama y comunicación |
| [docs/adr/](docs/adr/) | Decisiones de arquitectura (5 implementadas, 2 propuestas) |
| [ATAM-LITE.md](docs/cierre/ATAM-LITE.md) | Escenarios de calidad, riesgos y trade-offs |
| [MATRIZ.md](docs/cierre/MATRIZ.md) | Trazabilidad requisito → servicio → evidencia |
| [CASO.md](docs/cierre/CASO.md) | Caso de estudio |
| [GOBERNANZA.md](docs/cierre/GOBERNANZA.md) | Reglas técnicas del equipo |
| [RESUMEN-EJECUTIVO.md](docs/cierre/RESUMEN-EJECUTIVO.md) | Resumen de una página |
| [REPARTO.md](docs/cierre/REPARTO.md) | Quién hizo qué |
