package com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion;

/**
 * Puerto secundario: la copia local (proyección) de las citas cuyo triage ya se completó. Se llena
 * con los eventos {@code triage.completados} que publica Triage por Kafka, así Historia clínica
 * aplica la regla sin llamar a Triage, y sigue funcionando aunque Triage esté caído.
 */
public interface TriagesCompletados {

    void registrar(String citaId);

    boolean estaCompletado(String citaId);
}
