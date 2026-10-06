package com.telemedicina.telemedicina.agenda.dominio;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Value Object: intervalo de tiempo que ocupa una Cita en la agenda.
 * Se valida al construirse — no puede existir una franja sin inicio, sin fin,
 * o con el inicio igual o posterior al fin.
 */
public record FranjaHoraria(LocalDateTime inicio, LocalDateTime fin) {

	public FranjaHoraria {
		if (inicio == null || fin == null) {
			throw new IllegalArgumentException("La franja horaria requiere inicio y fin");
		}
		if (!inicio.isBefore(fin)) {
			throw new IllegalArgumentException("El inicio de la franja horaria debe ser anterior al fin");
		}
	}

	/** Dos franjas se solapan si comparten algún instante; tocarse en el borde (10:00-10:30 y 10:30-11:00) no cuenta. */
	public boolean seSolapaCon(FranjaHoraria otra) {
		return inicio.isBefore(otra.fin) && otra.inicio.isBefore(fin);
	}

	public boolean esDelDia(LocalDate dia) {
		return inicio.toLocalDate().equals(dia);
	}

	public Duration duracion() {
		return Duration.between(inicio, fin);
	}
}
