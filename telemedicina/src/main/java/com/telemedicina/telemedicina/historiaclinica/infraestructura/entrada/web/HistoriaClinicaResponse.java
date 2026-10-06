package com.telemedicina.telemedicina.historiaclinica.infraestructura.entrada.web;

import java.time.LocalDateTime;
import java.util.List;

public record HistoriaClinicaResponse(String id, String pacienteId, String grupoSanguineo,
                                      List<String> alergias, String antecedentes,
                                      List<RegistroConsultaResponse> registrosConsulta,
                                      List<RegistroAccesoResponse> registrosAcceso) {

    public record RegistroAccesoResponse(String medicoId, LocalDateTime fechaHora) {
    }
}
