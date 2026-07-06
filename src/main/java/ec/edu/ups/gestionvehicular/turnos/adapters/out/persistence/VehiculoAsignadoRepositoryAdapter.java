package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.VehiculoAsignadoPort;
import ec.edu.ups.gestionvehicular.turnos.domain.model.VehiculoAsignado;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VehiculoAsignadoRepositoryAdapter implements VehiculoAsignadoPort {

    private final VehiculoAsignadoJpaRepository jpaRepository;

    public VehiculoAsignadoRepositoryAdapter(VehiculoAsignadoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<VehiculoAsignado> buscarPorVehiculoYPolicia(UUID vehiculoId, UUID policiaId) {
        return jpaRepository.findByVehiculoIdAndPoliciaId(vehiculoId, policiaId)
            .map(entidad -> new VehiculoAsignado(entidad.getVehiculoId(), entidad.getPoliciaId()));
    }
}
