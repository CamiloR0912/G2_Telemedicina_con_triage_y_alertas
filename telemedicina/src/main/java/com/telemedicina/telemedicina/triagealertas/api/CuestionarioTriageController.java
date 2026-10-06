package com.telemedicina.telemedicina.triagealertas.api;

import com.telemedicina.telemedicina.triagealertas.AlertaClinica;
import com.telemedicina.telemedicina.triagealertas.CuestionarioTriage;
import com.telemedicina.telemedicina.triagealertas.NivelUrgencia;
import com.telemedicina.telemedicina.triagealertas.RespuestaTriage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CuestionarioTriageController {

    private final GestionTriageService gestionTriageService;

    public CuestionarioTriageController(GestionTriageService gestionTriageService) {
        this.gestionTriageService = gestionTriageService;
    }

    /** HU-03 y HU-04 */
    @PostMapping("/triage")
    @ResponseStatus(HttpStatus.CREATED)
    public CuestionarioTriageResponse responder(@RequestBody ResponderTriageRequest request) {
        // Una lista nula se deja pasar: la Factory es quien decide que el cuestionario necesita respuestas.
        List<RespuestaTriage> respuestas = request.respuestas() == null ? null : request.respuestas().stream()
                .map(r -> r == null ? null : new RespuestaTriage(r.sintoma(), r.intensidad()))
                .toList();
        return CuestionarioTriageResponse.de(
                gestionTriageService.responder(request.pacienteId(), request.citaId(), respuestas));
    }

    @GetMapping("/triage/{id}")
    public CuestionarioTriageResponse buscarPorId(@PathVariable String id) {
        return CuestionarioTriageResponse.de(gestionTriageService.buscarPorId(id));
    }

    /** Regla de negocio · "No puede iniciarse una consulta sin un triage completado". */
    @GetMapping("/triage/completado")
    public TriageCompletadoResponse triageCompletado(@RequestParam String citaId) {
        return new TriageCompletadoResponse(citaId, gestionTriageService.triageCompletado(citaId));
    }

    /** HU-09 */
    @GetMapping("/alertas/activas")
    public List<AlertaActivaResponse> alertasActivas() {
        return gestionTriageService.alertasActivas().stream().map(AlertaActivaResponse::de).toList();
    }

    /** HU-09 */
    @PostMapping("/triage/{id}/alerta/atender")
    public CuestionarioTriageResponse atenderAlerta(@PathVariable String id,
                                                    @RequestBody AtenderAlertaRequest request) {
        return CuestionarioTriageResponse.de(gestionTriageService.atenderAlerta(id, request.enfermeriaId()));
    }

    public record ResponderTriageRequest(String pacienteId, String citaId, List<RespuestaRequest> respuestas) {
    }

    public record RespuestaRequest(String sintoma, int intensidad) {
    }

    public record AtenderAlertaRequest(String enfermeriaId) {
    }

    public record TriageCompletadoResponse(String citaId, boolean completado) {
    }

    public record CuestionarioTriageResponse(String id, String pacienteId, String citaId,
                                             List<RespuestaRequest> respuestas, LocalDateTime fechaRespuesta,
                                             String nivelUrgencia, LocalDateTime fechaClasificacion,
                                             boolean completado, AlertaResponse alerta) {

        static CuestionarioTriageResponse de(CuestionarioTriage cuestionario) {
            return new CuestionarioTriageResponse(cuestionario.getId(), cuestionario.getPacienteId(),
                    cuestionario.getCitaId(),
                    cuestionario.getRespuestas().stream()
                            .map(r -> new RespuestaRequest(r.sintoma(), r.intensidad())).toList(),
                    cuestionario.getFechaRespuesta(),
                    cuestionario.getNivelUrgencia().map(NivelUrgencia::valor).orElse(null),
                    cuestionario.getFechaClasificacion(), cuestionario.estaCompletado(),
                    cuestionario.getAlertaClinica().map(AlertaResponse::de).orElse(null));
        }
    }

    public record AlertaResponse(String id, String nivelUrgencia, LocalDateTime fechaGeneracion,
                                 boolean notificada, boolean activa, String atendidaPor,
                                 LocalDateTime fechaAtencion) {

        static AlertaResponse de(AlertaClinica alerta) {
            return new AlertaResponse(alerta.getId(), alerta.getNivelUrgencia().valor(), alerta.getFechaGeneracion(),
                    alerta.isNotificada(), alerta.estaActiva(), alerta.getAtendidaPor(), alerta.getFechaAtencion());
        }
    }

    /** Una fila del panel de alertas activas de enfermería (HU-09). */
    public record AlertaActivaResponse(String alertaId, String cuestionarioId, String pacienteId, String citaId,
                                       String nivelUrgencia, LocalDateTime fechaGeneracion, boolean notificada) {

        static AlertaActivaResponse de(CuestionarioTriage cuestionario) {
            AlertaClinica alerta = cuestionario.getAlertaClinica().orElseThrow();
            return new AlertaActivaResponse(alerta.getId(), cuestionario.getId(), cuestionario.getPacienteId(),
                    cuestionario.getCitaId(), alerta.getNivelUrgencia().valor(), alerta.getFechaGeneracion(),
                    alerta.isNotificada());
        }
    }
}
