package com.telemedicina.telemedicina.triagealertas.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones del dominio de Triage y alertas a códigos HTTP, solo para su controller. */
@RestControllerAdvice(assignableTypes = CuestionarioTriageController.class)
class TriageAlertasExceptionHandler {

    @ExceptionHandler(CuestionarioTriageNoEncontradoException.class)
    ProblemDetail noEncontrado(CuestionarioTriageNoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Atender una alerta ya atendida o inexistente: choca con el estado actual del cuestionario. */
    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail conflicto(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail datosInvalidos(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
