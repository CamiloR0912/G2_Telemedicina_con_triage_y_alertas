package com.telemedicina.telemedicina.triagealertas.aplicacion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage;

/** Test double hecho a mano: cumple el puerto guardando los cuestionarios en un Map. */
class RepositorioCuestionariosTriageFalso implements RepositorioCuestionariosTriage {

    private final Map<String, CuestionarioTriage> almacen = new LinkedHashMap<>();

    @Override
    public CuestionarioTriage guardar(CuestionarioTriage cuestionario) {
        almacen.put(cuestionario.getId(), cuestionario);
        return cuestionario;
    }

    @Override
    public Optional<CuestionarioTriage> buscarPorId(String id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<CuestionarioTriage> buscarPorCitaId(String citaId) {
        return almacen.values().stream()
                .filter(cuestionario -> cuestionario.getCitaId().equals(citaId))
                .toList();
    }

    @Override
    public List<CuestionarioTriage> buscarConAlertaActiva() {
        return almacen.values().stream()
                .filter(CuestionarioTriage::tieneAlertaActiva)
                .toList();
    }
}
