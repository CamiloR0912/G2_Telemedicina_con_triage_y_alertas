package com.telemedicina.triagealertasservice.triagealertas.infraestructura.configuracion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología RabbitMQ de las alertas clínicas. Triage publica en un exchange de tipo topic; cada
 * interesado engancha su propia cola: la de enfermería (declarada aquí, HU-04) y la de Agenda
 * (declarada en agenda-service). Un solo envío llega a las dos.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_ALERTAS = "alertas.exchange";
    public static final String RUTA_URGENCIA_ALTA = "alerta.urgencia.alta";
    public static final String COLA_ENFERMERIA = "enfermeria.alertas.queue";

    @Bean
    TopicExchange alertasExchange() {
        return new TopicExchange(EXCHANGE_ALERTAS);
    }

    /** La consume la estación de enfermería (fuera de este backend); mientras tanto, la alerta espera aquí. */
    @Bean
    Queue enfermeriaAlertasQueue() {
        return new Queue(COLA_ENFERMERIA);
    }

    @Bean
    Binding enfermeriaAlertasBinding(Queue enfermeriaAlertasQueue, TopicExchange alertasExchange) {
        return BindingBuilder.bind(enfermeriaAlertasQueue).to(alertasExchange).with("alerta.#");
    }
}
