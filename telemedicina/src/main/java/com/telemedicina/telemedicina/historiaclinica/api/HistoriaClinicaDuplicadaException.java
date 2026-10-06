package com.telemedicina.telemedicina.historiaclinica.api;

public class HistoriaClinicaDuplicadaException extends RuntimeException {

    public HistoriaClinicaDuplicadaException(String pacienteId) {
        super("El paciente " + pacienteId + " ya tiene historia clínica registrada");
    }
}
