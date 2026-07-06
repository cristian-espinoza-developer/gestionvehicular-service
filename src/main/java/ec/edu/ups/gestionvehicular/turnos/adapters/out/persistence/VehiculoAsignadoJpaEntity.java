package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vehiculo_asignado")
@IdClass(VehiculoAsignadoId.class)
public class VehiculoAsignadoJpaEntity {

    @Id
    private UUID vehiculoId;

    @Id
    private UUID policiaId;

    protected VehiculoAsignadoJpaEntity() {
    }

    public VehiculoAsignadoJpaEntity(UUID vehiculoId, UUID policiaId) {
        this.vehiculoId = vehiculoId;
        this.policiaId = policiaId;
    }

    public UUID getVehiculoId() {
        return vehiculoId;
    }

    public UUID getPoliciaId() {
        return policiaId;
    }
}
