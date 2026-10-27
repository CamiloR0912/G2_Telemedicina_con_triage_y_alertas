package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

import java.util.ArrayList;
import java.util.List;

import com.telemedicina.triagealertasservice.triagealertas.dominio.AlertaClinica;

/** Test double hecho a mano: enfermería simulada que guarda los ids de las alertas que recibió. */
class NotificadorEnfermeriaFalso implements NotificadorEnfermeria {

    final List<String> alertasRecibidas = new ArrayList<>();
    boolean canalCaido;

    @Override
    public void notificar(String cuestionarioId, String pacienteId, String citaId, AlertaClinica alerta) {
        if (canalCaido) {
            throw new IllegalStateException("canal caído");
        }
        alertasRecibidas.add(alerta.getId());
    }
}
