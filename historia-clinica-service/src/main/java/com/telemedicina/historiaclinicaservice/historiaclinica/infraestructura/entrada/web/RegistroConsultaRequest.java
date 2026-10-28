package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.entrada.web;

public record RegistroConsultaRequest(String citaId, String consultaId, String diagnostico,
                                      String notas, String recetaSimplificada) {
}
