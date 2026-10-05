package com.telemedicina.telemedicina.triagealertas;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Factory: concentra la validación y construcción de la raíz {@link CuestionarioTriage}
 * (HU-03 · el paciente responde el cuestionario de triage antes de su consulta).
 */
public class CuestionarioTriageFactory {

    private final Clock reloj;

    public CuestionarioTriageFactory(Clock reloj) {
        this.reloj = reloj;
    }

    public CuestionarioTriage crear(String pacienteId, String citaId, List<RespuestaTriage> respuestas) {
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("El cuestionario de triage requiere el id del paciente");
        }
        if (citaId == null || citaId.isBlank()) {
            throw new IllegalArgumentException("El cuestionario de triage requiere el id de la cita");
        }
        if (respuestas == null || respuestas.isEmpty()) {
            throw new IllegalArgumentException("El cuestionario de triage debe tener al menos una respuesta");
        }
        Set<String> sintomas = new HashSet<>();
        for (RespuestaTriage respuesta : respuestas) {
            if (respuesta == null) {
                throw new IllegalArgumentException("El cuestionario de triage no admite respuestas nulas");
            }
            if (!sintomas.add(respuesta.sintoma())) {
                throw new IllegalArgumentException("Síntoma repetido en el cuestionario: " + respuesta.sintoma());
            }
        }

        return new CuestionarioTriage(
                UUID.randomUUID().toString(),
                pacienteId.trim(),
                citaId.trim(),
                respuestas,
                LocalDateTime.now(reloj));
    }
}
