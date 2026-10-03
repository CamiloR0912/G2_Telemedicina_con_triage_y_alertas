package com.telemedicina.telemedicina.historiaclinica;

/**
 * Lo único que Historia clínica necesita saber del subdominio Agenda: si existe una cita
 * (activa o histórica) entre un médico y un paciente. Se pregunta solo por ids; este
 * subdominio nunca conoce la entidad Cita.
 */
@FunctionalInterface
public interface HistorialCitas {

    boolean existeCitaActivaOHistorica(String medicoId, String pacienteId);
}
