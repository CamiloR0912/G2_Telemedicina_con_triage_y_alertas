package com.telemedicina.telemedicina.triagealertas.dominio;

import  org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NivelUrgenciaTest {

    @Test
    void aceptaLosTresNivelesYNormalizaMayusculasYEspacios() {
        assertThat(new NivelUrgencia(" ALTO ")).isEqualTo(NivelUrgencia.ALTO);
        assertThat(new NivelUrgencia("Medio")).isEqualTo(NivelUrgencia.MEDIO);
        assertThat(new NivelUrgencia("bajo")).isEqualTo(NivelUrgencia.BAJO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "critico", "urgente", "muy alto"})
    void rechazaValoresFueraDeBajoMedioAlto(String valor) {
        assertThatThrownBy(() -> new NivelUrgencia(valor))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void soloElNivelAltoEsAlto() {
        assertThat(NivelUrgencia.ALTO.esAlto()).isTrue();
        assertThat(NivelUrgencia.MEDIO.esAlto()).isFalse();
        assertThat(NivelUrgencia.BAJO.esAlto()).isFalse();
    }

    @Test
    void laPrioridadOrdenaDeBajoAAlto() {
        assertThat(NivelUrgencia.BAJO.prioridad()).isLessThan(NivelUrgencia.MEDIO.prioridad());
        assertThat(NivelUrgencia.MEDIO.prioridad()).isLessThan(NivelUrgencia.ALTO.prioridad());
    }
}
