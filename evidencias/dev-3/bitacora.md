# Bitácora · Dev 3 (Camilo Ramirez) · Subdominio Historia clínica

> Completar **Tiempo real** y **Fecha** con lo que de verdad tomó cada paso; deben coincidir con la fecha de los commits
> (`git log --oneline --author="<usuario>"`).

| Paso | Qué se hizo | Tiempo estimado | Tiempo real | Fecha | Commit |
|---|---|---|---|---|---|
| 1 · Lenguaje Ubicuo | Glosario término → HU → clase (`lenguaje-ubicuo.md`) | ~20 min | 20 min | 3/10/2026 | `docs(historiaclinica): lenguaje ubicuo del subdominio` |
| 2 · Value Object | `RegistroAcceso` (record con validación en constructor compacto) + `RegistroAccesoTest` | ~45 min | 40 min | 3/10/2026 | `feat(historiaclinica): value object RegistroAcceso` |
| 3 · Servicio de Dominio | `ControlAccesoHistoriaService` + `HistorialCitas` + `AccesoHistoriaDenegadoException` + test | ~35 min | 45 min | 3/10/2026 | `feat(historiaclinica): servicio de dominio ControlAccesoHistoriaService` |
| 4 · Límite del Agregado | `HistoriaClinica` (raíz), `RegistroConsulta`, `package-info.java` + `HistoriaClinicaTest` | ~15 min | 35 min | 3/10/2026 | `feat(historiaclinica): limite del agregado HistoriaClinica` |
| 5 · Factory | `HistoriaClinicaFactory` + `HistoriaClinicaFactoryTest` | ~35 min | 40 min | 3/10/2026 | `feat(historiaclinica): factory HistoriaClinicaFactory` |
| 6 · Commit y push | Push de la rama `dev-3/historia-clinica` y PR hacia `main` | ~5 min | 10 min | 3/10/2026 | — |

## Capturas (en `capturas/`)

- [ ] `paso1-lenguaje-ubicuo.png`
- [ ] `paso2-value-object.png` y `paso2-tests.png`
- [ ] `paso3-servicio.png` y `paso3-tests.png`
- [ ] `paso4-agregado.png` y `paso4-tests.png`
- [ ] `paso5-factory.png` y `paso5-tests.png`
