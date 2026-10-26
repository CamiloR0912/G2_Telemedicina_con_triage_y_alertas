package com.telemedicina.telemedicina.agenda.infraestructura.entrada.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

import com.telemedicina.telemedicina.agenda.aplicacion.CitaUseCase;

/**
 * Adaptador primario: traduce HTTP a llamadas del puerto {@link CitaUseCase}.
 * Depende del puerto, nunca de la clase concreta CitaService.
 */
@RestController
@RequestMapping("/api")
public class CitaController {

	private final CitaUseCase citaUseCase;

	public CitaController(CitaUseCase citaUseCase) {
		this.citaUseCase = citaUseCase;
	}

	/** HU-02 */
	@PostMapping("/citas")
	@ResponseStatus(HttpStatus.CREATED)
	public CitaResponse agendar(@RequestBody AgendarCitaRequest request) {
		var cita = citaUseCase.agendar(request.pacienteId(), request.medicoId(), request.especialidad(),
				request.inicio(), request.fin());
		return CitaMapper.aResponse(cita);
	}

	@GetMapping("/citas/{id}")
	public CitaResponse buscarPorId(@PathVariable UUID id) {
		return CitaMapper.aResponse(citaUseCase.buscarPorId(id));
	}

	/** HU-07 */
	@PostMapping("/citas/{id}/cancelar")
	public CitaResponse cancelar(@PathVariable UUID id) {
		return CitaMapper.aResponse(citaUseCase.cancelar(id));
	}

	/** HU-07 */
	@PostMapping("/citas/{id}/reprogramar")
	public CitaResponse reprogramar(@PathVariable UUID id, @RequestBody ReprogramarCitaRequest request) {
		return CitaMapper.aResponse(citaUseCase.reprogramar(id, request.inicio(), request.fin()));
	}

	/** HU-08 */
	@GetMapping("/medicos/{medicoId}/agenda")
	public List<CitaResponse> agendaDelDia(@PathVariable String medicoId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {
		return citaUseCase.agendaDelDia(medicoId, dia).stream()
				.map(CitaMapper::aResponse)
				.toList();
	}
}
