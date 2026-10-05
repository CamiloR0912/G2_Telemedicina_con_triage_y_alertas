package com.telemedicina.telemedicina.triagealertas;

import java.util.List;

/**
 * Value Object: nivel de urgencia con el que se clasifica un cuestionario de triage.
 * HU-03 · "El cuestionario clasifica automáticamente un nivel de urgencia (bajo, medio, alto)".
 * Solo admite esos tres valores; cualquier otro se rechaza al construirlo.
 */
public record NivelUrgencia(String valor) {

    private static final List<String> VALORES_VALIDOS = List.of("bajo", "medio", "alto");

    public static final NivelUrgencia BAJO = new NivelUrgencia("bajo");
    public static final NivelUrgencia MEDIO = new NivelUrgencia("medio");
    public static final NivelUrgencia ALTO = new NivelUrgencia("alto");

    public NivelUrgencia {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El nivel de urgencia es obligatorio");
        }
        valor = valor.trim().toLowerCase();
        if (!VALORES_VALIDOS.contains(valor)) {
            throw new IllegalArgumentException(
                    "Nivel de urgencia inválido: " + valor + " (debe ser bajo, medio o alto)");
        }
    }

    /** HU-03 · solo el nivel alto genera una alerta clínica. */
    public boolean esAlto() {
        return ALTO.valor.equals(valor);
    }

    /** HU-09 · permite ordenar por urgencia: bajo = 1, medio = 2, alto = 3. */
    public int prioridad() {
        return VALORES_VALIDOS.indexOf(valor) + 1;
    }
}
