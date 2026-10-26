package com.telemedicina.telemedicina.triagealertas.infraestructura.salida.persistencia;

import com.telemedicina.telemedicina.triagealertas.dominio.RespuestaTriage;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Forma de una {@link RespuestaTriage} en la base de datos (tabla respuestas_triage). */
@Embeddable
class RespuestaTriageEmbeddable {

    @Column(nullable = false)
    private String sintoma;

    @Column(nullable = false)
    private int intensidad;

    protected RespuestaTriageEmbeddable() {
    }

    static RespuestaTriageEmbeddable desde(RespuestaTriage respuesta) {
        var embebida = new RespuestaTriageEmbeddable();
        embebida.sintoma = respuesta.sintoma();
        embebida.intensidad = respuesta.intensidad();
        return embebida;
    }

    RespuestaTriage aDominio() {
        return new RespuestaTriage(sintoma, intensidad);
    }
}
