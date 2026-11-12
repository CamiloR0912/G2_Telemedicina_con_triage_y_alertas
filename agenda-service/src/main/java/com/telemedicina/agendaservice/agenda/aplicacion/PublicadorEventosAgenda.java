package com.telemedicina.agendaservice.agenda.aplicacion;

import com.telemedicina.agendaservice.agenda.dominio.Cita;

/**
 * Puerto secundario: avisar al resto del sistema que una cita entró en la Saga de cancelación.
 * El núcleo no sabe que detrás hay una tabla outbox y Kafka.
 */
@FunctionalInterface
public interface PublicadorEventosAgenda {

	void citaEnCancelacion(Cita cita);
}
