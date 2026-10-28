package com.telemedicina.telemedicina.triagealertas.infraestructura.entrada.web;

import java.time.LocalDateTime;

/** Una fila del panel de alertas activas de enfermería (HU-09). */
public record AlertaActivaResponse(String alertaId, String cuestionarioId, String pacienteId, String citaId,
                                   String nivelUrgencia, LocalDateTime fechaGeneracion, boolean notificada) {
}
