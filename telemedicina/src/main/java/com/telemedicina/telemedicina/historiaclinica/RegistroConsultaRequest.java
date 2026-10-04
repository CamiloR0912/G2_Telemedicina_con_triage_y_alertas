package com.telemedicina.telemedicina.historiaclinica;

public record RegistroConsultaRequest(String consultaId, String diagnostico,
                                      String notas, String recetaSimplificada) {
}
