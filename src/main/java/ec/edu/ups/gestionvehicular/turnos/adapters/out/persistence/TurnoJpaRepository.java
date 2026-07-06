package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TurnoJpaRepository extends JpaRepository<TurnoJpaEntity, UUID> {

    boolean existsByVehiculoIdAndEstado(UUID vehiculoId, EstadoTurno estado);
}
