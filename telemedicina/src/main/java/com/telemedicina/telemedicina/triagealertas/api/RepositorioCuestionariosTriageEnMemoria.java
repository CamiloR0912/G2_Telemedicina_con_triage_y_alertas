package com.telemedicina.telemedicina.triagealertas.api;

import com.telemedicina.telemedicina.triagealertas.CuestionarioTriage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Repositorio de cuestionarios de triage en memoria. */
@Repository
public class RepositorioCuestionariosTriageEnMemoria {

    private final Map<String, CuestionarioTriage> cuestionarios = new ConcurrentHashMap<>();

    public CuestionarioTriage guardar(CuestionarioTriage cuestionario) {
        cuestionarios.put(cuestionario.getId(), cuestionario);
        return cuestionario;
    }

    public Optional<CuestionarioTriage> buscarPorId(String id) {
        return Optional.ofNullable(cuestionarios.get(id));
    }

    public List<CuestionarioTriage> todos() {
        return List.copyOf(cuestionarios.values());
    }
}
