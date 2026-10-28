# Pruebas de la API con Bruno

Colección de [Bruno](https://www.usebruno.com/) con los 14 endpoints de la API (Agenda, Historia
clínica, Triage y alertas), incluidos los casos de error: 48 peticiones y 89 tests.

## Cómo usarla

1. Arranca la API desde `telemedicina/`: `mvn spring-boot:run` (o `./mvnw spring-boot:run`).
   Escucha en `http://localhost:8080`.
2. En Bruno: **Open Collection** → selecciona esta carpeta `bruno/`.
3. Elige el entorno **local** (arriba a la derecha).
4. Ejecuta la colección o cada carpeta con **Run** (clic derecho → Run). Las peticiones de una carpeta
   dependen de las anteriores (ids y fechas guardados con `bru.setVar`), así que se ejecutan en orden.

Desde la terminal (sin abrir Bruno):

```bash
cd bruno
npx @usebruno/cli run --env local                    # toda la colección
npx @usebruno/cli run agenda --env local             # una sola carpeta
```

## Datos de prueba

Los datos viven en memoria: se borran al reiniciar la API. Cada ejecución genera sus propios médicos,
pacientes, consultas y citas de triage, así que la colección se puede correr varias veces seguidas.

Historia clínica le pregunta a Agenda si el médico tiene cita con el paciente. Por eso la carpeta
empieza agendando una cita entre `med-1` y `pac-1` (petición `00`).

| Variable          | Valor   | Uso                                         |
|-------------------|---------|---------------------------------------------|
| `medicoConCita`   | `med-1` | Tiene cita con `pac-1` → acceso permitido    |
| `medicoSinCita`   | `med-2` | Nunca recibe cita con `pac-1` → 403          |
| `pacienteConCita` | `pac-1` | Paciente usado en historia clínica          |
| `enfermeriaId`    | `enf-1` | Enfermera que atiende la alerta             |

## Endpoints cubiertos

**Agenda** (`agenda/`)

| Método | Ruta                                   | Casos                                            |
|--------|----------------------------------------|--------------------------------------------------|
| POST   | `/api/citas`                           | 201; 409 médico/paciente ocupado; 400 pasado, franja invertida, sin especialidad; cupo liberado al cancelar/reprogramar |
| GET    | `/api/citas/{id}`                      | 200, 404                                         |
| GET    | `/api/citas?medicoId=&fecha=`          | agenda del día (HU-08), ordenada y sin canceladas |
| GET    | `/api/citas/disponibilidad?medicoId=&pacienteId=&inicio=&fin=` | ocupada / libre en el borde |
| POST   | `/api/citas/{id}/reprogramar`          | 200; 400 misma franja; 409 franja ocupada o cita cancelada |
| POST   | `/api/citas/{id}/cancelar`             | 200; 409 ya cancelada                            |

**Historia clínica** (`historia-clinica/`)

| Método | Ruta                                             | Casos                                  |
|--------|--------------------------------------------------|----------------------------------------|
| POST   | `/api/historias-clinicas`                        | 201, 409 duplicada, 400 grupo inválido |
| GET    | `/api/historias-clinicas/{pacienteId}`           | 200 + auditoría, 403, 404, 400 sin `X-Medico-Id` |
| POST   | `/api/historias-clinicas/{pacienteId}/consultas` | 201, 409 duplicada, 400 sin diagnóstico, 403 |

**Triage y alertas** (`triage-alertas/`)

| Método | Ruta                               | Casos                                        |
|--------|------------------------------------|----------------------------------------------|
| POST   | `/api/triage`                      | bajo, medio, alto + alerta; 400 sin respuestas, intensidad > 10, síntoma repetido |
| GET    | `/api/triage/{id}`                 | 200, 404                                     |
| GET    | `/api/triage/completado?citaId=`   | `true` / `false`                             |
| GET    | `/api/alertas/activas`             | contiene la alerta alta; sin la atendida     |
| POST   | `/api/triage/{id}/alerta/atender`  | 200, 409 ya atendida, 409 sin alerta         |
