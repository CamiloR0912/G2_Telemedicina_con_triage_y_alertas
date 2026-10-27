package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import com.telemedicina.triagealertasservice.triagealertas.dominio.AlertaClinica;
import com.telemedicina.triagealertasservice.triagealertas.dominio.ClasificacionTriageService;
import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriage;
import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriageFactory;
import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriageNoEncontradoException;
import com.telemedicina.triagealertasservice.triagealertas.dominio.RespuestaTriage;

import jakarta.transaction.Transactional;

/**
 * Caso de uso: orquesta el dominio (Factory, Servicio de Dominio, CuestionarioTriage) y los dos
 * puertos secundarios. Las reglas viven en el dominio; aquí solo se coordinan.
 *
 * <p>No lleva {@code @Service}: se registra en {@code TriageAlertasConfig}, así este paquete no
 * importa nada de Spring y el test lo crea con {@code new} y un reloj fijo.</p>
 *
 * <p>{@code jakarta.transaction.Transactional} (estándar de Jakarta EE, no de Spring) lo respeta
 * Spring igual: el cuestionario y su evento en la tabla outbox se guardan en la misma transacción.</p>
 */
public class CuestionarioTriageService implements CuestionarioTriageUseCase {

    /** HU-09 · primero la urgencia más alta; a igual urgencia, la alerta que lleva más tiempo esperando. */
    private static final Comparator<CuestionarioTriage> POR_ORDEN_DE_URGENCIA =
            Comparator.<CuestionarioTriage>comparingInt(c -> c.getAlertaClinica().orElseThrow()
                            .getNivelUrgencia().prioridad()).reversed()
                    .thenComparing(c -> c.getAlertaClinica().orElseThrow().getFechaGeneracion());

    private final RepositorioCuestionariosTriage repositorioCuestionarios;
    private final NotificadorEnfermeria notificadorEnfermeria;
    private final PublicadorEventosTriage publicadorEventos;
    private final CuestionarioTriageFactory cuestionarioTriageFactory;
    private final ClasificacionTriageService clasificacionTriageService;
    private final Clock reloj;

    public CuestionarioTriageService(RepositorioCuestionariosTriage repositorioCuestionarios,
                                     NotificadorEnfermeria notificadorEnfermeria,
                                     PublicadorEventosTriage publicadorEventos,
                                     CuestionarioTriageFactory cuestionarioTriageFactory,
                                     ClasificacionTriageService clasificacionTriageService,
                                     Clock reloj) {
        this.repositorioCuestionarios = repositorioCuestionarios;
        this.notificadorEnfermeria = notificadorEnfermeria;
        this.publicadorEventos = publicadorEventos;
        this.cuestionarioTriageFactory = cuestionarioTriageFactory;
        this.clasificacionTriageService = clasificacionTriageService;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public CuestionarioTriage responder(String pacienteId, String citaId, List<RespuestaTriage> respuestas) {
        var cuestionario = cuestionarioTriageFactory.crear(pacienteId, citaId, respuestas);
        clasificacionTriageService.clasificar(cuestionario);
        // Se guarda antes de notificar: si el canal falla, la alerta ya quedó registrada.
        var guardado = repositorioCuestionarios.guardar(cuestionario);
        // Transactional Outbox: el evento se confirma junto con el cuestionario, o ninguno de los dos.
        publicadorEventos.triageCompletado(guardado);
        if (guardado.getAlertaClinica().isPresent() && notificarEnfermeria(guardado)) {
            guardado = repositorioCuestionarios.guardar(guardado);
        }
        return guardado;
    }

    @Override
    public CuestionarioTriage buscarPorId(String id) {
        return repositorioCuestionarios.buscarPorId(id)
                .orElseThrow(() -> new CuestionarioTriageNoEncontradoException(id));
    }

    @Override
    public List<CuestionarioTriage> alertasActivas() {
        return repositorioCuestionarios.buscarConAlertaActiva().stream()
                .filter(CuestionarioTriage::tieneAlertaActiva)
                .sorted(POR_ORDEN_DE_URGENCIA)
                .toList();
    }

    @Override
    public CuestionarioTriage atenderAlerta(String cuestionarioId, String enfermeriaId) {
        var cuestionario = buscarPorId(cuestionarioId);
        cuestionario.atenderAlerta(enfermeriaId, LocalDateTime.now(reloj));
        return repositorioCuestionarios.guardar(cuestionario);
    }

    @Override
    public boolean triageCompletado(String citaId) {
        return repositorioCuestionarios.buscarPorCitaId(citaId).stream()
                .anyMatch(CuestionarioTriage::estaCompletado);
    }

    /**
     * HU-04 · si el canal de notificación falla, la alerta no se pierde: queda activa y sin
     * notificar, visible en el panel de alertas activas (HU-09).
     */
    private boolean notificarEnfermeria(CuestionarioTriage cuestionario) {
        AlertaClinica alerta = cuestionario.getAlertaClinica().orElseThrow();
        try {
            notificadorEnfermeria.notificar(cuestionario.getId(), cuestionario.getPacienteId(),
                    cuestionario.getCitaId(), alerta);
        } catch (RuntimeException e) {
            return false;
        }
        cuestionario.marcarAlertaNotificada();
        return true;
    }
}
