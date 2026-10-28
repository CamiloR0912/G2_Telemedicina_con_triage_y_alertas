package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.ControlAccesoHistoriaService;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinica;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaDuplicadaException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaFactory;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaNoEncontradaException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.RegistroConsulta;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.TriageNoCompletadoException;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso: implementa el puerto primario orquestando el repositorio, la factory y el
 * servicio de dominio. Las reglas de negocio siguen viviendo en el dominio.
 */
@Service
public class HistoriaClinicaService implements HistoriaClinicaUseCase {

    private final RepositorioHistoriasClinicas repositorioHistoriasClinicas;
    private final HistoriaClinicaFactory historiaClinicaFactory;
    private final ControlAccesoHistoriaService controlAccesoHistoriaService;
    private final TriagesCompletados triagesCompletados;

    public HistoriaClinicaService(RepositorioHistoriasClinicas repositorioHistoriasClinicas,
                                  HistoriaClinicaFactory historiaClinicaFactory,
                                  ControlAccesoHistoriaService controlAccesoHistoriaService,
                                  TriagesCompletados triagesCompletados) {
        this.repositorioHistoriasClinicas = repositorioHistoriasClinicas;
        this.historiaClinicaFactory = historiaClinicaFactory;
        this.controlAccesoHistoriaService = controlAccesoHistoriaService;
        this.triagesCompletados = triagesCompletados;
    }

    @Override
    public HistoriaClinica registrar(String pacienteId, String grupoSanguineo,
                                     List<String> alergias, String antecedentes) {
        HistoriaClinica historia = historiaClinicaFactory.crear(pacienteId, grupoSanguineo, alergias, antecedentes);
        if (repositorioHistoriasClinicas.existePorPacienteId(historia.getPacienteId())) {
            throw new HistoriaClinicaDuplicadaException(historia.getPacienteId());
        }
        return repositorioHistoriasClinicas.guardar(historia);
    }

    @Override
    public HistoriaClinica consultar(String pacienteId, String medicoId) {
        HistoriaClinica historia = buscar(pacienteId);
        controlAccesoHistoriaService.consultarHistoria(historia, medicoId);
        // se guarda para que el registro de acceso quede persistido (auditoría HU-06)
        return repositorioHistoriasClinicas.guardar(historia);
    }

    @Override
    public RegistroConsulta registrarConsulta(String pacienteId, String medicoId, String citaId, String consultaId,
                                              String diagnostico, String notas, String recetaSimplificada) {
        if (citaId == null || citaId.isBlank()) {
            throw new IllegalArgumentException("El registro de consulta requiere el id de la cita");
        }
        HistoriaClinica historia = buscar(pacienteId);
        if (!triagesCompletados.estaCompletado(citaId.trim())) {
            throw new TriageNoCompletadoException(citaId.trim());
        }
        RegistroConsulta registro = controlAccesoHistoriaService.registrarConsulta(
                historia, medicoId, consultaId, diagnostico, notas, recetaSimplificada);
        repositorioHistoriasClinicas.guardar(historia);
        return registro;
    }

    @Override
    public void registrarTriageCompletado(String citaId) {
        triagesCompletados.registrar(citaId);
    }

    private HistoriaClinica buscar(String pacienteId) {
        return repositorioHistoriasClinicas.buscarPorPacienteId(pacienteId)
                .orElseThrow(() -> new HistoriaClinicaNoEncontradaException(pacienteId));
    }
}
