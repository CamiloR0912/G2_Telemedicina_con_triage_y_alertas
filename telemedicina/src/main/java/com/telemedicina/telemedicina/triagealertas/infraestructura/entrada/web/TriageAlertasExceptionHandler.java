package com.telemedicina.telemedicina.triagealertas.infraestructura.entrada.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriageNoEncontradoException;

/**
 * Convierte las excepciones del dominio en respuestas HTTP. Solo aplica a
 * {@link CuestionarioTriageController}, para no interferir con los controladores de otros subdominios.
 */
@RestControllerAdvice(assignableTypes = CuestionarioTriageController.class)
class TriageAlertasExceptionHandler {

    @ExceptionHandler(CuestionarioTriageNoEncontradoException.class)
    ProblemDetail cuestionarioNoEncontrado(CuestionarioTriageNoEncontradoException e) {
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
