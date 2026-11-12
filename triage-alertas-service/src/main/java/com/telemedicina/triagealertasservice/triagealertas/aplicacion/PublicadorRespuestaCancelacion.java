package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

/**
 * Puerto secundario: la respuesta de Triage a la Saga de cancelación. El núcleo no sabe que
 * detrás hay una tabla outbox y Kafka.
 */
public interface PublicadorRespuestaCancelacion {

    void cancelacionConfirmada(String citaId);

    void cancelacionRechazada(String citaId, String motivo);
}
