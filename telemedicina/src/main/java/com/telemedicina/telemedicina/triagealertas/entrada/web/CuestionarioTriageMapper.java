package com.telemedicina.telemedicina.triagealertas.infraestructura.entrada.web;

import java.util.List;

import com.telemedicina.telemedicina.triagealertas.dominio.AlertaClinica;
import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage;
import com.telemedicina.telemedicina.triagealertas.dominio.NivelUrgencia;
import com.telemedicina.telemedicina.triagealertas.dominio.RespuestaTriage;

/** Traduce entre lo que llega/sale por HTTP y el modelo de dominio. */
final class CuestionarioTriageMapper {

    private CuestionarioTriageMapper() {
    }

    /** Una lista nula se deja pasar: la Factory es quien decide que el cuestionario necesita respuestas. */
    static List<RespuestaTriage> aRespuestas(List<ResponderTriageRequest.RespuestaRequest> respuestas) {
        if (respuestas == null) {
            return null;
        }
        return respuestas.stream()
                .map(r -> r == null ? null : new RespuestaTriage(r.sintoma(), r.intensidad()))
                .toList();
    }

    static CuestionarioTriageResponse aResponse(CuestionarioTriage cuestionario) {
        var respuestas = cuestionario.getRespuestas().stream()
                .map(r -> new CuestionarioTriageResponse.RespuestaResponse(r.sintoma(), r.intensidad()))
                .toList();
        var alerta = cuestionario.getAlertaClinica().map(CuestionarioTriageMapper::aResponse).orElse(null);
        return new CuestionarioTriageResponse(cuestionario.getId(), cuestionario.getPacienteId(),
                cuestionario.getCitaId(), respuestas, cuestionario.getFechaRespuesta(),
                cuestionario.getNivelUrgencia().map(NivelUrgencia::valor).orElse(null),
                cuestionario.getFechaClasificacion(), cuestionario.estaCompletado(), alerta);
    }

    static AlertaActivaResponse aAlertaActiva(CuestionarioTriage cuestionario) {
        var alerta = cuestionario.getAlertaClinica().orElseThrow();
        return new AlertaActivaResponse(alerta.getId(), cuestionario.getId(), cuestionario.getPacienteId(),
                cuestionario.getCitaId(), alerta.getNivelUrgencia().valor(), alerta.getFechaGeneracion(),
                alerta.isNotificada());
    }

    private static CuestionarioTriageResponse.AlertaResponse aResponse(AlertaClinica alerta) {
        return new CuestionarioTriageResponse.AlertaResponse(alerta.getId(), alerta.getNivelUrgencia().valor(),
                alerta.getFechaGeneracion(), alerta.isNotificada(), alerta.estaActiva(), alerta.getAtendidaPor(),
                alerta.getFechaAtencion());
    }
}
