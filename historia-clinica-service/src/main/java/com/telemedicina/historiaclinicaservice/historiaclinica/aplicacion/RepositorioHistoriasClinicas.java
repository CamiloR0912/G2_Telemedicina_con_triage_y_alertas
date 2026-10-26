package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinica;

import java.util.Optional;

/**
 * Puerto secundario mínimo de persistencia: solo lo que {@link HistoriaClinicaService} llama de verdad.
 */
public interface RepositorioHistoriasClinicas {

    Optional<HistoriaClinica> buscarPorPacienteId(String pacienteId);

    boolean existePorPacienteId(String pacienteId);

    HistoriaClinica guardar(HistoriaClinica historia);
}
