/**
 * Núcleo del Bounded Context <b>Triage y alertas</b> (Dev 2): modelo de dominio. Solo importa
 * {@code java.*}; no conoce Spring, JPA ni HTTP.
 *
 * <p>Origen: pareja de eventos pivote "Nivel de urgencia fue clasificado" → "Alerta clínica fue
 * generada", que cambia el responsable del paciente/cuestionario a enfermería.</p>
 *
 * <ul>
 *   <li>Raíz del Agregado: {@link com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriage}</li>
 *   <li>Value Objects: {@link com.telemedicina.triagealertasservice.triagealertas.dominio.NivelUrgencia},
 *       {@link com.telemedicina.triagealertasservice.triagealertas.dominio.RespuestaTriage}</li>
 *   <li>Entidad interna: {@link com.telemedicina.triagealertasservice.triagealertas.dominio.AlertaClinica}</li>
 *   <li>Servicio de Dominio: {@link com.telemedicina.triagealertasservice.triagealertas.dominio.ClasificacionTriageService}</li>
 *   <li>Factory: {@link com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriageFactory}</li>
 *   <li>Excepción: {@link com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriageNoEncontradoException}</li>
 * </ul>
 *
 * <p>Paciente y Cita viven en otros subdominios: aquí solo se guardan sus ids.</p>
 */
package com.telemedicina.triagealertasservice.triagealertas.dominio;
