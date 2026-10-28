package com.telemedicina.agendaservice.agenda.infraestructura.configuracion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agenda declara SU cola y la engancha al exchange que publica Triage. Declarar el exchange
 * aquí también es idempotente: así Agenda arranca aunque Triage nunca haya arrancado.
 */
@Configuration
public class RabbitConfig {

	public static final String EXCHANGE_ALERTAS = "alertas.exchange";
	public static final String RUTA_URGENCIA_ALTA = "alerta.urgencia.alta";
	public static final String COLA_AGENDA = "agenda.alertas-urgentes.queue";

	@Bean
	TopicExchange alertasExchange() {
		return new TopicExchange(EXCHANGE_ALERTAS);
	}

	@Bean
	Queue agendaAlertasQueue() {
		return new Queue(COLA_AGENDA);
	}

	@Bean
	Binding agendaAlertasBinding(Queue agendaAlertasQueue, TopicExchange alertasExchange) {
		return BindingBuilder.bind(agendaAlertasQueue).to(alertasExchange).with(RUTA_URGENCIA_ALTA);
	}
}
