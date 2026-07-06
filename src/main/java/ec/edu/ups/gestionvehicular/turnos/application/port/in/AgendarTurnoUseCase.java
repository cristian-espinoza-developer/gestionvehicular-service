package ec.edu.ups.gestionvehicular.turnos.application.port.in;

import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;

import java.util.UUID;

public interface AgendarTurnoUseCase {

    Turno agendar(UUID policiaId, UUID vehiculoId, UUID franjaId);
}
