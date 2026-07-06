package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "franja_mantenimiento")
public class FranjaMantenimientoJpaEntity {

    @Id
    private UUID id;

    private LocalDateTime fechaHoraInicio;

    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    private EstadoFranja estado;

    protected FranjaMantenimientoJpaEntity() {
    }

    public FranjaMantenimientoJpaEntity(UUID id, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, EstadoFranja estado) {
        this.id = id;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.estado = estado;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public EstadoFranja getEstado() {
        return estado;
    }
}
