package com.telemedicina.telemedicina.historiaclinica.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ControlAccesoHistoriaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-03T14:00:00Z"), ZONA);

    // Agenda simulada: solo med-1 tiene cita con pac-1
    private final HistorialCitas historialCitas =
            (medicoId, pacienteId) -> medicoId.equals("med-1") && pacienteId.equals("pac-1");

    private ControlAccesoHistoriaService servicio;
    private HistoriaClinica historia;

    @BeforeEach
    void setUp() {
        servicio = new ControlAccesoHistoriaService(historialCitas, RELOJ);
        historia = new HistoriaClinicaFactory().crear("pac-1", "O+", List.of("Penicilina"), "Asma");
    }

    @Test
    void medicoConCitaConsultaLaHistoriaYQuedaRegistrado() {
        HistoriaClinica resultado = servicio.consultarHistoria(historia, "med-1");

        assertThat(resultado).isSameAs(historia);
        assertThat(historia.getRegistrosAcceso())
                .containsExactly(new RegistroAcceso("med-1", LocalDateTime.now(RELOJ)));
    }

    @Test
    void medicoSinCitaNoPuedeConsultarLaHistoria() {
        assertThatThrownBy(() -> servicio.consultarHistoria(historia, "med-2"))
                .isInstanceOf(AccesoHistoriaDenegadoException.class);
        assertThat(historia.getRegistrosAcceso()).isEmpty();
    }

    @Test
    void medicoConCitaRegistraDiagnosticoNotasYReceta() {
        RegistroConsulta registro = servicio.registrarConsulta(
                historia, "med-1", "cons-1", "Faringitis", "Reposo 3 días", "Ibuprofeno 400 mg c/8h");

        assertThat(registro.getDiagnostico()).isEqualTo("Faringitis");
        assertThat(registro.getRecetaSimplificada()).isEqualTo("Ibuprofeno 400 mg c/8h");
        assertThat(registro.getFechaRegistro()).isEqualTo(LocalDateTime.now(RELOJ));
        assertThat(historia.getRegistrosConsulta()).containsExactly(registro);
    }

    @Test
    void medicoSinCitaNoPuedeRegistrarConsulta() {
        assertThatThrownBy(() -> servicio.registrarConsulta(
                historia, "med-2", "cons-1", "Faringitis", "", ""))
                .isInstanceOf(AccesoHistoriaDenegadoException.class);
        assertThat(historia.getRegistrosConsulta()).isEmpty();
    }
}
