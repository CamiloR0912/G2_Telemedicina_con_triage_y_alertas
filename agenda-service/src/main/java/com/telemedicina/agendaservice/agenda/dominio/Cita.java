package com.telemedicina.agendaservice.agenda.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * Raíz del Agregado del subdominio Agenda.
 *
 * Paciente y Médico pertenecen a otros subdominios: aquí solo se referencian
 * por id (pacienteId, medicoId), nunca por objeto completo. Agenda tampoco
 * conoce Triage ni Alertas, para poder seguir agendando aunque esos módulos
 * estén caídos.
 *
 * El constructor es de paquete: una Cita nueva solo se crea a través de {@link CitaFactory}.
 */
public class Cita {

	private final UUID id;
	private final String pacienteId;
	private final String medicoId;
	private final String especialidad;
	private FranjaHoraria franja;
	private EstadoCita estado;
	/** Estado al que vuelve la cita si la Saga de cancelación se compensa. Solo existe durante EN_CANCELACION. */
	private EstadoCita estadoPrevio;
	private boolean prioritaria;

	Cita(UUID id, String pacienteId, String medicoId, String especialidad, FranjaHoraria franja) {
		this(id, pacienteId, medicoId, especialidad, franja, EstadoCita.AGENDADA, null, false);
	}

	private Cita(UUID id, String pacienteId, String medicoId, String especialidad, FranjaHoraria franja,
			EstadoCita estado, EstadoCita estadoPrevio, boolean prioritaria) {
		this.id = id;
		this.pacienteId = pacienteId;
		this.medicoId = medicoId;
		this.especialidad = especialidad;
		this.franja = franja;
		this.estado = estado;
		this.estadoPrevio = estadoPrevio;
		this.prioritaria = prioritaria;
	}

	/**
	 * Rehidrata una Cita que ya existía (por ejemplo, leída de la base de datos).
	 * No es "agendar": no valida reglas de creación ni genera id nuevo. Una Cita
	 * nueva se sigue creando solo con {@link CitaFactory#agendar}.
	 */
	public static Cita reconstituir(UUID id, String pacienteId, String medicoId, String especialidad,
			FranjaHoraria franja, EstadoCita estado, EstadoCita estadoPrevio, boolean prioritaria) {
		Objects.requireNonNull(id, "El id de la cita es obligatorio");
		Objects.requireNonNull(franja, "La franja horaria es obligatoria");
		Objects.requireNonNull(estado, "El estado de la cita es obligatorio");
		return new Cita(id, pacienteId, medicoId, especialidad, franja, estado, estadoPrevio, prioritaria);
	}

	/**
	 * HU-03: "para que el sistema priorice mi atención si es urgente". Lo dispara la alerta
	 * de urgencia alta que llega desde Triage por RabbitMQ; Agenda nunca llama a Triage.
	 */
	public void priorizar() {
		if (!estaActiva()) {
			throw new IllegalStateException("No se puede priorizar la cita cancelada " + id);
		}
		this.prioritaria = true;
	}

	/**
	 * HU-07 · paso 1 de la Saga de cancelación: la cita queda EN_CANCELACION, todavía ocupando su
	 * cupo, hasta que Triage confirme o rechace. Se recuerda el estado actual por si hay que compensar.
	 */
	public void iniciarCancelacion() {
		if (!estaActiva()) {
			throw new IllegalStateException("La cita " + id + " ya está cancelada");
		}
		if (estado == EstadoCita.EN_CANCELACION) {
			throw new IllegalStateException("La cita " + id + " ya está en proceso de cancelación");
		}
		this.estadoPrevio = estado;
		this.estado = EstadoCita.EN_CANCELACION;
	}

	/** Saga · Triage confirmó: la cita deja de estar activa y su cupo queda libre. */
	public void confirmarCancelacion() {
		exigirEnCancelacion();
		this.estado = EstadoCita.CANCELADA;
		this.estadoPrevio = null;
	}

	/** Saga · compensación: Triage rechazó la cancelación y la cita vuelve a su estado anterior. */
	public void compensarCancelacion() {
		exigirEnCancelacion();
		this.estado = estadoPrevio;
		this.estadoPrevio = null;
	}

	private void exigirEnCancelacion() {
		if (estado != EstadoCita.EN_CANCELACION) {
			throw new IllegalStateException("La cita " + id + " no tiene una cancelación en curso");
		}
	}

	/**
	 * HU-07: mueve la cita a una nueva franja. La verificación de que la nueva
	 * franja esté libre la hace {@link DisponibilidadService#reprogramar}, porque
	 * requiere conocer las demás citas.
	 */
	void reprogramar(FranjaHoraria nuevaFranja) {
		Objects.requireNonNull(nuevaFranja, "La nueva franja horaria es obligatoria");
		if (!estaActiva()) {
			throw new IllegalStateException("No se puede reprogramar una cita cancelada");
		}
		if (estado == EstadoCita.EN_CANCELACION) {
			throw new IllegalStateException("No se puede reprogramar una cita en proceso de cancelación");
		}
		if (nuevaFranja.equals(franja)) {
			throw new IllegalArgumentException("La nueva franja horaria es igual a la actual");
		}
		this.franja = nuevaFranja;
		this.estado = EstadoCita.REPROGRAMADA;
	}

	public boolean estaActiva() {
		return estado.esActiva();
	}

	/** Una cita ocupa cupo en una franja solo si está activa y se solapa con ella. */
	public boolean ocupaCupoEn(FranjaHoraria otraFranja) {
		return estaActiva() && franja.seSolapaCon(otraFranja);
	}

	public UUID getId() {
		return id;
	}

	public String getPacienteId() {
		return pacienteId;
	}

	public String getMedicoId() {
		return medicoId;
	}

	public String getEspecialidad() {
		return especialidad;
	}

	public FranjaHoraria getFranja() {
		return franja;
	}

	public EstadoCita getEstado() {
		return estado;
	}

	public EstadoCita getEstadoPrevio() {
		return estadoPrevio;
	}

	public boolean isPrioritaria() {
		return prioritaria;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Cita otra && id.equals(otra.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
