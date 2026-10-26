package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

/**
 * "No pude averiguar si hay cita" (503) es distinto de "no hay cita" (403): el cliente de la API
 * tiene que poder diferenciarlos.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
