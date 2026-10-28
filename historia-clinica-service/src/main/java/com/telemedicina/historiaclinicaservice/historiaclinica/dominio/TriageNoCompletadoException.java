package com.telemedicina.historiaclinicaservice.historiaclinica.dominio;

/** Regla de negocio: "No puede iniciarse una consulta sin un triage completado". */
public class TriageNoCompletadoException extends RuntimeException {

    public TriageNoCompletadoException(String citaId) {
        super("La cita " + citaId + " no tiene un triage completado: no se puede registrar la consulta");
    }
}
