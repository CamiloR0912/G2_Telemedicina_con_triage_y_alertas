package com.telemedicina.telemedicina.historiaclinica.infraestructura.entrada.web;

import java.time.LocalDateTime;

public record RegistroConsultaResponse(String consultaId, String medicoId, String diagnostico,
                                       String notas, String recetaSimplificada, LocalDateTime fechaRegistro) {
}
