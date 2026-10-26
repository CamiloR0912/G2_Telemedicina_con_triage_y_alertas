package com.telemedicina.historiaclinicaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.client.ImportGrpcClients;

import com.telemedicina.historiaclinicaservice.grpc.AgendaGrpcGrpc.AgendaGrpcBlockingStub;

/** Registra el stub gRPC hacia agenda-service sobre el canal "agenda" de application.yml. */
@SpringBootApplication
@ImportGrpcClients(target = "agenda", types = AgendaGrpcBlockingStub.class)
public class HistoriaClinicaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HistoriaClinicaServiceApplication.class, args);
	}

}
