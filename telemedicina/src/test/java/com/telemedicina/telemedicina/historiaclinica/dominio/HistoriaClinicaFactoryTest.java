package com.telemedicina.telemedicina.historiaclinica.dominio;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HistoriaClinicaFactoryTest {

    private final HistoriaClinicaFactory factory = new HistoriaClinicaFactory();

    @Test
    void creaHistoriaClinicaBasicaNormalizada() {
        HistoriaClinica historia = factory.crear(" pac-1 ", "ab+",
                Arrays.asList("Penicilina", " ", null, "Penicilina", " Látex "), "  Hipertensión ");

        assertThat(historia.getId()).isNotBlank();
        assertThat(historia.getPacienteId()).isEqualTo("pac-1");
        assertThat(historia.getGrupoSanguineo()).isEqualTo("AB+");
        assertThat(historia.getAlergias()).containsExactly("Penicilina", "Látex");
        assertThat(historia.getAntecedentes()).isEqualTo("Hipertensión");
        assertThat(historia.getRegistrosAcceso()).isEmpty();
        assertThat(historia.getRegistrosConsulta()).isEmpty();
    }

    @Test
    void cadaHistoriaTieneSuPropioId() {
        assertThat(factory.crear("pac-1", "O+", null, null).getId())
                .isNotEqualTo(factory.crear("pac-2", "O+", null, null).getId());
    }

    @Test
    void rechazaPacienteVacio() {
        assertThatThrownBy(() -> factory.crear(" ", "O+", List.of(), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaGrupoSanguineoInvalido() {
        assertThatThrownBy(() -> factory.crear("pac-1", "C+", List.of(), ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.crear("pac-1", null, List.of(), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
