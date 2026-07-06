package ec.edu.ups.gestionvehicular.turnos.application.port.out;

import ec.edu.ups.gestionvehicular.turnos.domain.model.VehiculoAsignado;

import java.util.Optional;
import java.util.UUID;

public interface VehiculoAsignadoPort {

    Optional<VehiculoAsignado> buscarPorVehiculoYPolicia(UUID vehiculoId, UUID policiaId);
}
