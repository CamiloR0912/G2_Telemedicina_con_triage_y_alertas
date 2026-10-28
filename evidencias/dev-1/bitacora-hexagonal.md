# Dev 1 · Subdominio Agenda — Bitácora de tiempo (Hexagonal)

Rama: `dev-1/hexagonal` (creada a partir de `dev-1/agenda`)

| Paso | Tiempo estimado | Tiempo real | Fecha | Archivos | Commit |
|---|---|---|---|---|---|
| 0 · Preparación | ~15 min | | | — | — |
| 1 · Auditoría de puertos y adaptadores | ~15 min | | | `evidencias/dev-1/hexagonal.md` | `docs(agenda): auditoria de puertos y adaptadores` |
| 2 · Puerto primario | ~30 min | | | `aplicacion/CitaUseCase.java`, `aplicacion/CitaService.java`, `infraestructura/entrada/web/*` | `feat(agenda): puerto primario CitaUseCase` |
| 3 · Puerto secundario + adaptador JPA | ~45 min | | | `aplicacion/RepositorioCitas.java`, `infraestructura/salida/persistencia/*`, `dominio/Cita.java` (`reconstituir`), `pom.xml`, `application.yaml` | `feat(agenda): puerto RepositorioCitas y adaptador JPA` |
| 4 · Paquetes por rol hexagonal | ~35 min | | | `agenda/dominio/*`, `agenda/aplicacion/*`, `agenda/infraestructura/*` | `refactor(agenda): paquetes por rol hexagonal` |
| 5 · Test del núcleo con Fake | ~25 min | | | `RepositorioCitasFalso.java`, `CitaServiceConFalsoTest.java` | `test(agenda): CitaService con repositorio falso` |
| 6 · Commit y push | ~5 min | | | — | `git push --set-upstream origin dev-1/hexagonal` |

## Tests

| Clase de test | Tests | Usa Spring |
|---|---|---|
| `FranjaHorariaTest` | 8 | No |
| `DisponibilidadServiceTest` | 10 | No |
| `CitaFactoryTest` | 6 | No |
| `CitaServiceConFalsoTest` | 6 | No (ni Mockito) |
| `TelemedicinaApplicationTests` | 1 | Sí (carga el contexto) |

## Capturas

En `evidencias/dev-1/capturas/`:

- `hex-paso2-puerto-primario.png` — `CitaUseCase.java` y el constructor de `CitaController`
- `hex-paso3-adaptador.png` — `RepositorioCitas.java` y `CitaRepositoryJpaAdapter.java`
- `hex-paso4-paquetes.png` — árbol de carpetas de `agenda`
- `hex-paso5-fake.png` y `hex-paso5-tests.png` — `CitaServiceConFalsoTest` en verde

## Observaciones

<!-- Anotar aquí si algún paso tomó mucho más de lo estimado y por qué. -->
