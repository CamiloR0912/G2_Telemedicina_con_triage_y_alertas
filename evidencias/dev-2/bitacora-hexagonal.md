# Dev 2 · Subdominio Triage y alertas — Bitácora de tiempo (Hexagonal)

Rama: `dev-2/hexagonal` (creada a partir de `dev-2`)

| Paso | Tiempo estimado | Tiempo real | Fecha | Archivos | Commit |
|---|---|---|---|---|---|
| 0 · Preparación | ~15 min | | | — | — |
| 1 · Auditoría de puertos y adaptadores | ~15 min | | | `evidencias/dev-2/hexagonal.md` | `docs(triage): auditoria de puertos y adaptadores` |
| 2 · Puerto primario | ~30 min | | | `aplicacion/CuestionarioTriageUseCase.java`, `aplicacion/CuestionarioTriageService.java`, `infraestructura/entrada/web/*` | `feat(triage): puerto primario CuestionarioTriageUseCase` |
| 3 · Puerto secundario + adaptador JPA | ~45 min | | | `aplicacion/RepositorioCuestionariosTriage.java`, `aplicacion/NotificadorEnfermeria.java`, `infraestructura/salida/persistencia/*`, `dominio/CuestionarioTriage.java` y `dominio/AlertaClinica.java` (`reconstituir`), `dominio/ClasificacionTriageService.java` (sin notificador), `pom.xml`, `application.yaml` | `feat(triage): puerto RepositorioCuestionariosTriage y adaptador JPA` |
| 4 · Paquetes por rol hexagonal | ~35 min | | | `triagealertas/dominio/*`, `triagealertas/aplicacion/*`, `triagealertas/infraestructura/*` | `refactor(triage): paquetes por rol hexagonal` |
| 5 · Test del núcleo con Fake | ~25 min | | | `RepositorioCuestionariosTriageFalso.java`, `NotificadorEnfermeriaFalso.java`, `CuestionarioTriageServiceConFalsoTest.java` | `test(triage): CuestionarioTriageService con repositorio falso` |
| 6 · Commit y push | ~5 min | | | — | `git push --set-upstream origin dev-2/hexagonal` |
| 7 · Reto: adaptador secundario simulado (opcional) | +20–25 min | | | `infraestructura/salida/notificacion/NotificadorEnfermeriaLog.java` | `feat(triage): adaptador NotificadorEnfermeriaLog` |

## Tests

| Clase de test | Tests | Usa Spring |
|---|---|---|
| `NivelUrgenciaTest` | 9 | No |
| `RespuestaTriageTest` | 5 | No |
| `CuestionarioTriageTest` | 7 | No |
| `CuestionarioTriageFactoryTest` | 5 | No |
| `ClasificacionTriageServiceTest` | 6 | No |
| `CuestionarioTriageServiceConFalsoTest` | 7 | No (ni Mockito) |
| `TelemedicinaApplicationTests` | 1 | Sí (carga el contexto y crea las tablas en H2) |

Total: 40 tests en verde con `./mvnw test`.

## Capturas

En `capturas`:

- `hex-paso2-usecase.png` — `CuestionarioTriageUseCase.java` y el constructor de `CuestionarioTriageController`
- `hex-paso3-adaptador.png` — `RepositorioCuestionariosTriage.java` y `CuestionarioTriageRepositoryJpaAdapter.java`
- `hex-paso4-paquetes.png` — árbol de carpetas de `triagealertas`
- `hex-paso5-fake.png` y `hex-paso5-tests.png` — `CuestionarioTriageServiceConFalsoTest` en verde

## Observaciones

<!-- Anotar aquí si algún paso tomó mucho más de lo estimado y por qué. -->
