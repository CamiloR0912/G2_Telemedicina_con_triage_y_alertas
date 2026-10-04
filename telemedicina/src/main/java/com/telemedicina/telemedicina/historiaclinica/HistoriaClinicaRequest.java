package com.telemedicina.telemedicina.historiaclinica;

import java.util.List;

public record HistoriaClinicaRequest(String pacienteId, String grupoSanguineo,
                                     List<String> alergias, String antecedentes) {
}
