package com.telemedicina.triagealertasservice.triagealertas.infraestructura.entrada.mensajeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.telemedicina.triagealertasservice.triagealertas.aplicacion.CancelacionCitaUseCase;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador primario de mensajería: Agenda inició la Saga de cancelación de una cita. Si Triage está
 * caído, el evento espera en Kafka y la Saga sigue sola cuando el servicio vuelve.
 */
@Component
class CitaEnCancelacionListener {

    private static final Logger log = LoggerFactory.getLogger(CitaEnCancelacionListener.class);

    private final CancelacionCitaUseCase cancelacionCitaUseCase;
    private final ObjectMapper objectMapper;

    CitaEnCancelacionListener(CancelacionCitaUseCase cancelacionCitaUseCase, ObjectMapper objectMapper) {
        this.cancelacionCitaUseCase = cancelacionCitaUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "citas.en-cancelacion")
    void alIniciarseCancelacion(String mensaje) {
        String citaId = objectMapper.readTree(mensaje).get("citaId").asString();
        boolean confirmada = cancelacionCitaUseCase.evaluarCancelacion(citaId);
        log.info("Saga: cancelación de la cita {} {}", citaId,
                confirmada ? "confirmada" : "rechazada (alerta urgente sin atender)");
    }
}
