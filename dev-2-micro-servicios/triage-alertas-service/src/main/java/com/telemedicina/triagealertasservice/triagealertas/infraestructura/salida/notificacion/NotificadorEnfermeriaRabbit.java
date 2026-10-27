package com.telemedicina.triagealertasservice.triagealertas.infraestructura.salida.notificacion;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.telemedicina.triagealertasservice.triagealertas.aplicacion.NotificadorEnfermeria;
import com.telemedicina.triagealertasservice.triagealertas.dominio.AlertaClinica;
import com.telemedicina.triagealertasservice.triagealertas.infraestructura.configuracion.RabbitConfig;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador secundario real del puerto {@link NotificadorEnfermeria} (reemplaza al
 * {@code NotificadorEnfermeriaLog} de la versión hexagonal). Envío directo, sin outbox: la alerta
 * tiene que llegar "en segundos" (requisito no funcional). Si RabbitMQ está caído, el envío lanza
 * excepción y el caso de uso deja la alerta activa y sin notificar, visible en el panel (HU-09).
 */
@Component
class NotificadorEnfermeriaRabbit implements NotificadorEnfermeria {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEnfermeriaRabbit.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    NotificadorEnfermeriaRabbit(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notificar(String cuestionarioId, String pacienteId, String citaId, AlertaClinica alerta) {
        String mensaje = objectMapper.writeValueAsString(Map.of(
                "alertaId", alerta.getId(),
                "cuestionarioId", cuestionarioId,
                "pacienteId", pacienteId,
                "citaId", citaId,
                "nivelUrgencia", alerta.getNivelUrgencia().valor(),
                "fechaGeneracion", alerta.getFechaGeneracion().toString()));
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_ALERTAS, RabbitConfig.RUTA_URGENCIA_ALTA, mensaje);
        log.info("Alerta clínica {} enviada a RabbitMQ · paciente {} · cita {}", alerta.getId(), pacienteId, citaId);
    }
}
