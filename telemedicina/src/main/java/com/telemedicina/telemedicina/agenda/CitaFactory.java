package com.telemedicina.telemedicina.agenda;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory: concentra la validación y construcción de una Cita nueva.
 * Nadie fuera del paquete puede hacer {@code new Cita(...)}.
 *
 * Recibe un {@link Clock} para que la regla "no se agenda en el pasado"
 * se pueda probar sin depender de la hora real.
 */
public class CitaFactory {

	private final Clock reloj;

	public CitaFactory(Clock reloj) {
		this.reloj = reloj;
	}

	public CitaFactory() {
		this(Clock.systemDefaultZone());
	}

	public Cita agendar(String pacienteId, String medicoId, String especialidad,
			LocalDateTime inicio, LocalDateTime fin) {
		exigirTexto(pacienteId, "El id del paciente es obligatorio");
		exigirTexto(medicoId, "El id del médico es obligatorio");
		exigirTexto(especialidad, "La especialidad es obligatoria");

		var franja = new FranjaHoraria(inicio, fin);
		if (!franja.inicio().isAfter(LocalDateTime.now(reloj))) {
			throw new IllegalArgumentException("No se puede agendar una cita en el pasado");
		}

		return new Cita(UUID.randomUUID(), pacienteId.trim(), medicoId.trim(), especialidad.trim(), franja);
	}

	private static void exigirTexto(String valor, String mensaje) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException(mensaje);
		}
	}
}
