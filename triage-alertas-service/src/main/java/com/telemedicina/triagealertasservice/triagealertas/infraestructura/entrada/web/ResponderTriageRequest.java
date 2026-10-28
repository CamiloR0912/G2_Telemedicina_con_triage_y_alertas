package com.telemedicina.triagealertasservice.triagealertas.infraestructura.entrada.web;

import java.util.List;

public record ResponderTriageRequest(String pacienteId, String citaId, List<RespuestaRequest> respuestas) {

    public record RespuestaRequest(String sintoma, int intensidad) {
    }
}
