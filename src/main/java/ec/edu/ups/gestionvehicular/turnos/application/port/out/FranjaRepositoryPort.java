package ec.edu.ups.gestionvehicular.turnos.application.port.out;

import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FranjaRepositoryPort {

    List<FranjaMantenimiento> buscarDisponibles(LocalDateTime ahora, int limite);

    Optional<FranjaMantenimiento> buscarPorId(UUID franjaId);

    boolean reservar(UUID franjaId);
}
