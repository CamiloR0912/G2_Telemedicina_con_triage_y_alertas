package com.telemedicina.telemedicina.triagealertas.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RespuestaTriageTest {

    @Test
    void normalizaElSintomaSinTildesNiMayusculas() {
        RespuestaTriage respuesta = new RespuestaTriage("  Pérdida de Conciencia ", 6);

        assertThat(respuesta.sintoma()).isEqualTo("perdida de conciencia");
        assertThat(respuesta.intensidad()).isEqualTo(6);
    }

    @Test
    void rechazaSintomaVacio() {
        assertThatThrownBy(() -> new RespuestaTriage(" ", 3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RespuestaTriage(null, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 11, 100})
    void rechazaIntensidadFueraDeCeroADiez(int intensidad) {
        assertThatThrownBy(() -> new RespuestaTriage("fiebre", intensidad))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
