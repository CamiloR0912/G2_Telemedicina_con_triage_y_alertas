package com.telemedicina.telemedicina.historiaclinica;

import java.time.LocalDateTime;

public record RegistroConsultaResponse(String consultaId, String medicoId, String diagnostico,
                                       String notas, String recetaSimplificada, LocalDateTime fechaRegistro) {
}
