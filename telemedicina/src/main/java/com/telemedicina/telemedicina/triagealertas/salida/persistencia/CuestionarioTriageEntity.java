package com.telemedicina.telemedicina.triagealertas.infraestructura.salida.persistencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.telemedicina.telemedicina.triagealertas.dominio.AlertaClinica;
import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage;
import com.telemedicina.telemedicina.triagealertas.dominio.NivelUrgencia;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 * Forma del Agregado CuestionarioTriage en la base de datos. Es un detalle del adaptador: el
 * dominio no la conoce, y por eso {@link CuestionarioTriage} no lleva ninguna anotación de JPA.
 * La alerta clínica (como máximo una por cuestionario) se guarda en columnas de esta misma tabla.
 */
@Entity
@Table(name = "cuestionarios_triage")
class CuestionarioTriageEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String citaId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "respuestas_triage", joinColumns = @JoinColumn(name = "cuestionario_id"))
    @OrderColumn(name = "orden")
    private List<RespuestaTriageEmbeddable> respuestas = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fechaRespuesta;

    private String nivelUrgencia;

    private LocalDateTime fechaClasificacion;

    private String alertaId;

    private LocalDateTime alertaFechaGeneracion;

    private boolean alertaNotificada;

    private String alertaAtendidaPor;

    private LocalDateTime alertaFechaAtencion;

    protected CuestionarioTriageEntity() {
    }

    static CuestionarioTriageEntity desde(CuestionarioTriage cuestionario) {
        var entidad = new CuestionarioTriageEntity();
        entidad.id = cuestionario.getId();
        entidad.pacienteId = cuestionario.getPacienteId();
        entidad.citaId = cuestionario.getCitaId();
        entidad.respuestas = new ArrayList<>(cuestionario.getRespuestas().stream()
                .map(RespuestaTriageEmbeddable::desde)
                .toList());
        entidad.fechaRespuesta = cuestionario.getFechaRespuesta();
        entidad.nivelUrgencia = cuestionario.getNivelUrgencia().map(NivelUrgencia::valor).orElse(null);
        entidad.fechaClasificacion = cuestionario.getFechaClasificacion();
        cuestionario.getAlertaClinica().ifPresent(alerta -> {
            entidad.alertaId = alerta.getId();
            entidad.alertaFechaGeneracion = alerta.getFechaGeneracion();
            entidad.alertaNotificada = alerta.isNotificada();
            entidad.alertaAtendidaPor = alerta.getAtendidaPor();
            entidad.alertaFechaAtencion = alerta.getFechaAtencion();
        });
        return entidad;
    }

    CuestionarioTriage aDominio() {
        NivelUrgencia nivel = nivelUrgencia == null ? null : new NivelUrgencia(nivelUrgencia);
        AlertaClinica alerta = alertaId == null ? null : AlertaClinica.reconstituir(alertaId, nivel,
                alertaFechaGeneracion, alertaNotificada, alertaAtendidaPor, alertaFechaAtencion);
        return CuestionarioTriage.reconstituir(id, pacienteId, citaId,
                respuestas.stream().map(RespuestaTriageEmbeddable::aDominio).toList(),
                fechaRespuesta, nivel, fechaClasificacion, alerta);
    }
}
