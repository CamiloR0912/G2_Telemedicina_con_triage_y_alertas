package com.telemedicina.telemedicina.triagealertas.infraestructura.entrada.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.telemedicina.telemedicina.triagealertas.aplicacion.CuestionarioTriageUseCase;

/**
 * Adaptador primario: traduce HTTP a llamadas del puerto {@link CuestionarioTriageUseCase}.
 * Depende del puerto, nunca de la clase concreta CuestionarioTriageService.
 */
@RestController
@RequestMapping("/api")
public class CuestionarioTriageController {

    private final CuestionarioTriageUseCase cuestionarioTriageUseCase;

    public CuestionarioTriageController(CuestionarioTriageUseCase cuestionarioTriageUseCase) {
        this.cuestionarioTriageUseCase = cuestionarioTriageUseCase;
    }

    /** HU-03 y HU-04 */
    @PostMapping("/triage")
    @ResponseStatus(HttpStatus.CREATED)
    public CuestionarioTriageResponse responder(@RequestBody ResponderTriageRequest request) {
        var cuestionario = cuestionarioTriageUseCase.responder(request.pacienteId(), request.citaId(),
                CuestionarioTriageMapper.aRespuestas(request.respuestas()));
        return CuestionarioTriageMapper.aResponse(cuestionario);
    }

    @GetMapping("/triage/{id}")
    public CuestionarioTriageResponse buscarPorId(@PathVariable String id) {
        return CuestionarioTriageMapper.aResponse(cuestionarioTriageUseCase.buscarPorId(id));
    }

    /** Regla de negocio · "No puede iniciarse una consulta sin un triage completado". */
    @GetMapping("/triage/completado")
    public TriageCompletadoResponse triageCompletado(@RequestParam String citaId) {
        return new TriageCompletadoResponse(citaId, cuestionarioTriageUseCase.triageCompletado(citaId));
    }

    /** HU-09 */
    @GetMapping("/alertas/activas")
    public List<AlertaActivaResponse> alertasActivas() {
        return cuestionarioTriageUseCase.alertasActivas().stream()
                .map(CuestionarioTriageMapper::aAlertaActiva)
                .toList();
    }

    /** HU-09 */
    @PostMapping("/triage/{id}/alerta/atender")
    public CuestionarioTriageResponse atenderAlerta(@PathVariable String id,
                                                    @RequestBody AtenderAlertaRequest request) {
        return CuestionarioTriageMapper.aResponse(
                cuestionarioTriageUseCase.atenderAlerta(id, request.enfermeriaId()));
    }
}
