package ec.edu.ups.gestionvehicular.turnos.application.service;

import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarFranjasDisponiblesUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.FranjaRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.VehiculoAsignadoPort;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.VehiculoSinAsignacionException;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultarFranjasDisponiblesService implements ConsultarFranjasDisponiblesUseCase {

    private static final int LIMITE_RESULTADOS = 20;

    private final FranjaRepositoryPort franjaRepositoryPort;
    private final VehiculoAsignadoPort vehiculoAsignadoPort;

    public ConsultarFranjasDisponiblesService(FranjaRepositoryPort franjaRepositoryPort, VehiculoAsignadoPort vehiculoAsignadoPort) {
        this.franjaRepositoryPort = franjaRepositoryPort;
        this.vehiculoAsignadoPort = vehiculoAsignadoPort;
    }

    @Override
    public List<FranjaMantenimiento> consultarDisponibles(UUID policiaId, UUID vehiculoId) {
        vehiculoAsignadoPort.buscarPorVehiculoYPolicia(vehiculoId, policiaId)
            .orElseThrow(() -> new VehiculoSinAsignacionException(vehiculoId));

        return franjaRepositoryPort.buscarDisponibles(LocalDateTime.now(), LIMITE_RESULTADOS);
    }
}
