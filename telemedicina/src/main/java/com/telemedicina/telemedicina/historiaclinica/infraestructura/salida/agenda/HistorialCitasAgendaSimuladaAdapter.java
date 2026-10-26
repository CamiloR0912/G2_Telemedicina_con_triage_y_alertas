package com.telemedicina.telemedicina.historiaclinica.infraestructura.salida.agenda;

import com.telemedicina.telemedicina.historiaclinica.dominio.HistorialCitas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador secundario simulado del puerto {@link HistorialCitas}: mientras el subdominio Agenda (Dev 1)
 * no exponga sus citas, las parejas médico:paciente con cita se leen de configuración
 * ({@code historia-clinica.agenda-simulada.citas}). Cambiarlo por la integración real no toca el núcleo.
 */
@Component
public class HistorialCitasAgendaSimuladaAdapter implements HistorialCitas {

    private static final Logger log = LoggerFactory.getLogger(HistorialCitasAgendaSimuladaAdapter.class);

    private final Set<String> citas;

    public HistorialCitasAgendaSimuladaAdapter(
            @Value("${historia-clinica.agenda-simulada.citas:}") List<String> citas) {
        this.citas = citas.stream()
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean existeCitaActivaOHistorica(String medicoId, String pacienteId) {
        boolean existe = citas.contains(medicoId + ":" + pacienteId);
        log.info("Agenda simulada: cita entre {} y {} = {}", medicoId, pacienteId, existe);
        return existe;
    }
}
