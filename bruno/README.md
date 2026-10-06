# Pruebas de la API con Bruno

Colección de [Bruno](https://www.usebruno.com/) con los 8 endpoints de la API, incluidos los casos de error.

## Cómo usarla

1. Arranca la API (`telemedicina/`): `./mvnw spring-boot:run`. Escucha en `http://localhost:8080`.
2. En Bruno: **Open Collection** → selecciona esta carpeta `bruno/`.
3. Elige el entorno **local** (arriba a la derecha).
4. Ejecuta cada carpeta completa con **Run** (clic derecho → Run). Las peticiones dependen de las
   anteriores (ids guardados con `bru.setVar`), así que se ejecutan en orden.

Desde la terminal (sin abrir Bruno):

```bash
cd bruno
npx @usebruno/cli run --env local                    # toda la colección
npx @usebruno/cli run historia-clinica --env local   # una sola carpeta
```

## Datos de prueba

La base de datos es H2 en memoria y la agenda está simulada en `application.yaml`
(`historia-clinica.agenda-simulada.citas: med-1:pac-1,med-2:pac-2`):

| Variable          | Valor   | Uso                                      |
|-------------------|---------|------------------------------------------|
| `medicoConCita`   | `med-1` | Tiene cita con `pac-1` → acceso permitido |
| `medicoSinCita`   | `med-2` | No tiene cita con `pac-1` → 403           |
| `pacienteConCita` | `pac-1` | Paciente usado en historia clínica       |
| `enfermeriaId`    | `enf-1` | Enfermera que atiende la alerta          |

Los ids de pacientes nuevos, consultas y citas de triage se generan en cada ejecución, así que la
colección se puede correr varias veces sin reiniciar la API.

## Endpoints cubiertos

**Historia clínica** (`historia-clinica/`)

| Método | Ruta                                          | Casos                               |
|--------|-----------------------------------------------|-------------------------------------|
| POST   | `/api/historias-clinicas`                     | 201, 409 duplicada, 400 grupo inválido |
| GET    | `/api/historias-clinicas/{pacienteId}`        | 200 + auditoría, 403, 404, 400 sin `X-Medico-Id` |
| POST   | `/api/historias-clinicas/{pacienteId}/consultas` | 201, 409 duplicada, 400 sin diagnóstico, 403 |

**Triage y alertas** (`triage-alertas/`)

| Método | Ruta                                  | Casos                                        |
|--------|---------------------------------------|----------------------------------------------|
| POST   | `/api/triage`                         | bajo, medio, alto + alerta; 400 sin respuestas, intensidad > 10, síntoma repetido |
| GET    | `/api/triage/{id}`                    | 200, 404                                     |
| GET    | `/api/triage/completado?citaId=`      | `true` / `false`                             |
| GET    | `/api/alertas/activas`                | contiene la alerta alta; sin la atendida     |
| POST   | `/api/triage/{id}/alerta/atender`     | 200, 409 ya atendida, 409 sin alerta         |
