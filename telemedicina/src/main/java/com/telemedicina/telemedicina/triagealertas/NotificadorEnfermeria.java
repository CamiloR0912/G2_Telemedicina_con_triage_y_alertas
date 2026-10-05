package com.telemedicina.telemedicina.triagealertas;

/**
 * Lo único que Triage y alertas necesita del mundo exterior para cumplir HU-04: avisar en tiempo
 * real al personal de enfermería. El dominio no sabe si es WebSocket, correo o SMS; eso lo
 * resolverá un adaptador que implemente esta interfaz.
 */
@FunctionalInterface
public interface NotificadorEnfermeria {

    void notificar(String cuestionarioId, String pacienteId, AlertaClinica alerta);
}
