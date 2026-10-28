package com.telemedicina.triagealertasservice.triagealertas.infraestructura.salida.mensajeria;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Polling Publisher: cada 2 segundos envía a Kafka los eventos pendientes del outbox y los marca
 * como publicados. Si Kafka no responde no marca nada: el evento se reintenta en la siguiente vuelta.
 */
@Component
class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository repositorio;
    private final KafkaTemplate<String, String> kafkaTemplate;

    OutboxPoller(OutboxEventRepository repositorio, KafkaTemplate<String, String> kafkaTemplate) {
        this.repositorio = repositorio;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publicarPendientes() {
        for (OutboxEvent evento : repositorio.findTop50ByPublicadoFalseOrderByIdAsc()) {
            try {
                // .get() espera la confirmación de Kafka: solo se marca "publicado" si Kafka lo recibió
                kafkaTemplate.send(evento.getTopico(), evento.getClave(), evento.getPayload())
                        .get(5, TimeUnit.SECONDS);
                evento.marcarPublicado();
                log.info("Outbox: evento {} publicado en {}", evento.getId(), evento.getTopico());
            } catch (Exception e) {
                log.warn("Outbox: no se pudo publicar el evento {} (se reintentará): {}", evento.getId(), e.getMessage());
                return; // conserva el orden: no seguimos con los siguientes
            }
        }
    }
}
