package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistoriaClinica;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Test double hecho a mano: cumple el puerto con un Map en memoria, sin Spring ni Mockito. */
class RepositorioHistoriasClinicasFalso implements RepositorioHistoriasClinicas {

    private final Map<String, HistoriaClinica> almacen = new LinkedHashMap<>();

    @Override
    public Optional<HistoriaClinica> buscarPorPacienteId(String pacienteId) {
        return Optional.ofNullable(almacen.get(pacienteId));
    }

    @Override
    public boolean existePorPacienteId(String pacienteId) {
        return almacen.containsKey(pacienteId);
    }

    @Override
    public HistoriaClinica guardar(HistoriaClinica historia) {
        almacen.put(historia.getPacienteId(), historia);
        return historia;
    }
}
