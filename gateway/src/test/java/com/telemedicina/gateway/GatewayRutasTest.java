package com.telemedicina.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

/**
 * Verifica la configuración de rutas del Gateway sin levantar el contexto de Spring: cada prefijo
 * de la API debe llegar, por nombre en Eureka (lb://), al microservicio dueño de ese bounded context.
 */
class GatewayRutasTest {

    private static final String RUTAS = "spring.cloud.gateway.server.webflux.routes";

    private static Properties config;

    @BeforeAll
    static void cargarConfiguracion() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        config = yaml.getObject();
    }

    @Test
    void escuchaEnElPuerto8000ComoEntradaUnica() {
        assertThat(config.getProperty("server.port")).isEqualTo("8000");
    }

    @Test
    void declaraUnaRutaPorCadaMicroservicio() {
        assertThat(config.getProperty(RUTAS + "[0].id")).isEqualTo("agenda-route");
        assertThat(config.getProperty(RUTAS + "[1].id")).isEqualTo("triage-alertas-route");
        assertThat(config.getProperty(RUTAS + "[2].id")).isEqualTo("historia-clinica-route");
        assertThat(config.getProperty(RUTAS + "[3].id")).isNull();
    }

    @Test
    void resuelveCadaServicioPorSuNombreEnEureka() {
        assertThat(config.getProperty(RUTAS + "[0].uri")).isEqualTo("lb://agenda-service");
        assertThat(config.getProperty(RUTAS + "[1].uri")).isEqualTo("lb://triage-alertas-service");
        assertThat(config.getProperty(RUTAS + "[2].uri")).isEqualTo("lb://historia-clinica-service");
    }

    @Test
    void enviaCadaPrefijoDeLaApiAlServicioDueno() {
        assertThat(config.getProperty(RUTAS + "[0].predicates[0]"))
                .isEqualTo("Path=/api/citas/**,/api/medicos/**");
        assertThat(config.getProperty(RUTAS + "[1].predicates[0]"))
                .isEqualTo("Path=/api/triage/**,/api/alertas/**");
        assertThat(config.getProperty(RUTAS + "[2].predicates[0]"))
                .isEqualTo("Path=/api/historias-clinicas/**");
    }

    @Test
    void seRegistraEnEurekaConSuIp() {
        assertThat(config.getProperty("spring.application.name")).isEqualTo("gateway");
        assertThat(config.getProperty("eureka.client.service-url.defaultZone"))
                .isEqualTo("http://localhost:8761/eureka");
        assertThat(config.getProperty("eureka.instance.prefer-ip-address")).isEqualTo("true");
    }
}
