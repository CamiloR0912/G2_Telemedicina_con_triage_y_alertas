package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriage;

import jakarta.transaction.Transactional;

/**
 * Caso de uso de la Saga de cancelación. No lleva {@code @Service}: se registra en
 * {@code TriageAlertasConfig}, igual que {@link CuestionarioTriageService}.
 */
public class CancelacionCitaService implements CancelacionCitaUseCase {

    static final String MOTIVO_ALERTA_PENDIENTE =
            "La cita tiene una alerta clínica de urgencia alta sin atender por enfermería";

    private final RepositorioCuestionariosTriage repositorioCuestionarios;
    private final PublicadorRespuestaCancelacion publicador;

    public CancelacionCitaService(RepositorioCuestionariosTriage repositorioCuestionarios,
                                  PublicadorRespuestaCancelacion publicador) {
        this.repositorioCuestionarios = repositorioCuestionarios;
        this.publicador = publicador;
    }

    @Override
    @Transactional
    public boolean evaluarCancelacion(String citaId) {
        boolean alertaPendiente = repositorioCuestionarios.buscarPorCitaId(citaId).stream()
                .anyMatch(CuestionarioTriage::tieneAlertaActiva);
        if (alertaPendiente) {
            publicador.cancelacionRechazada(citaId, MOTIVO_ALERTA_PENDIENTE);
            return false;
        }
        publicador.cancelacionConfirmada(citaId);
        return true;
    }
}
