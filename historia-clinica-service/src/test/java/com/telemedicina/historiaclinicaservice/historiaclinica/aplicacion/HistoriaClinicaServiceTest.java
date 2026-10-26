package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.AccesoHistoriaDenegadoException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.ControlAccesoHistoriaService;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinica;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaDuplicadaException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaFactory;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaNoEncontradaException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistorialCitas;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.TriageNoCompletadoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El caso de uso delega en el puerto secundario: se prueba con el repositorio mockeado. */
@ExtendWith(MockitoExtension.class)
class HistoriaClinicaServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZoneId.of("America/Bogota"));

    @Mock
    private RepositorioHistoriasClinicas repositorioHistoriasClinicas;

    private final HistoriaClinicaFactory factory = new HistoriaClinicaFactory();
    private final TriagesCompletadosFalso triagesCompletados = new TriagesCompletadosFalso();
    private HistoriaClinicaService servicio;

    @BeforeEach
    void setUp() {
        HistorialCitas historialCitas = (medicoId, pacienteId) -> medicoId.equals("med-1");
        servicio = new HistoriaClinicaService(repositorioHistoriasClinicas, factory,
                new ControlAccesoHistoriaService(historialCitas, RELOJ), triagesCompletados);
    }

    @Test
    void registraUnaHistoriaNueva() {
        when(repositorioHistoriasClinicas.existePorPacienteId("pac-1")).thenReturn(false);
        when(repositorioHistoriasClinicas.guardar(any())).thenAnswer(i -> i.getArgument(0));

        HistoriaClinica historia = servicio.registrar(" pac-1 ", "O+", List.of("Penicilina"), "");

        assertThat(historia.getPacienteId()).isEqualTo("pac-1");
        verify(repositorioHistoriasClinicas).guardar(historia);
    }

    @Test
    void noRegistraDosHistoriasParaElMismoPaciente() {
        when(repositorioHistoriasClinicas.existePorPacienteId("pac-1")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar("pac-1", "O+", List.of(), ""))
                .isInstanceOf(HistoriaClinicaDuplicadaException.class);
        verify(repositorioHistoriasClinicas, never()).guardar(any());
    }

    @Test
    void consultarGuardaElRegistroDeAcceso() {
        HistoriaClinica historia = factory.crear("pac-1", "O+", List.of(), "");
        when(repositorioHistoriasClinicas.buscarPorPacienteId("pac-1")).thenReturn(Optional.of(historia));
        when(repositorioHistoriasClinicas.guardar(historia)).thenReturn(historia);

        servicio.consultar("pac-1", "med-1");

        assertThat(historia.getRegistrosAcceso()).hasSize(1);
        verify(repositorioHistoriasClinicas).guardar(historia);
    }

    @Test
    void consultarHistoriaInexistenteFalla() {
        when(repositorioHistoriasClinicas.buscarPorPacienteId("pac-9")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultar("pac-9", "med-1"))
                .isInstanceOf(HistoriaClinicaNoEncontradaException.class);
    }

    @Test
    void medicoSinCitaNoRegistraConsultaNiSeGuardaNada() {
        HistoriaClinica historia = factory.crear("pac-1", "O+", List.of(), "");
        when(repositorioHistoriasClinicas.buscarPorPacienteId("pac-1")).thenReturn(Optional.of(historia));

        servicio.registrarTriageCompletado("cita-1");

        assertThatThrownBy(() -> servicio.registrarConsulta("pac-1", "med-2", "cita-1", "cons-1", "Gripa", "", ""))
                .isInstanceOf(AccesoHistoriaDenegadoException.class);
        verify(repositorioHistoriasClinicas, never()).guardar(any());
    }

    @Test
    void sinTriageCompletadoNoSeRegistraLaConsultaNiSeGuardaNada() {
        HistoriaClinica historia = factory.crear("pac-1", "O+", List.of(), "");
        when(repositorioHistoriasClinicas.buscarPorPacienteId("pac-1")).thenReturn(Optional.of(historia));

        assertThatThrownBy(() -> servicio.registrarConsulta("pac-1", "med-1", "cita-1", "cons-1", "Gripa", "", ""))
                .isInstanceOf(TriageNoCompletadoException.class);
        assertThat(historia.getRegistrosConsulta()).isEmpty();
        verify(repositorioHistoriasClinicas, never()).guardar(any());
    }

    @Test
    void registrarConsultaSinCitaEsDatoInvalido() {
        assertThatThrownBy(() -> servicio.registrarConsulta("pac-1", "med-1", " ", "cons-1", "Gripa", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
