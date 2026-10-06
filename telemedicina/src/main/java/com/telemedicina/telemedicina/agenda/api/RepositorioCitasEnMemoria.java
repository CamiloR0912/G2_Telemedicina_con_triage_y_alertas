package com.telemedicina.telemedicina.agenda.api;

import com.telemedicina.telemedicina.agenda.Cita;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio de Citas en memoria: los datos se pierden al reiniciar la aplicación.
 * Basta para probar la API sin base de datos.
 */
@Repository
public class RepositorioCitasEnMemoria {

	private final Map<UUID, Cita> citas = new ConcurrentHashMap<>();

	public Cita guardar(Cita cita) {
		citas.put(cita.getId(), cita);
		return cita;
	}

	public Optional<Cita> buscarPorId(UUID id) {
		return Optional.ofNullable(citas.get(id));
	}

	public List<Cita> todas() {
		return List.copyOf(citas.values());
	}
}
