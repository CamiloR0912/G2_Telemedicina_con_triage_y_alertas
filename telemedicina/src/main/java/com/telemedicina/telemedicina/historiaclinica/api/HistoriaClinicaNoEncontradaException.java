package com.telemedicina.telemedicina.historiaclinica.api;

public class HistoriaClinicaNoEncontradaException extends RuntimeException {

    public HistoriaClinicaNoEncontradaException(String pacienteId) {
        super("El paciente " + pacienteId + " no tiene historia clínica registrada");
    }
}
