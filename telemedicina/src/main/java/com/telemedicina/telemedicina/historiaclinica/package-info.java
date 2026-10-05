/**
 * Bounded Context <b>Historia clínica</b> (Dev 3).
 *
 * <p>Origen: evento pivote "Historia clínica del paciente fue consultada por el médico", que a su
 * vez dispara "Acceso a la historia clínica fue registrado".</p>
 *
 * <p>Organizado por rol hexagonal (las dependencias apuntan siempre hacia adentro):</p>
 * <ul>
 *   <li>{@code dominio} (núcleo): raíz del Agregado {@code HistoriaClinica}, entidad interna
 *       {@code RegistroConsulta}, Value Object {@code RegistroAcceso}, Servicio de Dominio
 *       {@code ControlAccesoHistoriaService}, Factory {@code HistoriaClinicaFactory} y el puerto secundario
 *       hacia Agenda {@code HistorialCitas}. No importa Spring, JPA ni HTTP.</li>
 *   <li>{@code aplicacion} (núcleo): puerto primario {@code HistoriaClinicaUseCase}, su implementación
 *       {@code HistoriaClinicaService} y el puerto secundario de persistencia {@code RepositorioHistoriasClinicas}.</li>
 *   <li>{@code infraestructura.entrada.web}: adaptador primario HTTP ({@code HistoriaClinicaController} y DTOs).</li>
 *   <li>{@code infraestructura.salida.persistencia}: adaptador secundario Spring Data JPA.</li>
 *   <li>{@code infraestructura.salida.agenda}: adaptador secundario simulado de {@code HistorialCitas}.</li>
 *   <li>{@code infraestructura.config}: registra como beans las piezas del dominio.</li>
 * </ul>
 *
 * <p>Paciente, Médico, Cita y Consulta viven en otros subdominios: aquí solo se guardan sus ids.</p>
 */
package com.telemedicina.telemedicina.historiaclinica;
