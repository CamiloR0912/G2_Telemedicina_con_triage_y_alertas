package com.telemedicina.telemedicina.triagealertas.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClasificacionTriageServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZONA);

    private final CuestionarioTriageFactory factory = new CuestionarioTriageFactory(RELOJ);
    private ClasificacionTriageService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ClasificacionTriageService(RELOJ);
    }

    private CuestionarioTriage cuestionario(RespuestaTriage... respuestas) {
        return factory.crear("pac-1", "cita-1", List.of(respuestas));
    }

    @Test
    void sintomaDeAlarmaClasificaAltoYGeneraAlertaActiva() {
        CuestionarioTriage triage = cuestionario(new RespuestaTriage("Dolor de pecho", 7));

        NivelUrgencia nivel = servicio.clasificar(triage);

        assertThat(nivel).isEqualTo(NivelUrgencia.ALTO);
        AlertaClinica alerta = triage.getAlertaClinica().orElseThrow();
        assertThat(alerta.estaActiva()).isTrue();
        assertThat(alerta.getNivelUrgencia()).isEqualTo(NivelUrgencia.ALTO);
        assertThat(alerta.getFechaGeneracion()).isEqualTo(LocalDateTime.now(RELOJ));
        // Avisar a enfermería ya no es tarea del dominio: lo hace el caso de uso (CuestionarioTriageService)
        assertThat(alerta.isNotificada()).isFalse();
    }

    @Test
    void cualquierSintomaSeveroClasificaAlto() {
        CuestionarioTriage triage = cuestionario(new RespuestaTriage("dolor abdominal", 9));

        assertThat(servicio.clasificar(triage)).isEqualTo(NivelUrgencia.ALTO);
        assertThat(triage.tieneAlertaActiva()).isTrue();
    }

    @Test
    void sintomaModeradoClasificaMedioSinAlerta() {
        CuestionarioTriage triage = cuestionario(new RespuestaTriage("fiebre", 5));

        assertThat(servicio.clasificar(triage)).isEqualTo(NivelUrgencia.MEDIO);
        assertThat(triage.getAlertaClinica()).isEmpty();
    }

    @Test
    void tresSintomasLevesClasificanMedio() {
        CuestionarioTriage triage = cuestionario(
                new RespuestaTriage("tos", 2),
                new RespuestaTriage("fiebre", 3),
                new RespuestaTriage("dolor de cabeza", 2));

        assertThat(servicio.clasificar(triage)).isEqualTo(NivelUrgencia.MEDIO);
    }

    @Test
    void sintomasLevesClasificanBajo() {
        CuestionarioTriage triage = cuestionario(
                new RespuestaTriage("tos", 2),
                new RespuestaTriage("dolor de pecho", 1));

        assertThat(servicio.clasificar(triage)).isEqualTo(NivelUrgencia.BAJO);
        assertThat(triage.estaCompletado()).isTrue();
        assertThat(triage.getAlertaClinica()).isEmpty();
    }

    @Test
    void unCuestionarioNoSeClasificaDosVeces() {
        CuestionarioTriage triage = cuestionario(new RespuestaTriage("dolor de pecho", 9));
        servicio.clasificar(triage);

        assertThatThrownBy(() -> servicio.clasificar(triage))
                .isInstanceOf(IllegalStateException.class);
        assertThat(triage.getAlertaClinica()).isPresent();
    }
}
