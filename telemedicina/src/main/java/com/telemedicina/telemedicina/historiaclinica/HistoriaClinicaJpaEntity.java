package com.telemedicina.telemedicina.historiaclinica;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de persistencia de la historia clínica. Vive solo en el adaptador JPA: el dominio
 * ({@link HistoriaClinica}) no conoce ninguna anotación de JPA.
 */
@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinicaJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String pacienteId;

    private String grupoSanguineo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "historia_clinica_alergias")
    @OrderColumn
    private List<String> alergias = new ArrayList<>();

    @Column(length = 2000)
    private String antecedentes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "historia_clinica_registros_consulta")
    @OrderColumn
    private List<RegistroConsultaJpa> registrosConsulta = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "historia_clinica_registros_acceso")
    @OrderColumn
    private List<RegistroAccesoJpa> registrosAcceso = new ArrayList<>();

    protected HistoriaClinicaJpaEntity() {
    }

    static HistoriaClinicaJpaEntity desdeDominio(HistoriaClinica historia) {
        HistoriaClinicaJpaEntity entidad = new HistoriaClinicaJpaEntity();
        entidad.id = historia.getId();
        entidad.pacienteId = historia.getPacienteId();
        entidad.grupoSanguineo = historia.getGrupoSanguineo();
        entidad.alergias = new ArrayList<>(historia.getAlergias());
        entidad.antecedentes = historia.getAntecedentes();
        entidad.registrosConsulta = new ArrayList<>(historia.getRegistrosConsulta().stream()
                .map(r -> new RegistroConsultaJpa(r.getConsultaId(), r.getMedicoId(), r.getDiagnostico(),
                        r.getNotas(), r.getRecetaSimplificada(), r.getFechaRegistro()))
                .toList());
        entidad.registrosAcceso = new ArrayList<>(historia.getRegistrosAcceso().stream()
                .map(a -> new RegistroAccesoJpa(a.medicoId(), a.fechaHora()))
                .toList());
        return entidad;
    }

    HistoriaClinica aDominio() {
        return HistoriaClinica.reconstituir(id, pacienteId, grupoSanguineo, alergias, antecedentes,
                registrosConsulta.stream()
                        .map(r -> RegistroConsulta.reconstituir(r.consultaId(), r.medicoId(), r.diagnostico(),
                                r.notas(), r.recetaSimplificada(), r.fechaRegistro()))
                        .toList(),
                registrosAcceso.stream()
                        .map(a -> new RegistroAcceso(a.medicoId(), a.fechaHora()))
                        .toList());
    }

    @Embeddable
    record RegistroConsultaJpa(String consultaId, String medicoId,
                               @Column(length = 2000) String diagnostico,
                               @Column(length = 4000) String notas,
                               @Column(length = 2000) String recetaSimplificada,
                               LocalDateTime fechaRegistro) {
    }

    @Embeddable
    record RegistroAccesoJpa(String medicoId, LocalDateTime fechaHora) {
    }
}
