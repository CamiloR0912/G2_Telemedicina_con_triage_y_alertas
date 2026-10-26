package com.telemedicina.telemedicina.triagealertas.dominio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del Agregado del subdominio Triage y alertas.
 *
 * <p>Límite del agregado: {@link RespuestaTriage} y {@link NivelUrgencia} (Value Objects) y
 * {@link AlertaClinica} (entidad interna) solo se crean y modifican a través de esta raíz.
 * Paciente y Cita pertenecen a otros subdominios y se referencian únicamente por id.</p>
 *
 * <p>Un cuestionario nuevo se construye solo mediante {@link CuestionarioTriageFactory}; uno leído
 * de la base de datos se reconstruye con {@link #reconstituir}.</p>
 */
public class CuestionarioTriage {

    private final String id;
    private final String pacienteId;
    private final String citaId;
    private final List<RespuestaTriage> respuestas;
    private final LocalDateTime fechaRespuesta;
    private NivelUrgencia nivelUrgencia;
    private LocalDateTime fechaClasificacion;
    private AlertaClinica alertaClinica;

    CuestionarioTriage(String id, String pacienteId, String citaId,
                       List<RespuestaTriage> respuestas, LocalDateTime fechaRespuesta) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.citaId = citaId;
        this.respuestas = List.copyOf(respuestas);
        this.fechaRespuesta = fechaRespuesta;
    }

    /**
     * Reconstruye un cuestionario ya guardado, con su id y su estado. No es "responder el
     * cuestionario": no genera id nuevo ni vuelve a validar, por eso lo usa solo el adaptador de
     * persistencia.
     */
    public static CuestionarioTriage reconstituir(String id, String pacienteId, String citaId,
                                                  List<RespuestaTriage> respuestas, LocalDateTime fechaRespuesta,
                                                  NivelUrgencia nivelUrgencia, LocalDateTime fechaClasificacion,
                                                  AlertaClinica alertaClinica) {
        Objects.requireNonNull(id, "El id del cuestionario es obligatorio");
        Objects.requireNonNull(respuestas, "Las respuestas del cuestionario son obligatorias");
        var cuestionario = new CuestionarioTriage(id, pacienteId, citaId, respuestas, fechaRespuesta);
        cuestionario.nivelUrgencia = nivelUrgencia;
        cuestionario.fechaClasificacion = fechaClasificacion;
        cuestionario.alertaClinica = alertaClinica;
        return cuestionario;
    }

    /** HU-03 · deja el nivel de urgencia con el que se clasificó el cuestionario. */
    void clasificar(NivelUrgencia nivel, LocalDateTime fecha) {
        if (nivel == null) {
            throw new IllegalArgumentException("El nivel de urgencia no puede ser nulo");
        }
        if (estaCompletado()) {
            throw new IllegalStateException("El cuestionario " + id + " ya fue clasificado");
        }
        this.nivelUrgencia = nivel;
        this.fechaClasificacion = fecha;
    }

    /** HU-03 · si el nivel es alto se genera una (y solo una) alerta clínica. */
    AlertaClinica generarAlerta(String alertaId, LocalDateTime fecha) {
        if (nivelUrgencia == null || !nivelUrgencia.esAlto()) {
            throw new IllegalStateException("Solo un triage con urgencia alta genera alerta clínica");
        }
        if (alertaClinica != null) {
            throw new IllegalStateException("El cuestionario " + id + " ya tiene una alerta clínica");
        }
        this.alertaClinica = new AlertaClinica(alertaId, nivelUrgencia, fecha);
        return alertaClinica;
    }

    /** HU-04 · enfermería ya fue notificada de la alerta (lo marca el caso de uso tras notificar). */
    public void marcarAlertaNotificada() {
        obtenerAlerta().marcarNotificada();
    }

    /** HU-09 · enfermería atiende la alerta clínica de este triage. */
    public void atenderAlerta(String enfermeriaId, LocalDateTime fecha) {
        obtenerAlerta().atender(enfermeriaId, fecha);
    }

    /** Regla de negocio · "No puede iniciarse una consulta sin un triage completado". */
    public boolean estaCompletado() {
        return nivelUrgencia != null;
    }

    public boolean tieneAlertaActiva() {
        return alertaClinica != null && alertaClinica.estaActiva();
    }

    private AlertaClinica obtenerAlerta() {
        if (alertaClinica == null) {
            throw new IllegalStateException("El cuestionario " + id + " no tiene alerta clínica");
        }
        return alertaClinica;
    }

    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public String getCitaId() { return citaId; }
    public List<RespuestaTriage> getRespuestas() { return respuestas; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public Optional<NivelUrgencia> getNivelUrgencia() { return Optional.ofNullable(nivelUrgencia); }
    public LocalDateTime getFechaClasificacion() { return fechaClasificacion; }
    public Optional<AlertaClinica> getAlertaClinica() { return Optional.ofNullable(alertaClinica); }
}
