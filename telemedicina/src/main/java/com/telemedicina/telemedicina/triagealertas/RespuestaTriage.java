package com.telemedicina.telemedicina.triagealertas;

import java.text.Normalizer;

/**
 * Value Object: una respuesta del cuestionario de triage (síntoma reportado y su intensidad).
 * HU-03 · el paciente responde el cuestionario de síntomas antes de su consulta.
 * La intensidad va de 0 (no lo presenta) a 10 (máxima).
 */
public record RespuestaTriage(String sintoma, int intensidad) {

    public static final int INTENSIDAD_MINIMA = 0;
    public static final int INTENSIDAD_MAXIMA = 10;

    public RespuestaTriage {
        if (sintoma == null || sintoma.isBlank()) {
            throw new IllegalArgumentException("La respuesta de triage requiere el síntoma");
        }
        if (intensidad < INTENSIDAD_MINIMA || intensidad > INTENSIDAD_MAXIMA) {
            throw new IllegalArgumentException(
                    "La intensidad del síntoma debe estar entre 0 y 10: " + intensidad);
        }
        // "Pérdida de conciencia" y "perdida de conciencia" son el mismo síntoma
        sintoma = Normalizer.normalize(sintoma.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
