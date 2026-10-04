package com.telemedicina.telemedicina.historiaclinica;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador secundario temporal: guarda las historias en memoria mientras no existe el adaptador JPA.
 */
@Component
public class RepositorioHistoriasClinicasEnMemoria implements RepositorioHistoriasClinicas {

    private final Map<String, HistoriaClinica> historiasPorPaciente = new ConcurrentHashMap<>();

    @Override
    public Optional<HistoriaClinica> buscarPorPacienteId(String pacienteId) {
        return Optional.ofNullable(historiasPorPaciente.get(pacienteId));
    }

    @Override
    public boolean existePorPacienteId(String pacienteId) {
        return historiasPorPaciente.containsKey(pacienteId);
    }

    @Override
    public HistoriaClinica guardar(HistoriaClinica historia) {
        historiasPorPaciente.put(historia.getPacienteId(), historia);
        return historia;
    }
}
