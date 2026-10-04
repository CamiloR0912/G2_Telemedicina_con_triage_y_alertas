# Dev 3 · Historia clínica — Arquitectura Hexagonal (Taller Lección 4)

## Punto de partida

A diferencia de `rica-api`, el subdominio Historia clínica salió del taller de DDD **solo con el núcleo de
dominio** (`HistoriaClinica`, `RegistroConsulta`, `RegistroAcceso`, `ControlAccesoHistoriaService`,
`HistoriaClinicaFactory`, `HistorialCitas`): no había controller, ni servicio de aplicación, ni persistencia.
Por eso, en cada paso del taller, además de nombrar los puertos y adaptadores, hay que **crear** las piezas
que en `rica-api` ya existían.

## Paso 1 · Auditoría de puertos y adaptadores existentes

**¿Existe hoy un repositorio de Spring Data que cumpla el rol de puerto secundario?**
No. Historia clínica no tiene persistencia todavía: las historias solo viven en memoria durante los tests.
Falta un puerto secundario de persistencia, y debe diseñarse según lo que el caso de uso necesita
(buscar por paciente, saber si existe, guardar), no según los ~30 métodos de `JpaRepository`.

**`HistorialCitas`: ¿puerto primario o secundario?**
Secundario. Quien inicia la llamada es el núcleo (`ControlAccesoHistoriaService` pregunta a la Agenda si hay
cita); la Agenda (Dev 1) es algo externo que responde. Ya es un puerto explícito: una interfaz definida por el
núcleo, con un único método, en el lenguaje del dominio y sin ninguna tecnología. Lo que le falta es un
adaptador que la implemente fuera del núcleo.

**¿Hay un adaptador primario (controller)?**
No. Ningún actor externo (HTTP) puede usar hoy el subdominio. Hay que crear `HistoriaClinicaController`
como adaptador primario sobre Spring MVC / HTTP.

**¿Existe un puerto primario explícito?**
No. `ControlAccesoHistoriaService` es un servicio de **dominio**: recibe una `HistoriaClinica` ya cargada y no
sabe cargarla ni guardarla. Falta (a) un contrato `HistoriaClinicaUseCase` con lo que el subdominio promete
(HU-01, HU-05, HU-06) y (b) un servicio de aplicación `HistoriaClinicaService` que lo implemente
orquestando repositorio + servicio de dominio + factory.

**`HistoriaClinicaFactory`: ¿núcleo o adaptador?**
Núcleo. Sus `import` son solo `java.util.*`: no depende de Spring, JPA ni HTTP. Además, a diferencia de
`InvestigadorFactory`, no consulta ningún repositorio, así que pertenece al **dominio** (no a aplicación):
crea la raíz usando su constructor package-private, que es parte del límite del agregado.

| Pieza | Rol hexagonal | Estado antes del taller |
|---|---|---|
| `HistoriaClinicaUseCase` | Puerto primario | **Falta** |
| `HistoriaClinicaService` | Caso de uso (implementa el puerto primario) | **Falta** |
| `HistoriaClinicaController` | Adaptador primario (HTTP / Spring MVC) | **Falta** |
| `RepositorioHistoriasClinicas` | Puerto secundario (persistencia) | **Falta** |
| `HistoriaClinicaRepositoryJpaAdapter` | Adaptador secundario (Spring Data JPA) | **Falta** |
| `HistorialCitas` | Puerto secundario (hacia Agenda) | Ya existe |
| Implementación de `HistorialCitas` | Adaptador secundario (Agenda) | **Falta** (Agenda es de Dev 1) |
| `ControlAccesoHistoriaService`, `HistoriaClinicaFactory`, `HistoriaClinica`, `RegistroConsulta`, `RegistroAcceso` | Núcleo (dominio) | Ya existe |
