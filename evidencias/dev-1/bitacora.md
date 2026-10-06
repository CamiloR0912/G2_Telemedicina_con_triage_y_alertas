# Dev 1 · Subdominio Agenda — Bitácora de tiempo

Dueña del subdominio: Maria José Espinosa · Rama: `dev-1/agenda`

| Paso | Tiempo estimado | Tiempo real | Fecha | Archivos | Commit |
|---|---|---|---|---|---|
| 1 · Lenguaje Ubicuo | ~20 min | | | `agenda/Cita.java`, `agenda/EstadoCita.java`, `evidencias/dev-1/lenguaje-ubicuo.md` | `feat(agenda): lenguaje ubicuo de Cita y EstadoCita` |
| 2 · Value Object | ~45 min | | | `agenda/FranjaHoraria.java`, `FranjaHorariaTest.java` | `feat(agenda): value object FranjaHoraria` |
| 3 · Servicio de Dominio | ~35 min | | | `agenda/DisponibilidadService.java`, `agenda/FranjaNoDisponibleException.java`, `DisponibilidadServiceTest.java` | `feat(agenda): servicio de dominio DisponibilidadService` |
| 4 · Límite del Agregado | ~15 min | | | `evidencias/dev-1/agregado.md` | `docs(agenda): limite del agregado Cita` |
| 5 · Factory | ~35 min | | | `agenda/CitaFactory.java`, `CitaFactoryTest.java` | `feat(agenda): factory CitaFactory` |
| 6 · Commit y push | ~5 min | | | — | Pull Request `dev-1/agenda` → `main` |

## Tests

| Clase de test | Tests | Paso |
|---|---|---|
| `FranjaHorariaTest` | 8 | 2 |
| `DisponibilidadServiceTest` | 10 | 3 |
| `CitaFactoryTest` | 6 | 5 |

## Capturas

En `evidencias/dev-1/capturas/`:

- `paso1-lenguaje-ubicuo.png` — `Cita.java` completo
- `paso2-value-object.png` y `paso2-test.png`
- `paso3-servicio.png` y `paso3-test.png`
- `paso4-agregado.png`
- `paso5-factory.png` y `paso5-test.png`

## Observaciones

<!-- Anotar aquí si algún paso tomó mucho más de lo estimado y por qué. -->
