package com.telemedicina.telemedicina.agenda.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class FranjaHorariaTest {

	private static final LocalDateTime DIEZ = LocalDateTime.of(2026, 10, 20, 10, 0);

	private static FranjaHoraria franja(int minutoInicio, int minutoFin) {
		return new FranjaHoraria(DIEZ.plusMinutes(minutoInicio), DIEZ.plusMinutes(minutoFin));
	}

	@Test
	void creaUnaFranjaValida() {
		var franja = franja(0, 30);

		assertEquals(DIEZ, franja.inicio());
		assertEquals(Duration.ofMinutes(30), franja.duracion());
	}

	@Test
	void rechazaInicioOFinNulos() {
		assertThrows(IllegalArgumentException.class, () -> new FranjaHoraria(null, DIEZ));
		assertThrows(IllegalArgumentException.class, () -> new FranjaHoraria(DIEZ, null));
	}

	@Test
	void rechazaInicioPosteriorAlFin() {
		assertThrows(IllegalArgumentException.class, () -> franja(30, 0));
	}

	@Test
	void rechazaInicioIgualAlFin() {
		assertThrows(IllegalArgumentException.class, () -> franja(0, 0));
	}

	@Test
	void detectaSolapeParcialYContenido() {
		assertTrue(franja(0, 30).seSolapaCon(franja(15, 45)));
		assertTrue(franja(0, 60).seSolapaCon(franja(15, 30)));
		assertTrue(franja(15, 30).seSolapaCon(franja(0, 60)));
	}

	@Test
	void franjasConsecutivasNoSeSolapan() {
		assertFalse(franja(0, 30).seSolapaCon(franja(30, 60)));
		assertFalse(franja(30, 60).seSolapaCon(franja(0, 30)));
	}

	@Test
	void dosFranjasConLosMismosValoresSonIguales() {
		assertEquals(franja(0, 30), franja(0, 30));
	}

	@Test
	void sabeAQueDiaPertenece() {
		assertTrue(franja(0, 30).esDelDia(LocalDate.of(2026, 10, 20)));
		assertFalse(franja(0, 30).esDelDia(LocalDate.of(2026, 10, 21)));
	}
}
