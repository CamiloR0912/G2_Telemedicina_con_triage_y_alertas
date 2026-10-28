package com.telemedicina.agendaservice.agenda.dominio;

import java.util.UUID;

public class CitaNoEncontradaException extends RuntimeException {

	public CitaNoEncontradaException(UUID id) {
		super("No existe la cita " + id);
	}
}
