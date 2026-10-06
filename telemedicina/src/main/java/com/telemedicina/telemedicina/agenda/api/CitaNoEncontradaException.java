package com.telemedicina.telemedicina.agenda.api;

import java.util.UUID;

public class CitaNoEncontradaException extends RuntimeException {

	public CitaNoEncontradaException(UUID id) {
		super("No existe la cita " + id);
	}
}
