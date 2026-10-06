package com.telemedicina.telemedicina.historiaclinica.infraestructura.salida.persistencia;

import com.telemedicina.telemedicina.historiaclinica.aplicacion.RepositorioHistoriasClinicas;
import com.telemedicina.telemedicina.historiaclinica.dominio.HistoriaClinica;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador secundario explícito: implementa el puerto {@link RepositorioHistoriasClinicas}
 * traduciendo entre el dominio y Spring Data JPA.
 */
@Component
public class HistoriaClinicaRepositoryJpaAdapter implements RepositorioHistoriasClinicas {

    private final HistoriaClinicaRepository historiaClinicaRepository;

    public HistoriaClinicaRepositoryJpaAdapter(HistoriaClinicaRepository historiaClinicaRepository) {
        this.historiaClinicaRepository = historiaClinicaRepository;
    }

    @Override
    public Optional<HistoriaClinica> buscarPorPacienteId(String pacienteId) {
        return historiaClinicaRepository.findByPacienteId(pacienteId).map(HistoriaClinicaJpaEntity::aDominio);
    }

    @Override
    public boolean existePorPacienteId(String pacienteId) {
        return historiaClinicaRepository.existsByPacienteId(pacienteId);
    }

    @Override
    public HistoriaClinica guardar(HistoriaClinica historia) {
        return historiaClinicaRepository.save(HistoriaClinicaJpaEntity.desdeDominio(historia)).aDominio();
    }
}
