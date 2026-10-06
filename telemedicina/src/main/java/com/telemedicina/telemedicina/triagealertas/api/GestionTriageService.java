package com.telemedicina.telemedicina.triagealertas.api;

import com.telemedicina.telemedicina.triagealertas.AlertaClinica;
import com.telemedicina.telemedicina.triagealertas.ClasificacionTriageService;
import com.telemedicina.telemedicina.triagealertas.CuestionarioTriage;
import com.telemedicina.telemedicina.triagealertas.CuestionarioTriageFactory;
import com.telemedicina.telemedicina.triagealertas.NotificadorEnfermeria;
import com.telemedicina.telemedicina.triagealertas.RespuestaTriage;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de aplicación de Triage y alertas: orquesta el repositorio, la factory y el servicio
 * de dominio. La clasificación, la alerta y la notificación las decide el dominio.
 */
@Service
public class GestionTriageService {

    private final RepositorioCuestionariosTriageEnMemoria repositorio;
    private final Clock reloj = Clock.systemDefaultZone();
    private final CuestionarioTriageFactory cuestionarioTriageFactory = new CuestionarioTriageFactory(reloj);
    private final ClasificacionTriageService clasificacionTriageService;

    public GestionTriageService(RepositorioCuestionariosTriageEnMemoria repositorio,
                                NotificadorEnfermeria notificadorEnfermeria) {
        this.repositorio = repositorio;
        this.clasificacionTriageService = new ClasificacionTriageService(notificadorEnfermeria, reloj);
    }

    /** HU-03 y HU-04 */
    public CuestionarioTriage responder(String pacienteId, String citaId, List<RespuestaTriage> respuestas) {
        CuestionarioTriage cuestionario = cuestionarioTriageFactory.crear(pacienteId, citaId, respuestas);
        clasificacionTriageService.clasificar(cuestionario);
        return repositorio.guardar(cuestionario);
    }

    public CuestionarioTriage buscarPorId(String id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new CuestionarioTriageNoEncontradoException(id));
    }

    /** Regla de negocio · "No puede iniciarse una consulta sin un triage completado". */
    public boolean triageCompletado(String citaId) {
        return repositorio.todos().stream()
                .anyMatch(c -> c.getCitaId().equals(citaId) && c.estaCompletado());
    }

    /** HU-09 · alertas sin atender, de la más antigua a la más reciente. */
    public List<CuestionarioTriage> alertasActivas() {
        return repositorio.todos().stream()
                .filter(CuestionarioTriage::tieneAlertaActiva)
                .sorted(Comparator.comparing(c -> c.getAlertaClinica()
                        .map(AlertaClinica::getFechaGeneracion).orElseThrow()))
                .toList();
    }

    /** HU-09 */
    public synchronized CuestionarioTriage atenderAlerta(String id, String enfermeriaId) {
        CuestionarioTriage cuestionario = buscarPorId(id);
        cuestionario.atenderAlerta(enfermeriaId, LocalDateTime.now(reloj));
        return repositorio.guardar(cuestionario);
    }
}
