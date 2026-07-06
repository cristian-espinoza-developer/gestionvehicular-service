package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.DestinatarioTipo;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.EstadoEnvio;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificacion")
public class NotificacionJpaEntity {

    @Id
    private UUID id;

    private UUID turnoId;

    @Enumerated(EnumType.STRING)
    private DestinatarioTipo destinatarioTipo;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estadoEnvio;

    private LocalDateTime fechaEnvio;

    protected NotificacionJpaEntity() {
    }

    public NotificacionJpaEntity(UUID id, UUID turnoId, DestinatarioTipo destinatarioTipo, EstadoEnvio estadoEnvio, LocalDateTime fechaEnvio) {
        this.id = id;
        this.turnoId = turnoId;
        this.destinatarioTipo = destinatarioTipo;
        this.estadoEnvio = estadoEnvio;
        this.fechaEnvio = fechaEnvio;
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
