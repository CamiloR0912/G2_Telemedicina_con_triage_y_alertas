package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

/**
 * Puerto primario: el paso de Triage en la Saga de cancelación de una cita (la inicia Agenda).
 */
public interface CancelacionCitaUseCase {

    /**
     * Regla de negocio · una cita con una alerta clínica de urgencia alta sin atender no puede
     * cancelarse: enfermería debe atenderla primero. Responde a Agenda con la decisión.
     *
     * @return {@code true} si la cancelación se confirmó, {@code false} si se rechazó
     */
    boolean evaluarCancelacion(String citaId);
}
