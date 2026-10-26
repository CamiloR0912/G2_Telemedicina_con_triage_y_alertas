package com.telemedicina.telemedicina.historiaclinica.dominio;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Factory: concentra la validación y construcción de la raíz {@link HistoriaClinica}
 * (HU-01 · el paciente se registra con su historia clínica básica).
 */
public class HistoriaClinicaFactory {

    private static final Set<String> GRUPOS_SANGUINEOS =
            Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    public HistoriaClinica crear(String pacienteId, String grupoSanguineo,
                                 List<String> alergias, String antecedentes) {
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("La historia clínica requiere el id del paciente");
        }
        if (grupoSanguineo == null || !GRUPOS_SANGUINEOS.contains(grupoSanguineo.trim().toUpperCase())) {
            throw new IllegalArgumentException("Grupo sanguíneo inválido: " + grupoSanguineo);
        }
        List<String> alergiasLimpias = alergias == null ? List.of() : alergias.stream()
                .filter(a -> a != null && !a.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        return new HistoriaClinica(
                UUID.randomUUID().toString(),
                pacienteId.trim(),
                grupoSanguineo.trim().toUpperCase(),
                alergiasLimpias,
                antecedentes == null ? "" : antecedentes.trim());
    }
}
