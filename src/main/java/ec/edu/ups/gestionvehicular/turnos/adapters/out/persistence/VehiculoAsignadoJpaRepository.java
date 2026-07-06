package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehiculoAsignadoJpaRepository extends JpaRepository<VehiculoAsignadoJpaEntity, VehiculoAsignadoId> {

    Optional<VehiculoAsignadoJpaEntity> findByVehiculoIdAndPoliciaId(UUID vehiculoId, UUID policiaId);
}
