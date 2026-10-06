package com.telemedicina.telemedicina.historiaclinica.api;

import com.telemedicina.telemedicina.historiaclinica.HistoriaClinica;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Repositorio de historias clínicas en memoria, indexado por paciente (una historia por paciente). */
@Repository
public class RepositorioHistoriasClinicasEnMemoria {

    private final Map<String, HistoriaClinica> historiasPorPaciente = new ConcurrentHashMap<>();

    public HistoriaClinica guardar(HistoriaClinica historia) {
        historiasPorPaciente.put(historia.getPacienteId(), historia);
        return historia;
    }

    public Optional<HistoriaClinica> buscarPorPacienteId(String pacienteId) {
        return Optional.ofNullable(historiasPorPaciente.get(pacienteId));
    }

    public boolean existePorPacienteId(String pacienteId) {
        return historiasPorPaciente.containsKey(pacienteId);
    }
}
