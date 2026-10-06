package com.telemedicina.telemedicina.triagealertas.api;

public class CuestionarioTriageNoEncontradoException extends RuntimeException {

    public CuestionarioTriageNoEncontradoException(String id) {
        super("No existe el cuestionario de triage " + id);
    }
}
