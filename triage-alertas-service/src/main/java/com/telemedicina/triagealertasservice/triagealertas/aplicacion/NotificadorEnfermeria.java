package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

import com.telemedicina.triagealertasservice.triagealertas.dominio.AlertaClinica;

/**
 * Puerto secundario: lo único que Triage y alertas necesita del mundo exterior para cumplir HU-04,
 * avisar en tiempo real al personal de enfermería. El núcleo no sabe si es WebSocket, correo o SMS;
 * eso lo resuelve un adaptador en {@code infraestructura.salida.notificacion} (hoy, RabbitMQ).
 * Lleva el id de la cita porque Agenda la usa para priorizar la cita del paciente.
 */
@FunctionalInterface
public interface NotificadorEnfermeria {

    void notificar(String cuestionarioId, String pacienteId, String citaId, AlertaClinica alerta);
}
