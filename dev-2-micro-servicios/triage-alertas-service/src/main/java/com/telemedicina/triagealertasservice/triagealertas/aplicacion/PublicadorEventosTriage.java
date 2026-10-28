package com.telemedicina.triagealertasservice.triagealertas.aplicacion;

import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriage;

/**
 * Puerto secundario: avisar al resto del sistema que un triage quedó completado (clasificado).
 * Historia clínica lo necesita para la regla "no puede iniciarse una consulta sin un triage
 * completado". El núcleo no sabe que detrás hay una tabla outbox y Kafka.
 */
@FunctionalInterface
public interface PublicadorEventosTriage {

    void triageCompletado(CuestionarioTriage cuestionario);
}
