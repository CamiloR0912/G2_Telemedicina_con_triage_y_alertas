package com.telemedicina.agendaservice.agenda.infraestructura.entrada.grpc;

import org.springframework.grpc.server.service.GrpcService;

import com.telemedicina.agendaservice.agenda.aplicacion.CitaUseCase;
import com.telemedicina.agendaservice.grpc.AgendaGrpcGrpc;
import com.telemedicina.agendaservice.grpc.CitaEntreRequest;
import com.telemedicina.agendaservice.grpc.CitaEntreResponse;

import io.grpc.stub.StreamObserver;

/**
 * Adaptador primario gRPC, hermano de {@code CitaController}: traduce la llamada de Historia
 * clínica al puerto {@link CitaUseCase}. Escucha en el puerto 9090 (spring.grpc.server.port).
 */
@GrpcService
public class AgendaGrpcService extends AgendaGrpcGrpc.AgendaGrpcImplBase {

	private final CitaUseCase citaUseCase;

	public AgendaGrpcService(CitaUseCase citaUseCase) {
		this.citaUseCase = citaUseCase;
	}

	@Override
	public void existeCitaEntre(CitaEntreRequest request, StreamObserver<CitaEntreResponse> responseObserver) {
		boolean existe = citaUseCase.existeCitaEntre(request.getMedicoId(), request.getPacienteId());
		responseObserver.onNext(CitaEntreResponse.newBuilder().setExiste(existe).build());
		responseObserver.onCompleted();
	}
}
