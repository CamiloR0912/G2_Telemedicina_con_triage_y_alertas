package com.telemedicina.telemedicina.historiaclinica.dominio;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Servicio de Dominio: la regla "un médico solo puede ver la historia clínica de pacientes con
 * cita activa o histórica con él" involucra Médico, Cita e HistoriaClinica, así que no pertenece
 * naturalmente a ninguna de esas entidades.
 */
public class ControlAccesoHistoriaService {

    private final HistorialCitas historialCitas;
    private final Clock reloj;

    public ControlAccesoHistoriaService(HistorialCitas historialCitas, Clock reloj) {
        this.historialCitas = historialCitas;
        this.reloj = reloj;
    }

    /** HU-06 · verifica la cita y deja el acceso auditado antes de entregar la historia. */
    public HistoriaClinica consultarHistoria(HistoriaClinica historia, String medicoId) {
        verificarCita(historia, medicoId);
        historia.registrarAcceso(new RegistroAcceso(medicoId, LocalDateTime.now(reloj)));
        return historia;
    }

    /** HU-05 · solo el médico con cita con el paciente puede dejar constancia clínica. */
    public RegistroConsulta registrarConsulta(HistoriaClinica historia, String medicoId, String consultaId,
                                              String diagnostico, String notas, String recetaSimplificada) {
        verificarCita(historia, medicoId);
        return historia.registrarConsulta(
                consultaId, medicoId, diagnostico, notas, recetaSimplificada, LocalDateTime.now(reloj));
    }

    private void verificarCita(HistoriaClinica historia, String medicoId) {
        if (!historialCitas.existeCitaActivaOHistorica(medicoId, historia.getPacienteId())) {
            throw new AccesoHistoriaDenegadoException(medicoId, historia.getPacienteId());
        }
    }
}
