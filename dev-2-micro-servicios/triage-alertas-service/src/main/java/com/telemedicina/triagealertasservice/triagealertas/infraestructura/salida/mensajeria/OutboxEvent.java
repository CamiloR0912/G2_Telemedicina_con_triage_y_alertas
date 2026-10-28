package com.telemedicina.triagealertasservice.triagealertas.infraestructura.salida.mensajeria;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla outbox: un evento guardado junto al cuestionario, pendiente de enviarse a Kafka. */
@Entity
@Table(name = "outbox_event")
class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topico;

    @Column(nullable = false)
    private String clave;

    @Column(nullable = false, length = 4000)
    private String payload;

    @Column(nullable = false)
    private Instant creadoEn;

    @Column(nullable = false)
    private boolean publicado;

    protected OutboxEvent() {
    }

    OutboxEvent(String topico, String clave, String payload) {
        this.topico = topico;
        this.clave = clave;
        this.payload = payload;
        this.creadoEn = Instant.now();
    }

    Long getId() { return id; }
    String getTopico() { return topico; }
    String getClave() { return clave; }
    String getPayload() { return payload; }

    void marcarPublicado() {
        this.publicado = true;
    }
}
