package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoTurno;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "turno")
public class TurnoJpaEntity {

    @Id
    private UUID id;

    private UUID vehiculoId;

    private UUID policiaId;

    private UUID franjaId;

    @Enumerated(EnumType.STRING)
    private EstadoTurno estado;

    private LocalDateTime fechaCreacion;

    protected TurnoJpaEntity() {
    }

    public TurnoJpaEntity(UUID id, UUID vehiculoId, UUID policiaId, UUID franjaId, EstadoTurno estado, LocalDateTime fechaCreacion) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.policiaId = policiaId;
        this.franjaId = franjaId;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVehiculoId() {
        return vehiculoId;
    }

    public UUID getPoliciaId() {
        return policiaId;
    }

    public UUID getFranjaId() {
        return franjaId;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
