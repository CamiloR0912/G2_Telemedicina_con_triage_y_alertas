package com.telemedicina.telemedicina.agenda.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class CitaFactoryTest {

	private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 8, 0);
	private static final Clock RELOJ = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

	private final CitaFactory factory = new CitaFactory(RELOJ);

	@Test
	void agendaUnaCitaValida() {
		var inicio = AHORA.plusHours(2);

		var cita = factory.agendar("pac-1", "med-1", "Cardiología", inicio, inicio.plusMinutes(30));

		assertNotNull(cita.getId());
		assertEquals("pac-1", cita.getPacienteId());
		assertEquals("med-1", cita.getMedicoId());
		assertEquals("Cardiología", cita.getEspecialidad());
		assertEquals(new FranjaHoraria(inicio, inicio.plusMinutes(30)), cita.getFranja());
		assertEquals(EstadoCita.AGENDADA, cita.getEstado());
		assertTrue(cita.estaActiva());
	}

	@Test
	void cadaCitaRecibeUnIdDistinto() {
		var inicio = AHORA.plusHours(2);

		var una = factory.agendar("pac-1", "med-1", "Cardiología", inicio, inicio.plusMinutes(30));
		var otra = factory.agendar("pac-1", "med-1", "Cardiología", inicio, inicio.plusMinutes(30));

		assertNotEquals(una.getId(), otra.getId());
	}

	@Test
	void limpiaEspaciosDeLosDatos() {
		var inicio = AHORA.plusHours(2);

		var cita = factory.agendar(" pac-1 ", " med-1 ", " Pediatría ", inicio, inicio.plusMinutes(30));

		assertEquals("pac-1", cita.getPacienteId());
		assertEquals("med-1", cita.getMedicoId());
		assertEquals("Pediatría", cita.getEspecialidad());
	}

	@Test
	void rechazaDatosObligatoriosVacios() {
		var inicio = AHORA.plusHours(2);
		var fin = inicio.plusMinutes(30);

		assertThrows(IllegalArgumentException.class, () -> factory.agendar(null, "med-1", "Cardiología", inicio, fin));
		assertThrows(IllegalArgumentException.class, () -> factory.agendar("pac-1", " ", "Cardiología", inicio, fin));
		assertThrows(IllegalArgumentException.class, () -> factory.agendar("pac-1", "med-1", "", inicio, fin));
	}

	@Test
	void rechazaFranjaInvalida() {
		var inicio = AHORA.plusHours(2);

		assertThrows(IllegalArgumentException.class,
				() -> factory.agendar("pac-1", "med-1", "Cardiología", inicio, inicio.minusMinutes(30)));
	}

	@Test
	void rechazaCitasEnElPasado() {
		assertThrows(IllegalArgumentException.class,
				() -> factory.agendar("pac-1", "med-1", "Cardiología", AHORA.minusHours(1), AHORA.minusMinutes(30)));
		assertThrows(IllegalArgumentException.class,
				() -> factory.agendar("pac-1", "med-1", "Cardiología", AHORA, AHORA.plusMinutes(30)));
	}
}
