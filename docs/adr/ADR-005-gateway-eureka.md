# ADR-005 · API Gateway como entrada única, con descubrimiento de servicios en Eureka

- **Estado:** Aceptada · implementada
- **Fecha:** 2026-11-11
- **Evidencia en el código:** `gateway/` (Spring Cloud Gateway WebFlux, puerto 8000, rutas `lb://` en `application.yml`), `eureka-server/` (`@EnableEurekaServer`, puerto 8761), dependencia `spring-cloud-starter-netflix-eureka-client` en los tres servicios.

## Contexto

Con tres servicios en tres puertos, cada cliente (Bruno o un futuro frontend) tendría que conocer qué
puerto atiende cada ruta. Si un servicio cambia de puerto o se ejecuta en varias instancias, todos los
clientes tendrían que actualizarse.

## Decisión

- Un **API Gateway** en el puerto 8000 enruta por prefijo: `/api/citas/**` y `/api/medicos/**` a
  `agenda-service`; `/api/triage/**` y `/api/alertas/**` a `triage-alertas-service`;
  `/api/historias-clinicas/**` a `historia-clinica-service`.
- Las rutas usan `lb://<nombre>`: el Gateway resuelve el nombre en **Eureka** y balancea entre instancias.
- Cada servicio se registra con su IP (`prefer-ip-address: true`).

## Alternativas consideradas

1. **Clientes llamando directo a cada puerto:** sin componentes extra, pero cada cliente queda acoplado a
   la topología y no hay un lugar común para seguridad o CORS.
2. **Nombres DNS de Docker Compose sin Eureka** (`http://agenda-service:8081`): funciona dentro de
   Compose, pero no balancea entre instancias ni sirve al ejecutar los servicios fuera de Docker.
3. **Proxy inverso estático (Nginx):** más liviano, pero requiere reconfigurarlo a mano cuando cambian
   instancias, y no se integra con Spring.

## Consecuencias

- **Positivas:** los clientes conocen un solo puerto; agregar instancias no requiere cambiar clientes;
  las rutas se prueban con `GatewayRutasTest` sin levantar el sistema.
- **Negativas:**
  - El Gateway es un **punto único de fallo**: si cae, ningún cliente llega a los servicios.
  - Justo después de arrancar, Eureka tarda unos segundos en mostrar un servicio y el Gateway responde
    **503**; por eso se redujeron los intervalos de refresco a 5 s.
  - El canal gRPC de Historia a Agenda **no** pasa por Eureka (ver ADR-002), así que hay dos formas de
    descubrir servicios en el mismo sistema.
