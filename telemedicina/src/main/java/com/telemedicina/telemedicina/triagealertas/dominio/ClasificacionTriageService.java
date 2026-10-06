package com.telemedicina.telemedicina.triagealertas.dominio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de Dominio: aplica las reglas del cuestionario de triage para clasificar el nivel de
 * urgencia y decide si dispara una Alerta clínica. Involucra CuestionarioTriage y AlertaClinica,
 * así que no pertenece naturalmente a ninguna de esas entidades.
 *
 * <p>No avisa a enfermería: eso es hablar con el mundo exterior y lo hace el caso de uso a través
 * del puerto secundario {@code NotificadorEnfermeria}. Así el dominio solo importa {@code java.*}.</p>
 *
 * <p>Reglas de clasificación:</p>
 * <ul>
 *   <li><b>alto</b>: un síntoma de alarma con intensidad &ge; 5, o cualquier síntoma con intensidad &ge; 8.</li>
 *   <li><b>medio</b>: algún síntoma con intensidad &ge; 5, o tres o más síntomas presentes.</li>
 *   <li><b>bajo</b>: en cualquier otro caso.</li>
 * </ul>
 */
public class ClasificacionTriageService {

    static final Set<String> SINTOMAS_DE_ALARMA = Set.of(
            "dolor de pecho", "dificultad para respirar", "perdida de conciencia", "sangrado abundante");

    private static final int INTENSIDAD_ALARMA = 5;
    private static final int INTENSIDAD_SEVERA = 8;
    private static final int INTENSIDAD_MODERADA = 5;
    private static final int SINTOMAS_PARA_MEDIO = 3;

    private final Clock reloj;

    public ClasificacionTriageService(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * HU-03 · clasifica el cuestionario y, si la urgencia es alta, genera la alerta clínica en la
     * misma operación (regla: "menos de 1 minuto").
     */
    public NivelUrgencia clasificar(CuestionarioTriage cuestionario) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        NivelUrgencia nivel = calcularNivel(cuestionario);
        cuestionario.clasificar(nivel, ahora);

        if (nivel.esAlto()) {
            cuestionario.generarAlerta(UUID.randomUUID().toString(), ahora);
        }
        return nivel;
    }

    private NivelUrgencia calcularNivel(CuestionarioTriage cuestionario) {
        boolean alarma = cuestionario.getRespuestas().stream().anyMatch(r ->
                (SINTOMAS_DE_ALARMA.contains(r.sintoma()) && r.intensidad() >= INTENSIDAD_ALARMA)
                        || r.intensidad() >= INTENSIDAD_SEVERA);
        if (alarma) {
            return NivelUrgencia.ALTO;
        }
        long sintomasPresentes = cuestionario.getRespuestas().stream()
                .filter(r -> r.intensidad() > 0)
                .count();
        boolean moderado = cuestionario.getRespuestas().stream()
                .anyMatch(r -> r.intensidad() >= INTENSIDAD_MODERADA);
        if (moderado || sintomasPresentes >= SINTOMAS_PARA_MEDIO) {
            return NivelUrgencia.MEDIO;
        }
        return NivelUrgencia.BAJO;
    }
}
