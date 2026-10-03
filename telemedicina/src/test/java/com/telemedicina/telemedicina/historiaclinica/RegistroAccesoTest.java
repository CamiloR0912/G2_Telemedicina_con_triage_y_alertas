package com.telemedicina.telemedicina.historiaclinica;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroAccesoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 3, 9, 30);

    @Test
    void creaRegistroConMedicoYFechaHora() {
        RegistroAcceso acceso = new RegistroAcceso(" med-1 ", AHORA);

        assertThat(acceso.medicoId()).isEqualTo("med-1");
        assertThat(acceso.fechaHora()).isEqualTo(AHORA);
    }

    @Test
    void rechazaMedicoVacio() {
        assertThatThrownBy(() -> new RegistroAcceso("  ", AHORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegistroAcceso(null, AHORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaFechaHoraNula() {
        assertThatThrownBy(() -> new RegistroAcceso("med-1", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dosRegistrosConLosMismosValoresSonIguales() {
        assertThat(new RegistroAcceso("med-1", AHORA)).isEqualTo(new RegistroAcceso("med-1", AHORA));
    }
}
