package com.telemedicina.telemedicina.historiaclinica;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador primario HTTP: traduce peticiones REST a llamadas al puerto {@link HistoriaClinicaUseCase}.
 * El médico que actúa llega en el encabezado {@code X-Medico-Id}.
 */
@RestController
@RequestMapping("/api/historias-clinicas")
public class HistoriaClinicaController {

    private final HistoriaClinicaUseCase historiaClinicaUseCase;

    public HistoriaClinicaController(HistoriaClinicaUseCase historiaClinicaUseCase) {
        this.historiaClinicaUseCase = historiaClinicaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoriaClinicaResponse registrar(@RequestBody HistoriaClinicaRequest request) {
        return HistoriaClinicaMapper.aResponse(historiaClinicaUseCase.registrar(
                request.pacienteId(), request.grupoSanguineo(), request.alergias(), request.antecedentes()));
    }

    @GetMapping("/{pacienteId}")
    public HistoriaClinicaResponse consultar(@PathVariable String pacienteId,
                                             @RequestHeader("X-Medico-Id") String medicoId) {
        return HistoriaClinicaMapper.aResponse(historiaClinicaUseCase.consultar(pacienteId, medicoId));
    }

    @PostMapping("/{pacienteId}/consultas")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroConsultaResponse registrarConsulta(@PathVariable String pacienteId,
                                                      @RequestHeader("X-Medico-Id") String medicoId,
                                                      @RequestBody RegistroConsultaRequest request) {
        return HistoriaClinicaMapper.aResponse(historiaClinicaUseCase.registrarConsulta(
                pacienteId, medicoId, request.consultaId(), request.diagnostico(),
                request.notas(), request.recetaSimplificada()));
    }
}
