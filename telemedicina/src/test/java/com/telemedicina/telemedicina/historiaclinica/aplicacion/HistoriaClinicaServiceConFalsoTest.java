package com.telemedicina.telemedicina.historiaclinica.aplicacion;

import com.telemedicina.telemedicina.historiaclinica.dominio.AccesoHistoriaDenegadoException;
import com.telemedicina.telemedicina.historiaclinica.dominio.ControlAccesoHistoriaService;
import com.telemedicina.telemedicina.historiaclinica.dominio.HistoriaClinica;
import com.telemedicina.telemedicina.historiaclinica.dominio.HistoriaClinicaFactory;
import com.telemedicina.telemedicina.historiaclinica.dominio.HistorialCitas;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Caso de uso de punta a punta sin @SpringBootTest, sin @Mock y sin base de datos. */
class HistoriaClinicaServiceConFalsoTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-04T14:00:00Z"), ZoneId.of("America/Bogota"));

    @Test
    void registraConsultaYAuditaElAccesoSinNingunaDependenciaDeSpringNiDeBaseDeDatos() {
        RepositorioHistoriasClinicasFalso repositorio = new RepositorioHistoriasClinicasFalso();
        HistorialCitas agenda = (medicoId, pacienteId) -> medicoId.equals("med-1") && pacienteId.equals("pac-1");
        HistoriaClinicaService service = new HistoriaClinicaService(repositorio, new HistoriaClinicaFactory(),
                new ControlAccesoHistoriaService(agenda, RELOJ));

        service.registrar("pac-1", "O+", List.of("Penicilina"), "Asma");
        service.registrarConsulta("pac-1", "med-1", "cons-1", "Faringitis", "Reposo", "Ibuprofeno 400 mg");
        service.consultar("pac-1", "med-1");

        HistoriaClinica guardada = repositorio.buscarPorPacienteId("pac-1").orElseThrow();
        assertThat(guardada.getRegistrosConsulta()).extracting("diagnostico").containsExactly("Faringitis");
        assertThat(guardada.getRegistrosAcceso()).extracting("medicoId").containsExactly("med-1");

        assertThatThrownBy(() -> service.consultar("pac-1", "med-2"))
                .isInstanceOf(AccesoHistoriaDenegadoException.class);
        assertThat(guardada.getRegistrosAcceso()).hasSize(1);
    }
}
