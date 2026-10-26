package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

import java.util.HashSet;
import java.util.Set;

/** Test double hecho a mano: la proyección de triages completados, en memoria. */
class TriagesCompletadosFalso implements TriagesCompletados {

    private final Set<String> citas = new HashSet<>();

    @Override
    public void registrar(String citaId) {
        citas.add(citaId);
    }

    @Override
    public boolean estaCompletado(String citaId) {
        return citas.contains(citaId);
    }
}
