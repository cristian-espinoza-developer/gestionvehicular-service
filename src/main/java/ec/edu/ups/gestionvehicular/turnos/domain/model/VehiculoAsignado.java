package ec.edu.ups.gestionvehicular.turnos.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class VehiculoAsignado {

    private final UUID vehiculoId;
    private final UUID policiaId;

    public VehiculoAsignado(UUID vehiculoId, UUID policiaId) {
        this.vehiculoId = Objects.requireNonNull(vehiculoId);
        this.policiaId = Objects.requireNonNull(policiaId);
    }

    public boolean perteneceA(UUID policiaId) {
        return this.policiaId.equals(policiaId);
    }

    public UUID getVehiculoId() {
        return vehiculoId;
    }

    public UUID getPoliciaId() {
        return policiaId;
    }
}
