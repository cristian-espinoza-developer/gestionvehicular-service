package ec.edu.ups.gestionvehicular.turnos.application.service;

import ec.edu.ups.gestionvehicular.turnos.application.port.in.AgendarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.FranjaRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.NotificadorTurnoPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.TurnoRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.VehiculoAsignadoPort;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.FranjaNoDisponibleException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.TurnoVigenteExistenteException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.VehiculoSinAsignacionException;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgendarTurnoService implements AgendarTurnoUseCase {

    private static final int LIMITE_ALTERNATIVAS = 5;

    private final FranjaRepositoryPort franjaRepositoryPort;
    private final TurnoRepositoryPort turnoRepositoryPort;
    private final VehiculoAsignadoPort vehiculoAsignadoPort;
    private final NotificadorTurnoPort notificadorTurnoPort;

    public AgendarTurnoService(FranjaRepositoryPort franjaRepositoryPort,
                               TurnoRepositoryPort turnoRepositoryPort,
                               VehiculoAsignadoPort vehiculoAsignadoPort,
                               NotificadorTurnoPort notificadorTurnoPort) {
        this.franjaRepositoryPort = franjaRepositoryPort;
        this.turnoRepositoryPort = turnoRepositoryPort;
        this.vehiculoAsignadoPort = vehiculoAsignadoPort;
        this.notificadorTurnoPort = notificadorTurnoPort;
    }

    @Override
    public Turno agendar(UUID policiaId, UUID vehiculoId, UUID franjaId) {
        vehiculoAsignadoPort.buscarPorVehiculoYPolicia(vehiculoId, policiaId)
            .orElseThrow(() -> new VehiculoSinAsignacionException(vehiculoId));

        if (turnoRepositoryPort.existeTurnoVigentePara(vehiculoId)) {
            throw new TurnoVigenteExistenteException(vehiculoId);
        }

        boolean reservada = franjaRepositoryPort.reservar(franjaId);
        if (!reservada) {
            List<FranjaMantenimiento> alternativas = franjaRepositoryPort.buscarDisponibles(LocalDateTime.now(), LIMITE_ALTERNATIVAS);
            throw new FranjaNoDisponibleException(alternativas);
        }

        Turno turno = Turno.agendar(UUID.randomUUID(), vehiculoId, policiaId, franjaId, LocalDateTime.now());
        Turno guardado = turnoRepositoryPort.guardar(turno);
        notificadorTurnoPort.notificar(guardado);
        return guardado;
    }
}
