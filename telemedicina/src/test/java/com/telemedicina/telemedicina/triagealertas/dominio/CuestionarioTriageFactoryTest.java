package com.telemedicina.telemedicina.triagealertas.dominio;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuestionarioTriageFactoryTest {

    private static final Clock RELOJ =
            Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZoneId.of("America/Bogota"));

    private final CuestionarioTriageFactory factory = new CuestionarioTriageFactory(RELOJ);
    private final List<RespuestaTriage> respuestas = List.of(new RespuestaTriage("fiebre", 4));

    @Test
    void creaElCuestionarioConIdFechaYReferenciasPorId() {
        CuestionarioTriage triage = factory.crear(" pac-1 ", " cita-1 ", respuestas);

        assertThat(triage.getId()).isNotBlank();
        assertThat(triage.getPacienteId()).isEqualTo("pac-1");
        assertThat(triage.getCitaId()).isEqualTo("cita-1");
        assertThat(triage.getRespuestas()).containsExactlyElementsOf(respuestas);
        assertThat(triage.getFechaRespuesta()).isEqualTo(LocalDateTime.now(RELOJ));
    }

    @Test
    void cadaCuestionarioTieneSuPropioId() {
        assertThat(factory.crear("pac-1", "cita-1", respuestas).getId())
                .isNotEqualTo(factory.crear("pac-1", "cita-1", respuestas).getId());
    }

    @Test
    void exigePacienteYCita() {
        assertThatThrownBy(() -> factory.crear(" ", "cita-1", respuestas))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.crear("pac-1", null, respuestas))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exigeAlMenosUnaRespuestaYNingunaNula() {
        assertThatThrownBy(() -> factory.crear("pac-1", "cita-1", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.crear("pac-1", "cita-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.crear("pac-1", "cita-1",
                Arrays.asList(new RespuestaTriage("tos", 1), null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaSintomasRepetidos() {
        assertThatThrownBy(() -> factory.crear("pac-1", "cita-1", List.of(
                new RespuestaTriage("Pérdida de conciencia", 3),
                new RespuestaTriage("perdida de conciencia", 6))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
