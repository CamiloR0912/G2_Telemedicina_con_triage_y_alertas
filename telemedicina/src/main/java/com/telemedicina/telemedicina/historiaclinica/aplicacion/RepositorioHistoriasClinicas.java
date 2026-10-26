package com.telemedicina.telemedicina.historiaclinica.aplicacion;

import com.telemedicina.telemedicina.historiaclinica.dominio.HistoriaClinica;

import java.util.Optional;

/**
 * Puerto secundario mínimo de persistencia: solo lo que {@link HistoriaClinicaService} llama de verdad.
 */
public interface RepositorioHistoriasClinicas {

    Optional<HistoriaClinica> buscarPorPacienteId(String pacienteId);

    boolean existePorPacienteId(String pacienteId);

    HistoriaClinica guardar(HistoriaClinica historia);
}
