package com.telemedicina.telemedicina.triagealertas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuestionarioTriageTest {

    private static final Clock RELOJ =
            Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime AHORA = LocalDateTime.now(RELOJ);

    private CuestionarioTriage triage;

    @BeforeEach
    void setUp() {
        triage = new CuestionarioTriageFactory(RELOJ)
                .crear("pac-1", "cita-1", List.of(new RespuestaTriage("fiebre", 4)));
    }

    @Test
    void recienRespondidoNoEstaCompletadoNiTieneAlerta() {
        assertThat(triage.estaCompletado()).isFalse();
        assertThat(triage.getNivelUrgencia()).isEmpty();
        assertThat(triage.getAlertaClinica()).isEmpty();
    }

    @Test
    void lasRespuestasNoSePuedenModificarDesdeAfuera() {
        assertThatThrownBy(() -> triage.getRespuestas().add(new RespuestaTriage("tos", 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void lasRespuestasNoCambianSiSeModificaLaListaOriginal() {
        List<RespuestaTriage> respuestas = new ArrayList<>(List.of(new RespuestaTriage("tos", 2)));
        CuestionarioTriage otro = new CuestionarioTriageFactory(RELOJ).crear("pac-2", "cita-2", respuestas);

        respuestas.add(new RespuestaTriage("dolor de pecho", 10));

        assertThat(otro.getRespuestas()).hasSize(1);
    }

    @Test
    void soloUnTriageAltoPuedeGenerarAlerta() {
        triage.clasificar(NivelUrgencia.MEDIO, AHORA);

        assertThatThrownBy(() -> triage.generarAlerta("alerta-1", AHORA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unTriageAltoGeneraUnaSolaAlerta() {
        triage.clasificar(NivelUrgencia.ALTO, AHORA);
        triage.generarAlerta("alerta-1", AHORA);

        assertThatThrownBy(() -> triage.generarAlerta("alerta-2", AHORA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enfermeriaAtiendeLaAlertaYDejaDeEstarActiva() {
        triage.clasificar(NivelUrgencia.ALTO, AHORA);
        triage.generarAlerta("alerta-1", AHORA);

        triage.atenderAlerta("enf-1", AHORA.plusMinutes(2));

        AlertaClinica alerta = triage.getAlertaClinica().orElseThrow();
        assertThat(triage.tieneAlertaActiva()).isFalse();
        assertThat(alerta.getAtendidaPor()).isEqualTo("enf-1");
        assertThat(alerta.getFechaAtencion()).isEqualTo(AHORA.plusMinutes(2));
        assertThatThrownBy(() -> triage.atenderAlerta("enf-2", AHORA.plusMinutes(3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noSePuedeAtenderUnaAlertaQueNoExiste() {
        assertThatThrownBy(() -> triage.atenderAlerta("enf-1", AHORA))
                .isInstanceOf(IllegalStateException.class);
    }
}
