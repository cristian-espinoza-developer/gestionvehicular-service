package ec.edu.ups.gestionvehicular.turnos.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class FranjaMantenimiento {

    private final UUID id;
    private final LocalDateTime fechaHoraInicio;
    private final LocalDateTime fechaHoraFin;
    private final EstadoFranja estado;

    public FranjaMantenimiento(UUID id, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, EstadoFranja estado) {
        if (fechaHoraFin.isBefore(fechaHoraInicio) || fechaHoraFin.isEqual(fechaHoraInicio)) {
            throw new IllegalArgumentException("fechaHoraFin debe ser posterior a fechaHoraInicio");
        }
        this.id = Objects.requireNonNull(id);
        this.fechaHoraInicio = Objects.requireNonNull(fechaHoraInicio);
        this.fechaHoraFin = Objects.requireNonNull(fechaHoraFin);
        this.estado = Objects.requireNonNull(estado);
    }

    public boolean estaDisponible(LocalDateTime ahora) {
        return estado == EstadoFranja.DISPONIBLE && fechaHoraInicio.isAfter(ahora);
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
