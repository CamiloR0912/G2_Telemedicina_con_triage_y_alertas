# ADR-007 · Dead Letter Topic y reintentos con backoff en los consumidores de Kafka

- **Estado:** **Propuesta** · NO implementada en el commit del corte
- **Fecha:** 2026-11-20
- **Evidencia en el código:** ninguna. No hay `DefaultErrorHandler`, `DeadLetterPublishingRecoverer` ni topics `.DLT` configurados.

## Contexto

Los consumidores `CitaEnCancelacionListener` (Triage), `RespuestaCancelacionListener` (Agenda) y
`TriageCompletadoListener` (Historia) procesan eventos de la saga y de la proyección (ADR-003). Si un
mensaje está mal formado o su procesamiento falla siempre, hoy se aplica el comportamiento por defecto
de Spring Kafka: hasta 10 intentos seguidos sin espera, y después el mensaje se registra en el log y
se salta. Un error en la saga podría dejar una cita en `EN_CANCELACION` para siempre, sin rastro fuera del log.

## Decisión propuesta

1. Configurar un `DefaultErrorHandler` con **backoff exponencial** (por ejemplo 1 s, 2 s y 4 s) para los
   errores transitorios.
2. Tras agotar los reintentos, enviar el mensaje a `<topic>.DLT` con un `DeadLetterPublishingRecoverer`.
3. Clasificar como no reintentables las excepciones de deserialización y de validación, para que vayan
   directo a la DLT.
4. Hacer idempotentes de forma explícita los consumidores de la saga, igual que el de `triage.completados`.

## Alternativas consideradas

1. **Reintentos infinitos sin DLT:** ningún mensaje se pierde, pero uno defectuoso bloquea la partición y
   detiene toda la saga.
2. **Topics de reintento no bloqueantes (`@RetryableTopic`):** no bloquean la partición, pero crean varios
   topics por consumidor y complican el orden de los eventos de una misma cita.

## Consecuencias (si se implementa)

- **Positivas:** los errores transitorios se recuperan solos; los permanentes quedan guardados en la DLT
  para revisarlos y reprocesarlos.
- **Negativas:** aparecen topics nuevos que alguien tiene que vigilar; sin un procedimiento de
  reprocesamiento, la DLT solo traslada el problema; y el backoff retrasa la confirmación de la saga
  hasta unos 7 s en el peor caso.
