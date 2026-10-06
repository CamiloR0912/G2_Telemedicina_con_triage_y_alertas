package com.telemedicina.telemedicina.historiaclinica.api;

import com.telemedicina.telemedicina.historiaclinica.ControlAccesoHistoriaService;
import com.telemedicina.telemedicina.historiaclinica.HistoriaClinica;
import com.telemedicina.telemedicina.historiaclinica.HistoriaClinicaFactory;
import com.telemedicina.telemedicina.historiaclinica.HistorialCitas;
import com.telemedicina.telemedicina.historiaclinica.RegistroConsulta;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * Servicio de aplicación de Historia clínica: orquesta el repositorio, la factory y el servicio
 * de dominio. Las reglas (acceso solo con cita, auditoría, consulta única) viven en el dominio.
 */
@Service
public class GestionHistoriaClinicaService {

    private final RepositorioHistoriasClinicasEnMemoria repositorio;
    private final HistoriaClinicaFactory historiaClinicaFactory = new HistoriaClinicaFactory();
    private final ControlAccesoHistoriaService controlAccesoHistoriaService;

    public GestionHistoriaClinicaService(RepositorioHistoriasClinicasEnMemoria repositorio,
                                         HistorialCitas historialCitas) {
        this.repositorio = repositorio;
        this.controlAccesoHistoriaService =
                new ControlAccesoHistoriaService(historialCitas, Clock.systemDefaultZone());
    }

    /** HU-01 */
    public synchronized HistoriaClinica registrar(String pacienteId, String grupoSanguineo,
                                                  List<String> alergias, String antecedentes) {
        HistoriaClinica historia = historiaClinicaFactory.crear(pacienteId, grupoSanguineo, alergias, antecedentes);
        if (repositorio.existePorPacienteId(historia.getPacienteId())) {
            throw new HistoriaClinicaDuplicadaException(historia.getPacienteId());
        }
        return repositorio.guardar(historia);
    }

    /** HU-06 */
    public synchronized HistoriaClinica consultar(String pacienteId, String medicoId) {
        HistoriaClinica historia = buscar(pacienteId);
        controlAccesoHistoriaService.consultarHistoria(historia, medicoId);
        return repositorio.guardar(historia);
    }

    /** HU-05 */
    public synchronized RegistroConsulta registrarConsulta(String pacienteId, String medicoId, String consultaId,
                                                           String diagnostico, String notas,
                                                           String recetaSimplificada) {
        HistoriaClinica historia = buscar(pacienteId);
        RegistroConsulta registro = controlAccesoHistoriaService.registrarConsulta(
                historia, medicoId, consultaId, diagnostico, notas, recetaSimplificada);
        repositorio.guardar(historia);
        return registro;
    }

    private HistoriaClinica buscar(String pacienteId) {
        return repositorio.buscarPorPacienteId(pacienteId)
                .orElseThrow(() -> new HistoriaClinicaNoEncontradaException(pacienteId));
    }
}
