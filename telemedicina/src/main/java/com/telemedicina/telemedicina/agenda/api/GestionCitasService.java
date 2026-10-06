package com.telemedicina.telemedicina.agenda.api;

import com.telemedicina.telemedicina.agenda.Cita;
import com.telemedicina.telemedicina.agenda.CitaFactory;
import com.telemedicina.telemedicina.agenda.DisponibilidadService;
import com.telemedicina.telemedicina.agenda.FranjaHoraria;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación de Agenda: orquesta el repositorio, la factory y el servicio de dominio.
 * Las reglas de negocio siguen viviendo en el dominio.
 *
 * Los métodos que cambian la agenda son synchronized para que dos peticiones simultáneas
 * no puedan ocupar el mismo cupo.
 */
@Service
public class GestionCitasService {

	private final RepositorioCitasEnMemoria repositorio;
	private final CitaFactory citaFactory = new CitaFactory();
	private final DisponibilidadService disponibilidadService = new DisponibilidadService();

	public GestionCitasService(RepositorioCitasEnMemoria repositorio) {
		this.repositorio = repositorio;
	}

	/** HU-02 */
	public synchronized Cita agendar(String pacienteId, String medicoId, String especialidad,
			LocalDateTime inicio, LocalDateTime fin) {
		Cita cita = citaFactory.agendar(pacienteId, medicoId, especialidad, inicio, fin);
		disponibilidadService.verificarDisponibilidad(
				cita.getMedicoId(), cita.getPacienteId(), cita.getFranja(), repositorio.todas());
		return repositorio.guardar(cita);
	}

	public Cita buscarPorId(UUID id) {
		return repositorio.buscarPorId(id).orElseThrow(() -> new CitaNoEncontradaException(id));
	}

	/** HU-07 */
	public synchronized Cita cancelar(UUID id) {
		Cita cita = buscarPorId(id);
		cita.cancelar();
		return repositorio.guardar(cita);
	}

	/** HU-07 */
	public synchronized Cita reprogramar(UUID id, LocalDateTime inicio, LocalDateTime fin) {
		Cita cita = buscarPorId(id);
		disponibilidadService.reprogramar(cita, new FranjaHoraria(inicio, fin), repositorio.todas());
		return repositorio.guardar(cita);
	}

	/** HU-08 · citas activas del médico en un día, de la más temprana a la más tardía. */
	public List<Cita> agendaDelDia(String medicoId, LocalDate dia) {
		return repositorio.todas().stream()
				.filter(Cita::estaActiva)
				.filter(c -> c.getMedicoId().equals(medicoId))
				.filter(c -> c.getFranja().esDelDia(dia))
				.sorted(Comparator.comparing(c -> c.getFranja().inicio()))
				.toList();
	}

	public boolean estaDisponible(String medicoId, String pacienteId, LocalDateTime inicio, LocalDateTime fin) {
		return disponibilidadService.estaDisponible(
				medicoId, pacienteId, new FranjaHoraria(inicio, fin), repositorio.todas());
	}

	/** Lo que Historia clínica consulta: ¿hay alguna cita no cancelada entre este médico y este paciente? */
	public boolean existeCitaVigente(String medicoId, String pacienteId) {
		return repositorio.todas().stream()
				.anyMatch(c -> c.estaActiva()
						&& c.getMedicoId().equals(medicoId)
						&& c.getPacienteId().equals(pacienteId));
	}
}
