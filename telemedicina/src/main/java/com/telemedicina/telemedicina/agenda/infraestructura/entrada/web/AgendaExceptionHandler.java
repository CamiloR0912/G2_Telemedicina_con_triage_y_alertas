package com.telemedicina.telemedicina.agenda.infraestructura.entrada.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.telemedicina.telemedicina.agenda.dominio.CitaNoEncontradaException;
import com.telemedicina.telemedicina.agenda.dominio.FranjaNoDisponibleException;

/**
 * Convierte las excepciones del dominio en respuestas HTTP. Solo aplica a
 * {@link CitaController}, para no interferir con los controladores de otros subdominios.
 */
@RestControllerAdvice(assignableTypes = CitaController.class)
class AgendaExceptionHandler {

	@ExceptionHandler(CitaNoEncontradaException.class)
	ProblemDetail citaNoEncontrada(CitaNoEncontradaException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	/** Franja ocupada, cita ya cancelada o reprogramar una cancelada: chocan con el estado actual. */
	@ExceptionHandler({ FranjaNoDisponibleException.class, IllegalStateException.class })
	ProblemDetail conflicto(RuntimeException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ProblemDetail datosInvalidos(IllegalArgumentException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}
}
