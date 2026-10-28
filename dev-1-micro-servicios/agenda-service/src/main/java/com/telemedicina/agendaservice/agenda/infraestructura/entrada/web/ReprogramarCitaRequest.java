package com.telemedicina.agendaservice.agenda.infraestructura.entrada.web;

import java.time.LocalDateTime;

public record ReprogramarCitaRequest(LocalDateTime inicio, LocalDateTime fin) {
}
