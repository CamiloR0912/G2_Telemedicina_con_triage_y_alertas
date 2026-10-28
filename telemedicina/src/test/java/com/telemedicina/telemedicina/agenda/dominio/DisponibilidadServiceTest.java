package com.telemedicina.telemedicina.agenda.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

class DisponibilidadServiceTest {

	private static final LocalDateTime DIEZ = LocalDateTime.of(2026, 10, 20, 10, 0);
	private static final Clock RELOJ = Clock.fixed(DIEZ.minusDays(1).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

	private final CitaFactory factory = new CitaFactory(RELOJ);
	private final DisponibilidadService servicio = new DisponibilidadService();

	private static FranjaHoraria franja(int minutoInicio, int minutoFin) {
		return new FranjaHoraria(DIEZ.plusMinutes(minutoInicio), DIEZ.plusMinutes(minutoFin));
	}

	private Cita cita(String pacienteId, String medicoId, int minutoInicio, int minutoFin) {
		return factory.agendar(pacienteId, medicoId, "Medicina general",
				DIEZ.plusMinutes(minutoInicio), DIEZ.plusMinutes(minutoFin));
	}

	@Test
	void franjaLibreEstaDisponible() {
		var existentes = List.of(cita("pac-1", "med-1", 0, 30));

		assertDoesNotThrow(() -> servicio.verificarDisponibilidad("med-1", "pac-2", franja(30, 60), existentes));
	}

	@Test
	void rechazaSolapeConCitaDelMismoMedico() {
		var existentes = List.of(cita("pac-1", "med-1", 0, 30));

		var error = assertThrows(FranjaNoDisponibleException.class,
				() -> servicio.verificarDisponibilidad("med-1", "pac-2", franja(15, 45), existentes));
		assertTrue(error.getMessage().contains("médico"));
	}

	@Test
	void rechazaSolapeConCitaDelMismoPaciente() {
		var existentes = List.of(cita("pac-1", "med-1", 0, 30));

		var error = assertThrows(FranjaNoDisponibleException.class,
				() -> servicio.verificarDisponibilidad("med-2", "pac-1", franja(15, 45), existentes));
		assertTrue(error.getMessage().contains("paciente"));
	}

	@Test
	void permiteMismaFranjaConOtroMedicoYOtroPaciente() {
		var existentes = List.of(cita("pac-1", "med-1", 0, 30));

		assertTrue(servicio.estaDisponible("med-2", "pac-2", franja(0, 30), existentes));
	}

	@Test
	void citaCanceladaLiberaElCupo() {
		var cancelada = cita("pac-1", "med-1", 0, 30);
		cancelada.cancelar();

		assertTrue(servicio.estaDisponible("med-1", "pac-2", franja(0, 30), List.of(cancelada)));
	}

	@Test
	void reprogramaSiLaNuevaFranjaEstaLibre() {
		var cita = cita("pac-1", "med-1", 0, 30);
		var otra = cita("pac-2", "med-1", 60, 90);

		servicio.reprogramar(cita, franja(30, 60), List.of(cita, otra));

		assertEquals(franja(30, 60), cita.getFranja());
		assertEquals(EstadoCita.REPROGRAMADA, cita.getEstado());
	}

	@Test
	void reprogramarNoChocaConsigoMisma() {
		var cita = cita("pac-1", "med-1", 0, 30);

		assertDoesNotThrow(() -> servicio.reprogramar(cita, franja(15, 45), List.of(cita)));
	}

	@Test
	void noReprogramaSobreUnaFranjaOcupada() {
		var cita = cita("pac-1", "med-1", 0, 30);
		var otra = cita("pac-2", "med-1", 60, 90);

		assertThrows(FranjaNoDisponibleException.class,
				() -> servicio.reprogramar(cita, franja(60, 90), List.of(cita, otra)));
		assertEquals(franja(0, 30), cita.getFranja());
		assertEquals(EstadoCita.AGENDADA, cita.getEstado());
	}

	@Test
	void noReprogramaUnaCitaCancelada() {
		var cita = cita("pac-1", "med-1", 0, 30);
		cita.cancelar();

		assertThrows(IllegalStateException.class,
				() -> servicio.reprogramar(cita, franja(30, 60), List.of(cita)));
	}

	@Test
	void noSeCancelaDosVeces() {
		var cita = cita("pac-1", "med-1", 0, 30);
		cita.cancelar();

		assertFalse(cita.estaActiva());
		assertThrows(IllegalStateException.class, cita::cancelar);
	}
}
