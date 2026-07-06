package ec.edu.ups.gestionvehicular.turnos.application.port.out;

import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;

import java.util.Optional;
import java.util.UUID;

public interface TurnoRepositoryPort {

    Turno guardar(Turno turno);

    Optional<Turno> buscarPorId(UUID turnoId);

    boolean existeTurnoVigentePara(UUID vehiculoId);
}
