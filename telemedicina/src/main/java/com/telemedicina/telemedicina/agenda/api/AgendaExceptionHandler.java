package com.telemedicina.telemedicina.agenda.api;

import com.telemedicina.telemedicina.agenda.FranjaNoDisponibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones del dominio de Agenda a códigos HTTP, solo para su controller. */
@RestControllerAdvice(assignableTypes = CitaController.class)
class AgendaExceptionHandler {

	@ExceptionHandler(CitaNoEncontradaException.class)
	ProblemDetail noEncontrada(CitaNoEncontradaException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	/** Franja ocupada, cita ya cancelada o reprogramar una cancelada: choca con el estado actual. */
	@ExceptionHandler({FranjaNoDisponibleException.class, IllegalStateException.class})
	ProblemDetail conflicto(RuntimeException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail datosInvalidos(IllegalArgumentException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}
}
