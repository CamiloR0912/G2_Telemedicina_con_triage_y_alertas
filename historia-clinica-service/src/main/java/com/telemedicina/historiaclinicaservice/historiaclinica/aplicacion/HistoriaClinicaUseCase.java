package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinica;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.RegistroConsulta;

import java.util.List;

/**
 * Puerto primario: lo que el subdominio Historia clínica promete a quien lo use desde afuera
 * (HTTP hoy, cualquier otro adaptador mañana).
 */
public interface HistoriaClinicaUseCase {

    /** HU-01 · el paciente se registra con su historia clínica básica. */
    HistoriaClinica registrar(String pacienteId, String grupoSanguineo, List<String> alergias, String antecedentes);

    /** HU-06 · el médico con cita consulta la historia y el acceso queda registrado. */
    HistoriaClinica consultar(String pacienteId, String medicoId);

    /**
     * HU-05 · el médico con cita registra diagnóstico, notas y receta simplificada.
     * Regla de negocio · solo si la cita ya tiene un triage completado.
     */
    RegistroConsulta registrarConsulta(String pacienteId, String medicoId, String citaId, String consultaId,
                                       String diagnostico, String notas, String recetaSimplificada);

    /** Llega desde Triage (evento Kafka triage.completados): la cita ya puede iniciar consulta. */
    void registrarTriageCompletado(String citaId);
}
