package ec.edu.ups.gestionvehicular.turnos.application.service;

import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.TurnoRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ConsultarTurnoService implements ConsultarTurnoUseCase {

    private final TurnoRepositoryPort turnoRepositoryPort;

    public ConsultarTurnoService(TurnoRepositoryPort turnoRepositoryPort) {
        this.turnoRepositoryPort = turnoRepositoryPort;
    }

    @Override
    public Turno consultarPorId(UUID turnoId) {
        return turnoRepositoryPort.buscarPorId(turnoId)
            .orElseThrow(() -> new NoSuchElementException("Turno no encontrado: " + turnoId));
    }
}
