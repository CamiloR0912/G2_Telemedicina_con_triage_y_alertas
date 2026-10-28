package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.entrada.mensajeria;

import com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion.HistoriaClinicaUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador primario de mensajería: cada evento {@code triage.completados} que publica Triage (desde
 * su outbox) entra por aquí al caso de uso. Ninguno de los dos servicios llama al otro.
 */
@Component
class TriageCompletadoListener {

    private static final Logger log = LoggerFactory.getLogger(TriageCompletadoListener.class);

    private final HistoriaClinicaUseCase historiaClinicaUseCase;
    private final ObjectMapper objectMapper;

    TriageCompletadoListener(HistoriaClinicaUseCase historiaClinicaUseCase, ObjectMapper objectMapper) {
        this.historiaClinicaUseCase = historiaClinicaUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "triage.completados")
    void alCompletarseTriage(String mensaje) {
        String citaId = objectMapper.readTree(mensaje).get("citaId").asString();
        historiaClinicaUseCase.registrarTriageCompletado(citaId);
        log.info("Evento recibido [triage.completados]: la cita {} ya puede iniciar consulta", citaId);
    }
}
