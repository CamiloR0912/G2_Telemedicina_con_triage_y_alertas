package com.telemedicina.telemedicina.agenda.infraestructura.salida.persistencia;

import java.time.LocalDateTime;
import java.util.UUID;

import com.telemedicina.telemedicina.agenda.dominio.Cita;
import com.telemedicina.telemedicina.agenda.dominio.EstadoCita;
import com.telemedicina.telemedicina.agenda.dominio.FranjaHoraria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Forma de la Cita en la base de datos. Es un detalle del adaptador: el dominio
 * no la conoce, y por eso {@link Cita} no lleva ninguna anotación de JPA.
 */
@Entity
@Table(name = "citas")
class CitaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String pacienteId;

	@Column(nullable = false)
	private String medicoId;

	@Column(nullable = false)
	private String especialidad;

	@Column(nullable = false)
	private LocalDateTime inicio;

	@Column(nullable = false)
	private LocalDateTime fin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoCita estado;

	protected CitaEntity() {
	}

	static CitaEntity desde(Cita cita) {
		var entidad = new CitaEntity();
		entidad.id = cita.getId();
		entidad.pacienteId = cita.getPacienteId();
		entidad.medicoId = cita.getMedicoId();
		entidad.especialidad = cita.getEspecialidad();
		entidad.inicio = cita.getFranja().inicio();
		entidad.fin = cita.getFranja().fin();
		entidad.estado = cita.getEstado();
		return entidad;
	}

	Cita aDominio() {
		return Cita.reconstituir(id, pacienteId, medicoId, especialidad, new FranjaHoraria(inicio, fin), estado);
	}
}
