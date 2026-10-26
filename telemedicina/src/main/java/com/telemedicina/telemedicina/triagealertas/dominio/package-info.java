/**
 * Núcleo del Bounded Context <b>Triage y alertas</b> (Dev 2): modelo de dominio. Solo importa
 * {@code java.*}; no conoce Spring, JPA ni HTTP.
 *
 * <p>Origen: pareja de eventos pivote "Nivel de urgencia fue clasificado" → "Alerta clínica fue
 * generada", que cambia el responsable del paciente/cuestionario a enfermería.</p>
 *
 * <ul>
 *   <li>Raíz del Agregado: {@link com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriage}</li>
 *   <li>Value Objects: {@link com.telemedicina.telemedicina.triagealertas.dominio.NivelUrgencia},
 *       {@link com.telemedicina.telemedicina.triagealertas.dominio.RespuestaTriage}</li>
 *   <li>Entidad interna: {@link com.telemedicina.telemedicina.triagealertas.dominio.AlertaClinica}</li>
 *   <li>Servicio de Dominio: {@link com.telemedicina.telemedicina.triagealertas.dominio.ClasificacionTriageService}</li>
 *   <li>Factory: {@link com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriageFactory}</li>
 *   <li>Excepción: {@link com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriageNoEncontradoException}</li>
 * </ul>
 *
 * <p>Paciente y Cita viven en otros subdominios: aquí solo se guardan sus ids.</p>
 */
package com.telemedicina.telemedicina.triagealertas.dominio;
