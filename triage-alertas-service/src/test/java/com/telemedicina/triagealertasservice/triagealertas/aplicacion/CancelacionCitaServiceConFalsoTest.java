package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.telemedicina.triagealertasservice.triagealertas.dominio.ClasificacionTriageService;
import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriageFactory;
import com.telemedicina.triagealertasservice.triagealertas.dominio.RespuestaTriage;

/** Paso de Triage en la Saga de cancelación, sin Spring ni Kafka: un publicador falso anota la respuesta. */
class CancelacionCitaServiceConFalsoTest {

    private static final Clock RELOJ =
            Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZoneId.of("America/Bogota"));

    private final RepositorioCuestionariosTriageFalso repositorio = new RepositorioCuestionariosTriageFalso();
    private final List<String> respuestas = new ArrayList<>();
    private final CancelacionCitaService saga = new CancelacionCitaService(repositorio,
            new PublicadorRespuestaCancelacion() {
                @Override
                public void cancelacionConfirmada(String citaId) {
                    respuestas.add("confirmada:" + citaId);
                }

                @Override
                public void cancelacionRechazada(String citaId, String motivo) {
                    respuestas.add("rechazada:" + citaId);
                }
            });
    private final CuestionarioTriageService triage = new CuestionarioTriageService(repositorio,
            new NotificadorEnfermeriaFalso(), c -> { }, new CuestionarioTriageFactory(RELOJ),
            new ClasificacionTriageService(RELOJ), RELOJ);

    @Test
    void confirmaSiLaCitaNoTieneTriageOSoloUrgenciaBaja() {
        triage.responder("pac-1", "cita-baja", List.of(new RespuestaTriage("fiebre", 2)));

        assertThat(saga.evaluarCancelacion("cita-baja")).isTrue();
        assertThat(saga.evaluarCancelacion("cita-sin-triage")).isTrue();
        assertThat(respuestas).containsExactly("confirmada:cita-baja", "confirmada:cita-sin-triage");
    }

    @Test
    void rechazaMientrasLaAlertaUrgenteNoSeAtiendeYConfirmaDespues() {
        var alto = triage.responder("pac-1", "cita-alta", List.of(new RespuestaTriage("Dolor de pecho", 9)));

        assertThat(saga.evaluarCancelacion("cita-alta")).isFalse();

        triage.atenderAlerta(alto.getId(), "enf-1");

        assertThat(saga.evaluarCancelacion("cita-alta")).isTrue();
        assertThat(respuestas).containsExactly("rechazada:cita-alta", "confirmada:cita-alta");
    }
}
