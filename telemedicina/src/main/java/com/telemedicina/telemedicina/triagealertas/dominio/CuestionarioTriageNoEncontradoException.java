package com.telemedicina.telemedicina.triagealertas.dominio;

/** No existe un cuestionario de triage con el id pedido. */
public class CuestionarioTriageNoEncontradoException extends RuntimeException {

    public CuestionarioTriageNoEncontradoException(String id) {
        super("No existe el cuestionario de triage " + id);
    }
}
