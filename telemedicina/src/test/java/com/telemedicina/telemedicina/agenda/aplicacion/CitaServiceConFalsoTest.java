package com.telemedicina.telemedicina.agenda.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.telemedicina.telemedicina.agenda.dominio.CitaFactory;
import com.telemedicina.telemedicina.agenda.dominio.CitaNoEncontradaException;
import com.telemedicina.telemedicina.agenda.dominio.DisponibilidadService;
import com.telemedicina.telemedicina.agenda.dominio.EstadoCita;
import com.telemedicina.telemedicina.agenda.dominio.FranjaHoraria;
import com.telemedicina.telemedicina.agenda.dominio.FranjaNoDisponibleException;

/** Sin @SpringBootTest, sin @Mock, sin base de datos: el núcleo solo necesita algo que cumpla RepositorioCitas. */
class CitaServiceConFalsoTest {

	private static final LocalDateTime DIEZ = LocalDateTime.of(2026, 10, 20, 10, 0);
	private static final Clock RELOJ = Clock.fixed(DIEZ.minusDays(1).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

	private final RepositorioCitasFalso repositorio = new RepositorioCitasFalso();
	private final CitaService service = new CitaService(repositorio, new CitaFactory(RELOJ),
			new DisponibilidadService());

	@Test
	void agendaYRecuperaUnaCitaSinNingunaDependenciaDeSpringNiDeBaseDeDatos() {
		var agendada = service.agendar("pac-1", "med-1", "Cardiología", DIEZ, DIEZ.plusMinutes(30));

		assertNotNull(agendada.getId());
		var recuperada = service.buscarPorId(agendada.getId());
		assertEquals("Cardiología", recuperada.getEspecialidad());
		assertEquals(EstadoCita.AGENDADA, recuperada.getEstado());
	}

	@Test
	void noAgendaSiElMedicoYaTieneUnaCitaEnEsaFranja() {
		service.agendar("pac-1", "med-1", "Cardiología", DIEZ, DIEZ.plusMinutes(30));

		assertThrows(FranjaNoDisponibleException.class,
				() -> service.agendar("pac-2", "med-1", "Cardiología", DIEZ.plusMinutes(15), DIEZ.plusMinutes(45)));
	}

	@Test
	void cancelarLiberaElCupoParaOtroPaciente() {
		var cita = service.agendar("pac-1", "med-1", "Cardiología", DIEZ, DIEZ.plusMinutes(30));

		service.cancelar(cita.getId());
		var otra = service.agendar("pac-2", "med-1", "Cardiología", DIEZ, DIEZ.plusMinutes(30));

		assertEquals(EstadoCita.CANCELADA, service.buscarPorId(cita.getId()).getEstado());
		assertEquals(EstadoCita.AGENDADA, otra.getEstado());
	}

	@Test
	void reprogramaAUnaFranjaLibre() {
		var cita = service.agendar("pac-1", "med-1", "Cardiología", DIEZ, DIEZ.plusMinutes(30));

		service.reprogramar(cita.getId(), DIEZ.plusHours(1), DIEZ.plusHours(1).plusMinutes(30));

		var reprogramada = service.buscarPorId(cita.getId());
		assertEquals(new FranjaHoraria(DIEZ.plusHours(1), DIEZ.plusHours(1).plusMinutes(30)), reprogramada.getFranja());
		assertEquals(EstadoCita.REPROGRAMADA, reprogramada.getEstado());
	}

	@Test
	void agendaDelDiaMuestraSoloCitasActivasDelMedicoOrdenadasPorHora() {
		var tarde = service.agendar("pac-1", "med-1", "Cardiología", DIEZ.plusHours(4), DIEZ.plusHours(4).plusMinutes(30));
		var manana = service.agendar("pac-2", "med-1", "Cardiología", DIEZ, DIEZ.plusMinutes(30));
		var cancelada = service.agendar("pac-3", "med-1", "Cardiología", DIEZ.plusHours(1), DIEZ.plusHours(1).plusMinutes(30));
		service.cancelar(cancelada.getId());
		service.agendar("pac-4", "med-1", "Cardiología", DIEZ.plusDays(1), DIEZ.plusDays(1).plusMinutes(30));
		service.agendar("pac-5", "med-2", "Pediatría", DIEZ, DIEZ.plusMinutes(30));

		var agenda = service.agendaDelDia("med-1", DIEZ.toLocalDate());

		assertEquals(2, agenda.size());
		assertEquals(manana.getId(), agenda.get(0).getId());
		assertEquals(tarde.getId(), agenda.get(1).getId());
	}

	@Test
	void buscarUnaCitaInexistenteFalla() {
		assertThrows(CitaNoEncontradaException.class, () -> service.buscarPorId(UUID.randomUUID()));
	}
}
