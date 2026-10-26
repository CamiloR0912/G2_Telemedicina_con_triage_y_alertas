package com.telemedicina.telemedicina.triagealertas.infraestructura.configuracion;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.telemedicina.telemedicina.triagealertas.aplicacion.CuestionarioTriageService;
import com.telemedicina.telemedicina.triagealertas.aplicacion.NotificadorEnfermeria;
import com.telemedicina.telemedicina.triagealertas.aplicacion.RepositorioCuestionariosTriage;
import com.telemedicina.telemedicina.triagealertas.dominio.ClasificacionTriageService;
import com.telemedicina.telemedicina.triagealertas.dominio.CuestionarioTriageFactory;

/**
 * Registra en Spring el núcleo de Triage y alertas. Así los paquetes dominio y aplicacion no
 * importan nada de Spring: se pueden usar con {@code new} en un test sin contenedor.
 *
 * <p>El reloj no se publica como bean para no chocar con el de otros subdominios.</p>
 */
@Configuration
public class TriageAlertasConfig {

    private static final Clock RELOJ = Clock.systemDefaultZone();

    @Bean
    CuestionarioTriageFactory cuestionarioTriageFactory() {
        return new CuestionarioTriageFactory(RELOJ);
    }

    @Bean
    ClasificacionTriageService clasificacionTriageService() {
        return new ClasificacionTriageService(RELOJ);
    }

    @Bean
    CuestionarioTriageService cuestionarioTriageService(RepositorioCuestionariosTriage repositorioCuestionarios,
                                                        NotificadorEnfermeria notificadorEnfermeria,
                                                        CuestionarioTriageFactory cuestionarioTriageFactory,
                                                        ClasificacionTriageService clasificacionTriageService) {
        return new CuestionarioTriageService(repositorioCuestionarios, notificadorEnfermeria,
                cuestionarioTriageFactory, clasificacionTriageService, RELOJ);
    }
}
