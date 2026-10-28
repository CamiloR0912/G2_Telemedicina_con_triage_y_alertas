package com.telemedicina.telemedicina.agenda.infraestructura.entrada.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record CitaResponse(UUID id, String pacienteId, String medicoId, String especialidad,
		LocalDateTime inicio, LocalDateTime fin, String estado) {
}
