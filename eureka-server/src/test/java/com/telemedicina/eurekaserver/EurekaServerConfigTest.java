package com.telemedicina.eurekaserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.core.io.ClassPathResource;

/**
 * Verifica, sin levantar el contexto de Spring, que el registro de servicios está configurado como
 * servidor puro: escucha en el puerto que conocen los clientes y no se registra a sí mismo.
 */
class EurekaServerConfigTest {

    private static Properties config;

    @BeforeAll
    static void cargarConfiguracion() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        config = yaml.getObject();
    }

    @Test
    void escuchaEnElPuertoQueUsanLosClientes() {
        assertThat(config.getProperty("server.port")).isEqualTo("8761");
    }

    @Test
    void noSeRegistraNiSeConsultaASiMismo() {
        assertThat(config.getProperty("eureka.client.register-with-eureka")).isEqualTo("false");
        assertThat(config.getProperty("eureka.client.fetch-registry")).isEqualTo("false");
    }

    @Test
    void refrescaElRegistroRapidoEnDesarrollo() {
        assertThat(Integer.parseInt(config.getProperty("eureka.server.response-cache-update-interval-ms")))
                .isLessThanOrEqualTo(5000);
    }

    @Test
    void laClasePrincipalActivaElServidorEureka() {
        assertThat(EurekaServerApplication.class.isAnnotationPresent(EnableEurekaServer.class)).isTrue();
    }
}
