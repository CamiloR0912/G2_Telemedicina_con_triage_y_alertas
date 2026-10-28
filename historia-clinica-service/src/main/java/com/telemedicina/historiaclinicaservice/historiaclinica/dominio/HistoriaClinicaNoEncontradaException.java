package com.telemedicina.historiaclinicaservice.historiaclinica.dominio;

public class HistoriaClinicaNoEncontradaException extends RuntimeException {

    public HistoriaClinicaNoEncontradaException(String pacienteId) {
        super("No existe historia clínica para el paciente " + pacienteId);
    }
}
