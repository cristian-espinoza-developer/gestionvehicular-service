package ec.edu.ups.gestionvehicular.turnos.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Notificacion {

    public enum DestinatarioTipo {
        MECANICO,
        ENCARGADO
    }

    public enum EstadoEnvio {
        ENVIADA,
        PENDIENTE
    }

    private final UUID id;
    private final UUID turnoId;
    private final DestinatarioTipo destinatarioTipo;
    private final EstadoEnvio estadoEnvio;
    private final LocalDateTime fechaEnvio;

    public Notificacion(UUID id, UUID turnoId, DestinatarioTipo destinatarioTipo, EstadoEnvio estadoEnvio, LocalDateTime fechaEnvio) {
        this.id = Objects.requireNonNull(id);
        this.turnoId = Objects.requireNonNull(turnoId);
        this.destinatarioTipo = Objects.requireNonNull(destinatarioTipo);
        this.estadoEnvio = Objects.requireNonNull(estadoEnvio);
        this.fechaEnvio = Objects.requireNonNull(fechaEnvio);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTurnoId() {
        return turnoId;
    }

    public DestinatarioTipo getDestinatarioTipo() {
        return destinatarioTipo;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
