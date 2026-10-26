package com.telemedicina.telemedicina.historiaclinica.infraestructura.config;

import com.telemedicina.telemedicina.historiaclinica.dominio.ControlAccesoHistoriaService;
import com.telemedicina.telemedicina.historiaclinica.dominio.HistoriaClinicaFactory;
import com.telemedicina.telemedicina.historiaclinica.dominio.HistorialCitas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Registra como beans las piezas del dominio, que no llevan anotaciones de Spring
 * para que el núcleo no dependa del framework.
 */
@Configuration
public class HistoriaClinicaConfig {

    @Bean
    HistoriaClinicaFactory historiaClinicaFactory() {
        return new HistoriaClinicaFactory();
    }

    @Bean
    ControlAccesoHistoriaService controlAccesoHistoriaService(HistorialCitas historialCitas) {
        return new ControlAccesoHistoriaService(historialCitas, Clock.systemDefaultZone());
    }
}
