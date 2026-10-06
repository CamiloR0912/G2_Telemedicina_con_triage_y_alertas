package com.telemedicina.telemedicina.agenda.infraestructura.entrada.web;

import java.time.LocalDateTime;

public record AgendarCitaRequest(String pacienteId, String medicoId, String especialidad,
		LocalDateTime inicio, LocalDateTime fin) {
}
