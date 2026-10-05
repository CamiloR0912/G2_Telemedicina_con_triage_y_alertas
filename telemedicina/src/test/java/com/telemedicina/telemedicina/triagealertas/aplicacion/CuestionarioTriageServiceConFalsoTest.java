package com.telemedicina.telemedicina.triagealertas.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.telemedicina.telemedicina.triagealertas.dominio.ClasificacionTriageService;
import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriageFactory;
import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriageNoEncontradoException;
import com.telemedicina.telemedicina.triagealertas.dominio.NivelUrgencia;
import com.telemedicina.telemedicina.triagealertas.dominio.RespuestaTriage;

/**
 * Sin @SpringBootTest, sin @Mock, sin base de datos: el núcleo solo necesita algo que cumpla
 * RepositorioCuestionariosTriage y NotificadorEnfermeria.
 */
class CuestionarioTriageServiceConFalsoTest {

    private static final Clock RELOJ =
            Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZoneId.of("America/Bogota"));

    private final RepositorioCuestionariosTriageFalso repositorio = new RepositorioCuestionariosTriageFalso();
    private final NotificadorEnfermeriaFalso enfermeria = new NotificadorEnfermeriaFalso();
    private final CuestionarioTriageService service = new CuestionarioTriageService(repositorio, enfermeria,
            new CuestionarioTriageFactory(RELOJ), new ClasificacionTriageService(RELOJ), RELOJ);

    @Test
    void respondeYRecuperaUnCuestionarioSinNingunaDependenciaDeSpringNiDeBaseDeDatos() {
        var respondido = service.responder("pac-1", "cita-1", List.of(new RespuestaTriage("fiebre", 3)));

        var recuperado = service.buscarPorId(respondido.getId());
        assertThat(recuperado.getPacienteId()).isEqualTo("pac-1");
        assertThat(recuperado.getNivelUrgencia()).contains(NivelUrgencia.BAJO);
        assertThat(recuperado.getAlertaClinica()).isEmpty();
        assertThat(enfermeria.alertasRecibidas).isEmpty();
    }

    @Test
    void urgenciaAltaGeneraAlertaYNotificaAEnfermeria() {
        var respondido = service.responder("pac-1", "cita-1", List.of(new RespuestaTriage("Dolor de pecho", 7)));

        var alerta = service.buscarPorId(respondido.getId()).getAlertaClinica().orElseThrow();
        assertThat(alerta.estaActiva()).isTrue();
        assertThat(alerta.isNotificada()).isTrue();
        assertThat(enfermeria.alertasRecibidas).containsExactly(alerta.getId());
    }

    @Test
    void siFallaLaNotificacionLaAlertaQuedaGuardadaActivaYSinNotificar() {
        enfermeria.canalCaido = true;

        var respondido = service.responder("pac-1", "cita-1",
                List.of(new RespuestaTriage("dificultad para respirar", 8)));

        var alerta = service.buscarPorId(respondido.getId()).getAlertaClinica().orElseThrow();
        assertThat(alerta.estaActiva()).isTrue();
        assertThat(alerta.isNotificada()).isFalse();
        assertThat(service.alertasActivas()).extracting(c -> c.getId()).containsExactly(respondido.getId());
    }

    @Test
    void panelDeAlertasActivasMuestraSoloLasSinAtenderDeLaMasAntiguaALaMasReciente() {
        var primera = service.responder("pac-1", "cita-1", List.of(new RespuestaTriage("dolor de pecho", 9)));
        var segunda = service.responder("pac-2", "cita-2", List.of(new RespuestaTriage("sangrado abundante", 6)));
        var atendida = service.responder("pac-3", "cita-3", List.of(new RespuestaTriage("dolor abdominal", 10)));
        service.responder("pac-4", "cita-4", List.of(new RespuestaTriage("tos", 2)));
        service.atenderAlerta(atendida.getId(), "enf-1");

        assertThat(service.alertasActivas()).extracting(c -> c.getId())
                .containsExactly(primera.getId(), segunda.getId());
    }

    @Test
    void enfermeriaAtiendeLaAlertaUnaSolaVez() {
        var respondido = service.responder("pac-1", "cita-1", List.of(new RespuestaTriage("dolor de pecho", 9)));

        var atendido = service.atenderAlerta(respondido.getId(), "enf-1");

        var alerta = atendido.getAlertaClinica().orElseThrow();
        assertThat(alerta.estaActiva()).isFalse();
        assertThat(alerta.getAtendidaPor()).isEqualTo("enf-1");
        assertThat(alerta.getFechaAtencion()).isEqualTo(LocalDateTime.now(RELOJ));
        assertThatThrownBy(() -> service.atenderAlerta(respondido.getId(), "enf-2"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void elTriageEstaCompletadoSoloParaLaCitaQueLoRespondio() {
        service.responder("pac-1", "cita-1", List.of(new RespuestaTriage("fiebre", 5)));

        assertThat(service.triageCompletado("cita-1")).isTrue();
        assertThat(service.triageCompletado("cita-2")).isFalse();
    }

    @Test
    void buscarUnCuestionarioInexistenteFalla() {
        assertThatThrownBy(() -> service.buscarPorId("no-existe"))
                .isInstanceOf(CuestionarioTriageNoEncontradoException.class);
    }
}
