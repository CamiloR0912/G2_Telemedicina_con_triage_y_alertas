# Pruebas de los microservicios con Bruno

## Arranque con Docker

Desde la raiz del repositorio:

```bash
docker compose up -d --build --wait --wait-timeout 240
docker compose ps
```

Docker compila los tres servicios con Maven y Java 25; no necesitas instalarlos en tu equipo.
La primera compilacion descarga las imagenes y dependencias.

| Servicio | Direccion local |
|---|---|
| Agenda HTTP | http://localhost:8081 |
| Agenda gRPC | localhost:9090 |
| Triage y alertas | http://localhost:8082 |
| Historia clinica | http://localhost:8083 |
| Kafka | localhost:9092 |
| RabbitMQ AMQP | localhost:5672 |
| RabbitMQ administracion | http://localhost:15672 |

RabbitMQ usa `telemedicina` / `telemedicina-local` (credenciales de desarrollo).
Dentro de Docker, los servicios se conectan usando los nombres de Compose.
Kafka anuncia una direccion interna y otra para clientes del equipo anfitrion.
Los healthchecks verifican los brokers y los puertos de los servicios; las pruebas Bruno
validan que las operaciones y la comunicacion entre servicios realmente funcionen.

Para detener: `docker compose down`. Las bases H2 y los brokers conservan sus datos en
volumenes separados. `docker compose down -v` elimina TODOS esos datos de desarrollo.
Las carpetas `data/` existentes fuera de Docker no se importan ni se modifican.
No arranques simultaneamente servicios locales que ocupen los mismos puertos.

## Ejecutar las pruebas

En Bruno abre esta carpeta como coleccion, selecciona el entorno **local** y ejecuta
**Run** sobre la coleccion o una carpeta completa. Respeta el orden `seq`: las peticiones
comparten identificadores creados durante la ejecucion.

Con Node.js y npm:

```bash
cd bruno
npx --yes @usebruno/cli run --env local
npx --yes @usebruno/cli run agenda --env local
npx --yes @usebruno/cli run historia-clinica --env local
npx --yes @usebruno/cli run triage-alertas --env local
```

El entorno separa `agendaUrl`, `triageUrl` e `historiaUrl`. Ajustalas si cambias los puertos.
La coleccion corresponde a los microservicios, no a la aplicacion antigua `telemedicina/`.

## Casos y precondiciones

- Agenda: creacion, solapamientos, franja contigua, validaciones, consulta, agenda del dia,
  reprogramacion y cancelacion. La cita contigua se cancela antes de continuar.
  La agenda del dia usa `/api/medicos/{medicoId}/agenda?dia=...`.
- Historia: genera medico y paciente nuevos, agenda su cita, registra la historia,
  comprueba acceso y auditoria por gRPC, rechaza una consulta sin triage y completa
  el triage de esa misma cita antes de probar consultas y sus errores.
- Kafka: antes de registrar la consulta, se sondea Historia hasta 30 veces, con un segundo
  entre intentos. El sondeo usa diagnostico vacio para no guardar consultas; espera el
  cambio de 409 por triage pendiente a 400 por diagnostico obligatorio. Cualquier otro
  error falla inmediatamente. Consultar triage en Triage no prueba que Historia recibio el evento.
- Triage y alertas: clasificacion baja/media/alta, validaciones, consulta, panel y atencion.
  Las pruebas de `notificada: true` necesitan RabbitMQ disponible.

Los datos son persistentes. Cada corrida genera identificadores nuevos para evitar
conflictos entre ejecuciones; los datos de prueba se acumulan en los volumenes.
La coleccion no simula caidas de brokers ni comprueba por si sola la entrega a un consumidor
externo de enfermeria.
