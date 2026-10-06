package com.telemedicina.telemedicina.agenda.api;

import com.telemedicina.telemedicina.agenda.Cita;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

	private final GestionCitasService gestionCitasService;

	public CitaController(GestionCitasService gestionCitasService) {
		this.gestionCitasService = gestionCitasService;
	}

	/** HU-02 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CitaResponse agendar(@RequestBody AgendarCitaRequest request) {
		return CitaResponse.de(gestionCitasService.agendar(request.pacienteId(), request.medicoId(),
				request.especialidad(), request.inicio(), request.fin()));
	}

	@GetMapping("/{id}")
	public CitaResponse buscarPorId(@PathVariable UUID id) {
		return CitaResponse.de(gestionCitasService.buscarPorId(id));
	}

	/** HU-08 */
	@GetMapping
	public List<CitaResponse> agendaDelDia(@RequestParam String medicoId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return gestionCitasService.agendaDelDia(medicoId, fecha).stream().map(CitaResponse::de).toList();
	}

	@GetMapping("/disponibilidad")
	public DisponibilidadResponse disponibilidad(@RequestParam String medicoId, @RequestParam String pacienteId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
		return new DisponibilidadResponse(gestionCitasService.estaDisponible(medicoId, pacienteId, inicio, fin));
	}

	/** HU-07 */
	@PostMapping("/{id}/cancelar")
	public CitaResponse cancelar(@PathVariable UUID id) {
		return CitaResponse.de(gestionCitasService.cancelar(id));
	}

	/** HU-07 */
	@PostMapping("/{id}/reprogramar")
	public CitaResponse reprogramar(@PathVariable UUID id, @RequestBody ReprogramarCitaRequest request) {
		return CitaResponse.de(gestionCitasService.reprogramar(id, request.inicio(), request.fin()));
	}

	public record AgendarCitaRequest(String pacienteId, String medicoId, String especialidad,
			LocalDateTime inicio, LocalDateTime fin) {
	}

	public record ReprogramarCitaRequest(LocalDateTime inicio, LocalDateTime fin) {
	}

	public record DisponibilidadResponse(boolean disponible) {
	}

	public record CitaResponse(UUID id, String pacienteId, String medicoId, String especialidad,
			LocalDateTime inicio, LocalDateTime fin, String estado, boolean activa) {

		static CitaResponse de(Cita cita) {
			return new CitaResponse(cita.getId(), cita.getPacienteId(), cita.getMedicoId(), cita.getEspecialidad(),
					cita.getFranja().inicio(), cita.getFranja().fin(), cita.getEstado().name(), cita.estaActiva());
		}
	}
}
