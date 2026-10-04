package com.telemedicina.telemedicina.historiaclinica;

public class HistoriaClinicaNoEncontradaException extends RuntimeException {

    public HistoriaClinicaNoEncontradaException(String pacienteId) {
        super("No existe historia clínica para el paciente " + pacienteId);
    }
}
