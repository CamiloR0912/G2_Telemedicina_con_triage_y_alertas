package com.telemedicina.telemedicina.historiaclinica;

/** Traduce del modelo de dominio a los DTOs del adaptador HTTP. */
final class HistoriaClinicaMapper {

    private HistoriaClinicaMapper() {
    }

    static HistoriaClinicaResponse aResponse(HistoriaClinica historia) {
        return new HistoriaClinicaResponse(
                historia.getId(),
                historia.getPacienteId(),
                historia.getGrupoSanguineo(),
                historia.getAlergias(),
                historia.getAntecedentes(),
                historia.getRegistrosConsulta().stream().map(HistoriaClinicaMapper::aResponse).toList(),
                historia.getRegistrosAcceso().stream()
                        .map(a -> new HistoriaClinicaResponse.RegistroAccesoResponse(a.medicoId(), a.fechaHora()))
                        .toList());
    }

    static RegistroConsultaResponse aResponse(RegistroConsulta registro) {
        return new RegistroConsultaResponse(
                registro.getConsultaId(),
                registro.getMedicoId(),
                registro.getDiagnostico(),
                registro.getNotas(),
                registro.getRecetaSimplificada(),
                registro.getFechaRegistro());
    }
}
