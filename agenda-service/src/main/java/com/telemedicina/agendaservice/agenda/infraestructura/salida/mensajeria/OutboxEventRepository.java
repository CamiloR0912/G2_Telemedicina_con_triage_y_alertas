package com.telemedicina.agendaservice.agenda.infraestructura.salida.mensajeria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	List<OutboxEvent> findTop50ByPublicadoFalseOrderByIdAsc();
}
