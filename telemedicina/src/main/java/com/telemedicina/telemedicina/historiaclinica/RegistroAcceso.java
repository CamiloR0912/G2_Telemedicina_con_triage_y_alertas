package com.telemedicina.telemedicina.historiaclinica;

import java.time.LocalDateTime;

/**
 * Value Object: constancia de que un médico consultó una historia clínica.
 * HU-06 · "Cada acceso a una historia clínica queda registrado con fecha, hora y médico".
 * El médico se referencia solo por id: pertenece a otro subdominio.
 */
public record RegistroAcceso(String medicoId, LocalDateTime fechaHora) {

    public RegistroAcceso {
        if (medicoId == null || medicoId.isBlank()) {
            throw new IllegalArgumentException("El registro de acceso requiere el id del médico");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("El registro de acceso requiere fecha y hora");
        }
        medicoId = medicoId.trim();
    }
}
