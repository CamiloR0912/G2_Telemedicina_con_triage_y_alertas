package com.telemedicina.telemedicina.agenda.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.telemedicina.telemedicina.agenda.dominio.Cita;
import com.telemedicina.telemedicina.agenda.dominio.CitaFactory;
import com.telemedicina.telemedicina.agenda.dominio.CitaNoEncontradaException;
import com.telemedicina.telemedicina.agenda.dominio.DisponibilidadService;
import com.telemedicina.telemedicina.agenda.dominio.FranjaHoraria;

/**
 * Caso de uso: orquesta el dominio (Factory, Servicio de Dominio, Cita) y el
 * puerto secundario. Las reglas viven en el dominio; aquí solo se coordinan.
 */
@Service
public class CitaService implements CitaUseCase {

	private final RepositorioCitas repositorioCitas;
	private final CitaFactory citaFactory;
	private final DisponibilidadService disponibilidadService;

	public CitaService(RepositorioCitas repositorioCitas, CitaFactory citaFactory,
			DisponibilidadService disponibilidadService) {
		this.repositorioCitas = repositorioCitas;
		this.citaFactory = citaFactory;
		this.disponibilidadService = disponibilidadService;
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
	public Cita cancelar(UUID id) {
		var cita = buscarPorId(id);
		cita.cancelar();
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
}
