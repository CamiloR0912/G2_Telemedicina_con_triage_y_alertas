package com.telemedicina.telemedicina.triagealertas.aplicacion;

import java.util.List;

import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage;
import com.telemedicina.telemedicina.triagealertas.dominio.RespuestaTriage;

/**
 * Puerto primario de Triage y alertas: lo que el subdominio promete a quien lo llame
 * (hoy el controlador REST, mañana cualquier otro adaptador de entrada).
 */
public interface CuestionarioTriageUseCase {

    /**
     * HU-03 · el paciente responde el cuestionario; se clasifica automáticamente y, si la urgencia
     * es alta, se genera la alerta clínica y se notifica a enfermería (HU-04).
     */
    CuestionarioTriage responder(String pacienteId, String citaId, List<RespuestaTriage> respuestas);

    CuestionarioTriage buscarPorId(String id);

    /** HU-09 · panel de alertas activas, de mayor a menor urgencia y de la más antigua a la más reciente. */
    List<CuestionarioTriage> alertasActivas();

    /** HU-09 · un integrante de enfermería atiende la alerta clínica del cuestionario. */
    CuestionarioTriage atenderAlerta(String cuestionarioId, String enfermeriaId);

    /** Regla de negocio · "No puede iniciarse una consulta sin un triage completado". */
    boolean triageCompletado(String citaId);
}
