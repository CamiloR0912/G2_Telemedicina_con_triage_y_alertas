package com.telemedicina.telemedicina.historiaclinica;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Raíz del Agregado del subdominio Historia clínica.
 *
 * <p>Límite del agregado: {@link RegistroConsulta} (entidad interna) y {@link RegistroAcceso}
 * (Value Object) solo se crean y modifican a través de esta raíz. Paciente, Médico, Cita y
 * Consulta pertenecen a otros subdominios y se referencian únicamente por id.</p>
 *
 * <p>Se construye solo mediante {@link HistoriaClinicaFactory}.</p>
 */
public class HistoriaClinica {

    private final String id;
    private final String pacienteId;
    private final String grupoSanguineo;
    private final List<String> alergias;
    private final String antecedentes;
    private final List<RegistroConsulta> registrosConsulta = new ArrayList<>();
    private final List<RegistroAcceso> registrosAcceso = new ArrayList<>();

    HistoriaClinica(String id, String pacienteId, String grupoSanguineo,
                    List<String> alergias, String antecedentes) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.grupoSanguineo = grupoSanguineo;
        this.alergias = List.copyOf(alergias);
        this.antecedentes = antecedentes;
    }

    /** HU-06 · deja constancia de que un médico consultó esta historia. */
    void registrarAcceso(RegistroAcceso acceso) {
        if (acceso == null) {
            throw new IllegalArgumentException("El registro de acceso no puede ser nulo");
        }
        registrosAcceso.add(acceso);
    }

    /** HU-05 · agrega diagnóstico, notas y receta simplificada de una consulta. */
    RegistroConsulta registrarConsulta(String consultaId, String medicoId, String diagnostico,
                                       String notas, String recetaSimplificada, LocalDateTime fecha) {
        boolean yaRegistrada = registrosConsulta.stream()
                .anyMatch(r -> r.getConsultaId().equals(consultaId));
        if (yaRegistrada) {
            throw new IllegalStateException("La consulta " + consultaId + " ya tiene registro clínico");
        }
        RegistroConsulta registro = new RegistroConsulta(
                consultaId, medicoId, diagnostico, notas, recetaSimplificada, fecha);
        registrosConsulta.add(registro);
        return registro;
    }

    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public String getGrupoSanguineo() { return grupoSanguineo; }
    public List<String> getAlergias() { return alergias; }
    public String getAntecedentes() { return antecedentes; }

    public List<RegistroConsulta> getRegistrosConsulta() {
        return Collections.unmodifiableList(registrosConsulta);
    }

    public List<RegistroAcceso> getRegistrosAcceso() {
        return Collections.unmodifiableList(registrosAcceso);
    }
}
