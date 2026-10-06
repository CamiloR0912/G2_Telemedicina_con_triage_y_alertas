package com.telemedicina.telemedicina.triagealertas.infraestructura.salida.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.telemedicina.telemedicina.triagealertas.aplicacion.RepositorioCuestionariosTriage;
import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage;

/**
 * Adaptador secundario: traduce el puerto {@link RepositorioCuestionariosTriage} (que diseñó el
 * núcleo) a llamadas de Spring Data JPA, y {@link CuestionarioTriageEntity} de ida y vuelta a
 * {@link CuestionarioTriage}.
 */
@Component
public class CuestionarioTriageRepositoryJpaAdapter implements RepositorioCuestionariosTriage {

    private final CuestionarioTriageJpaRepository cuestionarioTriageJpaRepository;

    CuestionarioTriageRepositoryJpaAdapter(CuestionarioTriageJpaRepository cuestionarioTriageJpaRepository) {
        this.cuestionarioTriageJpaRepository = cuestionarioTriageJpaRepository;
    }

    @Override
    public CuestionarioTriage guardar(CuestionarioTriage cuestionario) {
        return cuestionarioTriageJpaRepository.save(CuestionarioTriageEntity.desde(cuestionario)).aDominio();
    }

    @Override
    public Optional<CuestionarioTriage> buscarPorId(String id) {
        return cuestionarioTriageJpaRepository.findById(id).map(CuestionarioTriageEntity::aDominio);
    }

    @Override
    public List<CuestionarioTriage> buscarPorCitaId(String citaId) {
        return cuestionarioTriageJpaRepository.findByCitaId(citaId).stream()
                .map(CuestionarioTriageEntity::aDominio)
                .toList();
    }

    @Override
    public List<CuestionarioTriage> buscarConAlertaActiva() {
        return cuestionarioTriageJpaRepository.findByAlertaIdIsNotNullAndAlertaAtendidaPorIsNull().stream()
                .map(CuestionarioTriageEntity::aDominio)
                .toList();
    }
}
