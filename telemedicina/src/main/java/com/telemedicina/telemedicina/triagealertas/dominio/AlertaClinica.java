package com.telemedicina.telemedicina.triagealertas.dominio;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad interna del Agregado {@link CuestionarioTriage}: alerta clínica generada cuando el
 * triage clasifica urgencia alta (HU-03). Enfermería la recibe (HU-04), la ve en el panel de
 * alertas activas (HU-09) y la atiende.
 *
 * <p>Su constructor y sus métodos de cambio son package-private: solo se crea y se modifica a
 * través de la raíz del agregado.</p>
 */
public class AlertaClinica {

    private final String id;
    private final NivelUrgencia nivelUrgencia;
    private final LocalDateTime fechaGeneracion;
    private boolean notificada;
    private String atendidaPor;
    private LocalDateTime fechaAtencion;

    AlertaClinica(String id, NivelUrgencia nivelUrgencia, LocalDateTime fechaGeneracion) {
        this.id = id;
        this.nivelUrgencia = nivelUrgencia;
        this.fechaGeneracion = fechaGeneracion;
    }

    /** Reconstruye una alerta ya guardada. Solo lo usa el adaptador de persistencia. */
    public static AlertaClinica reconstituir(String id, NivelUrgencia nivelUrgencia, LocalDateTime fechaGeneracion,
                                             boolean notificada, String atendidaPor, LocalDateTime fechaAtencion) {
        Objects.requireNonNull(id, "El id de la alerta es obligatorio");
        var alerta = new AlertaClinica(id, nivelUrgencia, fechaGeneracion);
        alerta.notificada = notificada;
        alerta.atendidaPor = atendidaPor;
        alerta.fechaAtencion = fechaAtencion;
        return alerta;
    }

    /** HU-04 · enfermería ya recibió la notificación en tiempo real. */
    void marcarNotificada() {
        this.notificada = true;
    }

    /** HU-09 · un integrante de enfermería atiende la alerta y deja de estar activa. */
    void atender(String enfermeriaId, LocalDateTime fecha) {
        if (!estaActiva()) {
            throw new IllegalStateException("La alerta " + id + " ya fue atendida");
        }
        if (enfermeriaId == null || enfermeriaId.isBlank()) {
            throw new IllegalArgumentException("Atender una alerta requiere el id de enfermería");
        }
        this.atendidaPor = enfermeriaId.trim();
        this.fechaAtencion = fecha;
    }

    public boolean estaActiva() {
        return atendidaPor == null;
    }

    public String getId() { return id; }
    public NivelUrgencia getNivelUrgencia() { return nivelUrgencia; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public boolean isNotificada() { return notificada; }
    public String getAtendidaPor() { return atendidaPor; }
    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
}
