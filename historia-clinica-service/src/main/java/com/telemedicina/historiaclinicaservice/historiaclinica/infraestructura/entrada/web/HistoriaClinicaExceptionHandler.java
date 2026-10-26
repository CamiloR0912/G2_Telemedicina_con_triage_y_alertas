package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.entrada.web;

import com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion.ServicioNoDisponibleException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.AccesoHistoriaDenegadoException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaDuplicadaException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinicaNoEncontradaException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.TriageNoCompletadoException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones del núcleo a códigos HTTP, solo para el controller de este subdominio. */
@RestControllerAdvice(assignableTypes = HistoriaClinicaController.class)
public class HistoriaClinicaExceptionHandler {

    @ExceptionHandler(AccesoHistoriaDenegadoException.class)
    ProblemDetail accesoDenegado(AccesoHistoriaDenegadoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(HistoriaClinicaNoEncontradaException.class)
    ProblemDetail noEncontrada(HistoriaClinicaNoEncontradaException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({HistoriaClinicaDuplicadaException.class, TriageNoCompletadoException.class,
            IllegalStateException.class})
    ProblemDetail conflicto(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /** agenda-service caído o circuito abierto: no se sabe si hay cita, así que no se entrega la historia. */
    @ExceptionHandler(ServicioNoDisponibleException.class)
    ProblemDetail servicioNoDisponible(ServicioNoDisponibleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail datosInvalidos(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
