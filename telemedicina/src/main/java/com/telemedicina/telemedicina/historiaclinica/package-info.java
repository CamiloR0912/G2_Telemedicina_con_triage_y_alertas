/**
 * Bounded Context <b>Historia clínica</b> (Dev 3).
 *
 * <p>Origen: evento pivote "Historia clínica del paciente fue consultada por el médico", que a su
 * vez dispara "Acceso a la historia clínica fue registrado".</p>
 *
 * <ul>
 *   <li>Raíz del Agregado: {@link com.telemedicina.telemedicina.historiaclinica.HistoriaClinica}</li>
 *   <li>Value Object: {@link com.telemedicina.telemedicina.historiaclinica.RegistroAcceso}</li>
 *   <li>Servicio de Dominio: {@link com.telemedicina.telemedicina.historiaclinica.ControlAccesoHistoriaService}</li>
 *   <li>Factory: {@link com.telemedicina.telemedicina.historiaclinica.HistoriaClinicaFactory}</li>
 * </ul>
 *
 * <p>Paciente, Médico, Cita y Consulta viven en otros subdominios: aquí solo se guardan sus ids.</p>
 */
package com.telemedicina.telemedicina.historiaclinica;
