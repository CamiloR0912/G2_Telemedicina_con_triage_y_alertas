package com.telemedicina.telemedicina.historiaclinica.api;

import com.telemedicina.telemedicina.agenda.api.GestionCitasService;
import com.telemedicina.telemedicina.historiaclinica.HistorialCitas;
import org.springframework.stereotype.Component;

/**
 * Implementa {@link HistorialCitas} preguntándole a Agenda solo por ids. Así Historia clínica
 * nunca conoce la entidad Cita: si Agenda cambia por dentro, este es el único punto a ajustar.
 */
@Component
public class HistorialCitasAgendaAdapter implements HistorialCitas {

    private final GestionCitasService gestionCitasService;

    public HistorialCitasAgendaAdapter(GestionCitasService gestionCitasService) {
        this.gestionCitasService = gestionCitasService;
    }

    @Override
    public boolean existeCitaActivaOHistorica(String medicoId, String pacienteId) {
        return gestionCitasService.existeCitaVigente(medicoId, pacienteId);
    }
}
