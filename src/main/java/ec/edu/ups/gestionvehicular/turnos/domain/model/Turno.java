package ec.edu.ups.gestionvehicular.turnos.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Turno {

    private final UUID id;
    private final UUID vehiculoId;
    private final UUID policiaId;
    private final UUID franjaId;
    private final EstadoTurno estado;
    private final LocalDateTime fechaCreacion;

    public Turno(UUID id, UUID vehiculoId, UUID policiaId, UUID franjaId, EstadoTurno estado, LocalDateTime fechaCreacion) {
        this.id = Objects.requireNonNull(id);
        this.vehiculoId = Objects.requireNonNull(vehiculoId);
        this.policiaId = Objects.requireNonNull(policiaId);
        this.franjaId = Objects.requireNonNull(franjaId);
        this.estado = Objects.requireNonNull(estado);
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion);
    }

    public static Turno agendar(UUID id, UUID vehiculoId, UUID policiaId, UUID franjaId, LocalDateTime ahora) {
        return new Turno(id, vehiculoId, policiaId, franjaId, EstadoTurno.AGENDADO, ahora);
    }

    public boolean estaVigente() {
        return estado == EstadoTurno.AGENDADO;
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
