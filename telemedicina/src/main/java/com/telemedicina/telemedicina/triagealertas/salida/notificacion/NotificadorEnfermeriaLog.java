package com.telemedicina.telemedicina.triagealertas.infraestructura.salida.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.telemedicina.telemedicina.triagealertas.aplicacion.NotificadorEnfermeria;
import com.telemedicina.telemedicina.triagealertas.dominio.AlertaClinica;

/**
 * Adaptador secundario simulado del puerto {@link NotificadorEnfermeria}: por ahora solo deja la
 * alerta en el log, pendiente de una integración real (WebSocket, correo, SMS…). Cambiarlo no
 * toca ni una línea del núcleo.
 */
@Component
class NotificadorEnfermeriaLog implements NotificadorEnfermeria {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEnfermeriaLog.class);

    @Override
    public void notificar(String cuestionarioId, String pacienteId, AlertaClinica alerta) {
        log.info("Alerta clínica {} (urgencia {}) para enfermería · paciente {} · cuestionario {}",
                alerta.getId(), alerta.getNivelUrgencia().valor(), pacienteId, cuestionarioId);
    }
}
