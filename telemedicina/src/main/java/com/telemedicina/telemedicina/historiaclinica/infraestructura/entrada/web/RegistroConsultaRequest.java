package com.telemedicina.telemedicina.historiaclinica.infraestructura.entrada.web;

public record RegistroConsultaRequest(String consultaId, String diagnostico,
                                      String notas, String recetaSimplificada) {
}
