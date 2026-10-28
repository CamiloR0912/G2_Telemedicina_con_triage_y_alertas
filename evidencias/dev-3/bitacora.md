# Bitácora · Dev 3 (Camilo Ramirez) · Subdominio Historia clínica

| Paso | Qué se hizo | Tiempo estimado | Tiempo real | Fecha | Commit |
|---|---|---|---|---|---|
| 1 · Lenguaje Ubicuo | Glosario término → HU → clase (`lenguaje-ubicuo.md`) | ~20 min | 20 min | 3/10/2026 | `docs: lenguaje ubicuo del subdominio` |
| 2 · Value Object | `RegistroAcceso` (record con validación en constructor compacto) + `RegistroAccesoTest` | ~45 min | 40 min | 3/10/2026 | `feat: value object RegistroAcceso` |
| 3 · Servicio de Dominio | `ControlAccesoHistoriaService` + `HistorialCitas` + `AccesoHistoriaDenegadoException` + test | ~35 min | 45 min | 3/10/2026 | `feat: servicio de dominio ControlAccesoHistoriaService` |
| 4 · Límite del Agregado | `HistoriaClinica` (raíz), `RegistroConsulta`, `package-info.java` + `HistoriaClinicaTest` | ~15 min | 35 min | 3/10/2026 | `feat: limite del agregado HistoriaClinica` |
| 5 · Factory | `HistoriaClinicaFactory` + `HistoriaClinicaFactoryTest` | ~35 min | 40 min | 3/10/2026 | `feat: factory HistoriaClinicaFactory` |
| 6 · Commit y push | Push de la rama `dev-3/historia-clinica` y PR hacia `main` | ~5 min | 10 min | 3/10/2026 | — |

## Capturas (en `capturas/`)

- [x] `paso1-lenguaje-ubicuo.png`
- [x] `paso2-value-object.png` y `paso2-tests.png`
- [x] `paso3-servicio.png` y `paso3-tests.png`
- [x] `paso4-agregado.png` y `paso4-tests.png`
- [x] `paso5-factory.png` y `paso5-tests.png`

## Arquitectura Hexagonal (Taller Lección 4) · rama `dev-3-hexagonal`

| Paso | Qué se hizo | Tiempo estimado | Tiempo real | Fecha | Commit |
|---|---|---|---|---|---|
| 0 · Preparación | Rama `dev-3-hexagonal` desde `dev-3`, `mvnw test` en verde | ~15 min | 5 min | 4/10/2026 | — |
| 1 · Auditoría | Puertos y adaptadores existentes/faltantes (`hexagonal.md`) | ~15 min | 20 min | 4/10/2026 | `docs(historiaclinica): auditoria de puertos y adaptadores` |
| 2 · Puerto primario | `HistoriaClinicaUseCase`, `HistoriaClinicaService`, `HistoriaClinicaController` + DTOs, adaptador simulado de Agenda, `HistoriaClinicaServiceTest` (Mockito) | ~30 min | 35 min | 4/10/2026 | `feat(historiaclinica): puerto primario HistoriaClinicaUseCase` |
| 3 · Puerto secundario + adaptador JPA | `RepositorioHistoriasClinicas`, `HistoriaClinicaRepository`, `HistoriaClinicaRepositoryJpaAdapter`, `HistoriaClinicaJpaEntity`, H2 | ~45 min | 55 min | 4/10/2026 | `feat(historiaclinica): puerto secundario minimo y adaptador JPA explicito` |
| 4 · Paquetes por rol | `dominio`, `aplicacion`, `infraestructura.entrada.web`, `infraestructura.salida.*`, `infraestructura.config` | ~35 min | 40 min | 4/10/2026 | `refactor(historiaclinica): reorganizar en paquetes por rol hexagonal` |
| 5 · Fake | `RepositorioHistoriasClinicasFalso` + `HistoriaClinicaServiceConFalsoTest` | ~25 min | 30 min | 4/10/2026 | `test(historiaclinica): probar el nucleo sin infraestructura con un Fake` |
| 6 · Commit y push | Bitácora y evidencias, push de `dev-3-hexagonal` | ~5 min | 5 min | 4/10/2026 | `docs(historiaclinica): bitacora y evidencias hexagonal` |

### Capturas hexagonal (en `capturas/`)

- [x] `hex-paso2-usecase.png` y `hex-paso2-tests.png`
- [x] `hex-paso3-adaptador-jpa.png` y `hex-paso3-tests.png`
- [x] `hex-paso4-paquetes.png` (árbol de paquetes) y `hex-paso4-tests.png`
- [x] `hex-paso5-fake.png` y `hex-paso5-tests.png`
