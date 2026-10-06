package com.telemedicina.telemedicina.historiaclinica.api;

import com.telemedicina.telemedicina.historiaclinica.HistoriaClinica;
import com.telemedicina.telemedicina.historiaclinica.RegistroAcceso;
import com.telemedicina.telemedicina.historiaclinica.RegistroConsulta;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** El médico que actúa llega en el encabezado {@code X-Medico-Id}. */
@RestController
@RequestMapping("/api/historias-clinicas")
public class HistoriaClinicaController {

    private final GestionHistoriaClinicaService gestionHistoriaClinicaService;

    public HistoriaClinicaController(GestionHistoriaClinicaService gestionHistoriaClinicaService) {
        this.gestionHistoriaClinicaService = gestionHistoriaClinicaService;
    }

    /** HU-01 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoriaClinicaResponse registrar(@RequestBody HistoriaClinicaRequest request) {
        return HistoriaClinicaResponse.de(gestionHistoriaClinicaService.registrar(
                request.pacienteId(), request.grupoSanguineo(), request.alergias(), request.antecedentes()));
    }

    /** HU-06 */
    @GetMapping("/{pacienteId}")
    public HistoriaClinicaResponse consultar(@PathVariable String pacienteId,
                                             @RequestHeader("X-Medico-Id") String medicoId) {
        return HistoriaClinicaResponse.de(gestionHistoriaClinicaService.consultar(pacienteId, medicoId));
    }

    /** HU-05 */
    @PostMapping("/{pacienteId}/consultas")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroConsultaResponse registrarConsulta(@PathVariable String pacienteId,
                                                      @RequestHeader("X-Medico-Id") String medicoId,
                                                      @RequestBody RegistroConsultaRequest request) {
        return RegistroConsultaResponse.de(gestionHistoriaClinicaService.registrarConsulta(
                pacienteId, medicoId, request.consultaId(), request.diagnostico(),
                request.notas(), request.recetaSimplificada()));
    }

    public record HistoriaClinicaRequest(String pacienteId, String grupoSanguineo,
                                         List<String> alergias, String antecedentes) {
    }

    public record RegistroConsultaRequest(String consultaId, String diagnostico,
                                          String notas, String recetaSimplificada) {
    }

    public record HistoriaClinicaResponse(String id, String pacienteId, String grupoSanguineo,
                                          List<String> alergias, String antecedentes,
                                          List<RegistroConsultaResponse> registrosConsulta,
                                          List<RegistroAcceso> registrosAcceso) {

        static HistoriaClinicaResponse de(HistoriaClinica historia) {
            return new HistoriaClinicaResponse(historia.getId(), historia.getPacienteId(),
                    historia.getGrupoSanguineo(), historia.getAlergias(), historia.getAntecedentes(),
                    historia.getRegistrosConsulta().stream().map(RegistroConsultaResponse::de).toList(),
                    List.copyOf(historia.getRegistrosAcceso()));
        }
    }

    public record RegistroConsultaResponse(String consultaId, String medicoId, String diagnostico,
                                           String notas, String recetaSimplificada,
                                           LocalDateTime fechaRegistro) {

        static RegistroConsultaResponse de(RegistroConsulta registro) {
            return new RegistroConsultaResponse(registro.getConsultaId(), registro.getMedicoId(),
                    registro.getDiagnostico(), registro.getNotas(), registro.getRecetaSimplificada(),
                    registro.getFechaRegistro());
        }
    }
}
