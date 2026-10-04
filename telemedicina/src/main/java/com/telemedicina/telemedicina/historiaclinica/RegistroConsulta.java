package com.telemedicina.telemedicina.historiaclinica;

import java.time.LocalDateTime;

/**
 * Entidad interna del agregado HistoriaClinica: lo que el médico deja como constancia
 * clínica de una consulta (HU-05 · diagnóstico, notas y receta simplificada).
 * Solo se crea a través de {@link HistoriaClinica#registrarConsulta}.
 */
public class RegistroConsulta {

    private final String consultaId;
    private final String medicoId;
    private final String diagnostico;
    private final String notas;
    private final String recetaSimplificada;
    private final LocalDateTime fechaRegistro;

    RegistroConsulta(String consultaId, String medicoId, String diagnostico,
                     String notas, String recetaSimplificada, LocalDateTime fechaRegistro) {
        if (consultaId == null || consultaId.isBlank()) {
            throw new IllegalArgumentException("El registro de consulta requiere el id de la consulta");
        }
        if (medicoId == null || medicoId.isBlank()) {
            throw new IllegalArgumentException("El registro de consulta requiere el id del médico");
        }
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new IllegalArgumentException("El diagnóstico es obligatorio");
        }
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("El registro de consulta requiere fecha");
        }
        this.consultaId = consultaId;
        this.medicoId = medicoId;
        this.diagnostico = diagnostico.trim();
        this.notas = notas == null ? "" : notas.trim();
        this.recetaSimplificada = recetaSimplificada == null ? "" : recetaSimplificada.trim();
        this.fechaRegistro = fechaRegistro;
    }

    /** Reconstruye un registro leído de persistencia; solo entra al agregado vía {@link HistoriaClinica#reconstituir}. */
    public static RegistroConsulta reconstituir(String consultaId, String medicoId, String diagnostico,
                                                String notas, String recetaSimplificada, LocalDateTime fechaRegistro) {
        return new RegistroConsulta(consultaId, medicoId, diagnostico, notas, recetaSimplificada, fechaRegistro);
    }

    public String getConsultaId() { return consultaId; }
    public String getMedicoId() { return medicoId; }
    public String getDiagnostico() { return diagnostico; }
    public String getNotas() { return notas; }
    public String getRecetaSimplificada() { return recetaSimplificada; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
