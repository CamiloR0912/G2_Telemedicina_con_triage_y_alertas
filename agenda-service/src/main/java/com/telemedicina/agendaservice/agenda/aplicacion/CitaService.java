package com.telemedicina.agendaservice.agenda.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.telemedicina.agendaservice.agenda.dominio.Cita;
import com.telemedicina.agendaservice.agenda.dominio.CitaFactory;
import com.telemedicina.agendaservice.agenda.dominio.CitaNoEncontradaException;
import com.telemedicina.agendaservice.agenda.dominio.DisponibilidadService;
import com.telemedicina.agendaservice.agenda.dominio.FranjaHoraria;

import jakarta.transaction.Transactional;

/**
 * Caso de uso: orquesta el dominio (Factory, Servicio de Dominio, Cita) y el
 * puerto secundario. Las reglas viven en el dominio; aquí solo se coordinan.
 */
@Service
public class CitaService implements CitaUseCase {

	private final RepositorioCitas repositorioCitas;
	private final CitaFactory citaFactory;
	private final DisponibilidadService disponibilidadService;
	private final PublicadorEventosAgenda publicadorEventos;

	public CitaService(RepositorioCitas repositorioCitas, CitaFactory citaFactory,
			DisponibilidadService disponibilidadService, PublicadorEventosAgenda publicadorEventos) {
		this.repositorioCitas = repositorioCitas;
		this.citaFactory = citaFactory;
		this.disponibilidadService = disponibilidadService;
		this.publicadorEventos = publicadorEventos;
	}

	@Override
	public Cita agendar(String pacienteId, String medicoId, String especialidad,
			LocalDateTime inicio, LocalDateTime fin) {
		var cita = citaFactory.agendar(pacienteId, medicoId, especialidad, inicio, fin);
		var existentes = repositorioCitas.buscarPorMedicoOPaciente(cita.getMedicoId(), cita.getPacienteId());
		disponibilidadService.verificarDisponibilidad(cita.getMedicoId(), cita.getPacienteId(), cita.getFranja(),
				existentes);
		return repositorioCitas.guardar(cita);
	}

	@Override
	public Cita buscarPorId(UUID id) {
		return repositorioCitas.buscarPorId(id).orElseThrow(() -> new CitaNoEncontradaException(id));
	}

	@Override
	@Transactional
	public Cita cancelar(UUID id) {
		var cita = buscarPorId(id);
		cita.iniciarCancelacion();
		var guardada = repositorioCitas.guardar(cita);
		// Transactional Outbox: el cambio de estado y el evento se confirman juntos, o ninguno.
		publicadorEventos.citaEnCancelacion(guardada);
		return guardada;
	}

	@Override
	public Cita confirmarCancelacion(UUID id) {
		var cita = buscarPorId(id);
		cita.confirmarCancelacion();
		return repositorioCitas.guardar(cita);
	}

	@Override
	public Cita compensarCancelacion(UUID id) {
		var cita = buscarPorId(id);
		cita.compensarCancelacion();
		return repositorioCitas.guardar(cita);
	}

	@Override
	public Cita reprogramar(UUID id, LocalDateTime inicio, LocalDateTime fin) {
		var cita = buscarPorId(id);
		var existentes = repositorioCitas.buscarPorMedicoOPaciente(cita.getMedicoId(), cita.getPacienteId());
		disponibilidadService.reprogramar(cita, new FranjaHoraria(inicio, fin), existentes);
		return repositorioCitas.guardar(cita);
	}

	@Override
	public List<Cita> agendaDelDia(String medicoId, LocalDate dia) {
		return repositorioCitas.buscarPorMedico(medicoId).stream()
				.filter(Cita::estaActiva)
				.filter(cita -> cita.getFranja().esDelDia(dia))
				.sorted(Comparator.comparing(cita -> cita.getFranja().inicio()))
				.toList();
	}

	@Override
	public Cita priorizar(UUID id) {
		var cita = buscarPorId(id);
		cita.priorizar();
		return repositorioCitas.guardar(cita);
	}

	@Override
	public boolean existeCitaEntre(String medicoId, String pacienteId) {
		return repositorioCitas.buscarPorMedico(medicoId).stream()
				.anyMatch(cita -> cita.getPacienteId().equals(pacienteId) && cita.estaActiva());
	}
}
