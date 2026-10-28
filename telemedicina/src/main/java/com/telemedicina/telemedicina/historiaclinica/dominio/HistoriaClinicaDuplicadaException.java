package com.telemedicina.telemedicina.historiaclinica.dominio;

public class HistoriaClinicaDuplicadaException extends RuntimeException {

    public HistoriaClinicaDuplicadaException(String pacienteId) {
        super("El paciente " + pacienteId + " ya tiene historia clínica registrada");
    }
}
