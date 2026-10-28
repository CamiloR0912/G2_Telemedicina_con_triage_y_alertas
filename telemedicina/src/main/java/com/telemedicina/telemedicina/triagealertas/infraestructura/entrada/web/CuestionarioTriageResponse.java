package com.telemedicina.telemedicina.triagealertas.infraestructura.entrada.web;

import java.time.LocalDateTime;
import java.util.List;

public record CuestionarioTriageResponse(String id, String pacienteId, String citaId,
                                         List<RespuestaResponse> respuestas, LocalDateTime fechaRespuesta,
                                         String nivelUrgencia, LocalDateTime fechaClasificacion,
                                         boolean completado, AlertaResponse alerta) {

    public record RespuestaResponse(String sintoma, int intensidad) {
    }

    public record AlertaResponse(String id, String nivelUrgencia, LocalDateTime fechaGeneracion,
                                 boolean notificada, boolean activa, String atendidaPor,
                                 LocalDateTime fechaAtencion) {
    }
}
