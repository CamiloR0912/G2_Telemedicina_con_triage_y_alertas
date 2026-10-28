package com.telemedicina.telemedicina.historiaclinica.dominio;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Invariantes del límite del Agregado: todo cambio pasa por la raíz. */
class HistoriaClinicaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 3, 9, 0);

    private final HistoriaClinica historia =
            new HistoriaClinicaFactory().crear("pac-1", "A+", List.of(), "");

    @Test
    void lasColeccionesInternasNoSeModificanDesdeAfuera() {
        assertThatThrownBy(() -> historia.getRegistrosAcceso().add(new RegistroAcceso("med-1", FECHA)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> historia.getAlergias().add("Látex"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void unaConsultaNoSePuedeRegistrarDosVeces() {
        historia.registrarConsulta("cons-1", "med-1", "Gripa", "", "", FECHA);

        assertThatThrownBy(() -> historia.registrarConsulta("cons-1", "med-1", "Otra", "", "", FECHA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void elDiagnosticoEsObligatorio() {
        assertThatThrownBy(() -> historia.registrarConsulta("cons-1", "med-1", " ", "", "", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
