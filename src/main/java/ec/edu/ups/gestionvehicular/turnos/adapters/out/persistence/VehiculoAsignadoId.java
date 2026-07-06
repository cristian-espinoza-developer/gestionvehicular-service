package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class VehiculoAsignadoId implements Serializable {

    private UUID vehiculoId;
    private UUID policiaId;

    public VehiculoAsignadoId() {
    }

    public VehiculoAsignadoId(UUID vehiculoId, UUID policiaId) {
        this.vehiculoId = vehiculoId;
        this.policiaId = policiaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehiculoAsignadoId that)) return false;
        return Objects.equals(vehiculoId, that.vehiculoId) && Objects.equals(policiaId, that.policiaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehiculoId, policiaId);
    }
}
