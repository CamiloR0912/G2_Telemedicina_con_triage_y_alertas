package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.salida.agenda;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.telemedicina.historiaclinicaservice.grpc.AgendaGrpcGrpc.AgendaGrpcBlockingStub;
import com.telemedicina.historiaclinicaservice.grpc.CitaEntreRequest;
import com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion.ServicioNoDisponibleException;
import com.telemedicina.historiaclinicaservice.historiaclinica.dominio.HistorialCitas;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.grpc.StatusRuntimeException;

/**
 * Adaptador secundario real del puerto {@link HistorialCitas} (reemplaza al
 * {@code HistorialCitasAgendaSimuladaAdapter} de la versión hexagonal): le pregunta a agenda-service
 * por gRPC. Es síncrono a propósito: dar acceso a una historia clínica no puede esperar a que llegue
 * un evento. El Circuit Breaker evita colgarse si Agenda está lenta o caída.
 */
@Component
class HistorialCitasGrpcAdapter implements HistorialCitas {

    private static final Logger log = LoggerFactory.getLogger(HistorialCitasGrpcAdapter.class);

    private final AgendaGrpcBlockingStub stub;
    private final CircuitBreaker circuitBreaker;

    HistorialCitasGrpcAdapter(AgendaGrpcBlockingStub stub) {
        this.stub = stub;
        this.circuitBreaker = CircuitBreaker.of("agenda-service",
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .build());
        this.circuitBreaker.getEventPublisher().onStateTransition(
                e -> log.warn("Circuit Breaker agenda-service: {}", e.getStateTransition()));
    }

    @Override
    public boolean existeCitaActivaOHistorica(String medicoId, String pacienteId) {
        try {
            return circuitBreaker.executeSupplier(() -> stub.withDeadlineAfter(5, TimeUnit.SECONDS)
                    .existeCitaEntre(CitaEntreRequest.newBuilder()
                            .setMedicoId(medicoId)
                            .setPacienteId(pacienteId)
                            .build())
                    .getExiste());
        } catch (CallNotPermittedException e) {
            throw new ServicioNoDisponibleException("agenda-service no está disponible (circuito abierto)", e);
        } catch (StatusRuntimeException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo contactar a agenda-service: " + e.getStatus().getCode(), e);
        }
    }
}
