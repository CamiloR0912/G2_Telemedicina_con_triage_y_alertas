package com.telemedicina.telemedicina.triagealertas.aplicacion;

import java.util.List;
import java.util.Optional;

import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage;

/**
 * Puerto secundario mínimo: solo los métodos que {@link CuestionarioTriageService} usa de verdad,
 * no los que JpaRepository regala.
 */
public interface RepositorioCuestionariosTriage {

    CuestionarioTriage guardar(CuestionarioTriage cuestionario);

    Optional<CuestionarioTriage> buscarPorId(String id);

    List<CuestionarioTriage> buscarPorCitaId(String citaId);

    /** HU-09 · cuestionarios cuya alerta clínica sigue sin atender. */
    List<CuestionarioTriage> buscarConAlertaActiva();
}
