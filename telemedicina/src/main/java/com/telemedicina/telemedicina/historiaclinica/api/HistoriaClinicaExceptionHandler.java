package com.telemedicina.telemedicina.historiaclinica.api;

import com.telemedicina.telemedicina.historiaclinica.AccesoHistoriaDenegadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones del dominio de Historia clínica a códigos HTTP, solo para su controller. */
@RestControllerAdvice(assignableTypes = HistoriaClinicaController.class)
class HistoriaClinicaExceptionHandler {

    @ExceptionHandler(AccesoHistoriaDenegadoException.class)
    ProblemDetail accesoDenegado(AccesoHistoriaDenegadoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(HistoriaClinicaNoEncontradaException.class)
    ProblemDetail noEncontrada(HistoriaClinicaNoEncontradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({HistoriaClinicaDuplicadaException.class, IllegalStateException.class})
    ProblemDetail conflicto(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail datosInvalidos(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
