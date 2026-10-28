package com.telemedicina.telemedicina.historiaclinica.dominio;

public class AccesoHistoriaDenegadoException extends RuntimeException {

    public AccesoHistoriaDenegadoException(String medicoId, String pacienteId) {
        super("El médico " + medicoId + " no tiene cita activa ni histórica con el paciente " + pacienteId);
    }
}
