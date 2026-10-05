/**
 * Bounded Context <b>Triage y alertas</b> (Dev 2).
 *
 * <p>Origen: pareja de eventos pivote "Nivel de urgencia fue clasificado" → "Alerta clínica fue
 * generada", que cambia el responsable del paciente/cuestionario a enfermería.</p>
 *
 * <ul>
 *   <li>Raíz del Agregado: {@link com.telemedicina.telemedicina.triagealertas.CuestionarioTriage}</li>
 *   <li>Value Objects: {@link com.telemedicina.telemedicina.triagealertas.NivelUrgencia},
 *       {@link com.telemedicina.telemedicina.triagealertas.RespuestaTriage}</li>
 *   <li>Entidad interna: {@link com.telemedicina.telemedicina.triagealertas.AlertaClinica}</li>
 *   <li>Servicio de Dominio: {@link com.telemedicina.telemedicina.triagealertas.ClasificacionTriageService}</li>
 *   <li>Factory: {@link com.telemedicina.telemedicina.triagealertas.CuestionarioTriageFactory}</li>
 * </ul>
 *
 * <p>Paciente y Cita viven en otros subdominios: aquí solo se guardan sus ids.</p>
 */
package com.telemedicina.telemedicina.triagealertas;
