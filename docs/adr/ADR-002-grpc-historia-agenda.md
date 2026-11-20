# ADR-002 · Comunicación síncrona gRPC de Historia clínica a Agenda, protegida con Circuit Breaker

- **Estado:** Aceptada · implementada
- **Fecha:** 2026-11-11
- **Evidencia en el código:** `agenda-service/src/main/proto/agenda.proto`, `AgendaGrpcService` (servidor, puerto 9090), `HistorialCitasGrpcAdapter` (cliente con Resilience4j) en `historia-clinica-service`.

## Contexto

HU-06: un médico solo puede ver la historia clínica de un paciente si tiene con él una cita activa o
histórica. Ese dato vive en Agenda. La comprobación ocurre en cada consulta de la historia y es una
regla de **seguridad**: si la respuesta no está disponible, el acceso debe negarse, no concederse.

## Decisión

Historia clínica pregunta a Agenda **de forma síncrona** con gRPC (`ExisteCitaEntre(medicoId, pacienteId)`),
con un deadline de 5 s y un **Circuit Breaker** (ventana de 10 llamadas, abre con 50 % de fallos y espera
10 s). El contrato solo responde `existe: true/false`: Agenda nunca entrega la Cita completa. Si el
circuito está abierto o Agenda no responde, Historia devuelve error de servicio no disponible.

**Por qué síncrona:** la autorización necesita el dato actual en el momento de la petición. Una réplica
alimentada por eventos podría conceder acceso con una cita ya cancelada o negarlo a una recién agendada.

## Alternativas consideradas

1. **HTTP/REST con `RestClient`**: más fácil de depurar con Bruno, pero sin contrato tipado y con más
   bytes por llamada; el equipo ya había decidido gRPC en el Taller 6 para llamadas internas.
2. **Réplica local por eventos** (Agenda publica `cita.agendada` y Historia guarda su propia copia): evita
   el acoplamiento temporal, pero la regla de seguridad quedaría sujeta a consistencia eventual.
3. **Llamada sin Circuit Breaker**: más simple, pero si Agenda se cuelga, cada hilo de Historia esperaría
   el deadline completo y el servicio se degradaría en cascada.

## Consecuencias

- **Positivas:** autorización siempre con el dato actual; contrato tipado y mínimo; fallo rápido cuando
  Agenda está caída.
- **Negativas:**
  - **Acoplamiento temporal:** si Agenda está caída, ningún médico puede abrir historias clínicas (la
    consulta responde 503 en lugar de dar acceso).
  - El canal gRPC usa una **dirección fija** (`static://localhost:9090`, o `agenda-service:9090` en
    Docker): no se balancea con Eureka, así que escalar Agenda a varias instancias no ayuda a Historia.
  - El `.proto` está copiado en dos servicios y puede desincronizarse.
