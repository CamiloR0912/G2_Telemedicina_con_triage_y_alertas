package com.telemedicina.telemedicina.triagealertas.api;

import com.telemedicina.telemedicina.triagealertas.AlertaClinica;
import com.telemedicina.telemedicina.triagealertas.NotificadorEnfermeria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * HU-04 · mientras no exista un canal real (WebSocket, correo, SMS), la notificación a
 * enfermería se deja en el log de la aplicación.
 */
@Component
public class NotificadorEnfermeriaLog implements NotificadorEnfermeria {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEnfermeriaLog.class);

    @Override
    public void notificar(String cuestionarioId, String pacienteId, AlertaClinica alerta) {
        log.warn("ALERTA CLÍNICA {} · urgencia {} · paciente {} · cuestionario {}",
                alerta.getId(), alerta.getNivelUrgencia().valor(), pacienteId, cuestionarioId);
    }
}
