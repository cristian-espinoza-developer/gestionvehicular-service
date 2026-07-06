package ec.edu.ups.gestionvehicular.turnos.adapters.in.web;

import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.api.TurnosApi;
import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.model.AgendarTurnoRequest;
import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.model.FranjaDisponible;
import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.model.FranjasDisponiblesResponse;
import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.model.TurnoResponse;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.AgendarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarFranjasDisponiblesUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.UUID;

@RestController
public class TurnosController implements TurnosApi {

    private final AgendarTurnoUseCase agendarTurnoUseCase;
    private final ConsultarFranjasDisponiblesUseCase consultarFranjasDisponiblesUseCase;
    private final ConsultarTurnoUseCase consultarTurnoUseCase;

    public TurnosController(AgendarTurnoUseCase agendarTurnoUseCase,
                             ConsultarFranjasDisponiblesUseCase consultarFranjasDisponiblesUseCase,
                             ConsultarTurnoUseCase consultarTurnoUseCase) {
        this.agendarTurnoUseCase = agendarTurnoUseCase;
        this.consultarFranjasDisponiblesUseCase = consultarFranjasDisponiblesUseCase;
        this.consultarTurnoUseCase = consultarTurnoUseCase;
    }

    @Override
    public ResponseEntity<TurnoResponse> agendarTurno(UUID xPoliciaId, AgendarTurnoRequest agendarTurnoRequest) {
        Turno turno = agendarTurnoUseCase.agendar(xPoliciaId, agendarTurnoRequest.getVehiculoId(), agendarTurnoRequest.getFranjaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(aTurnoResponse(turno));
    }

    @Override
    public ResponseEntity<TurnoResponse> consultarTurno(UUID turnoId) {
        Turno turno = consultarTurnoUseCase.consultarPorId(turnoId);
        return ResponseEntity.ok(aTurnoResponse(turno));
    }

    @Override
    public ResponseEntity<FranjasDisponiblesResponse> listarFranjasDisponibles(UUID vehiculoId, UUID xPoliciaId) {
        FranjasDisponiblesResponse respuesta = new FranjasDisponiblesResponse();
        consultarFranjasDisponiblesUseCase.consultarDisponibles(xPoliciaId, vehiculoId)
            .forEach(franja -> respuesta.addFranjasItem(aFranjaDisponible(franja)));
        return ResponseEntity.ok(respuesta);
    }

    static TurnoResponse aTurnoResponse(Turno turno) {
        return new TurnoResponse()
            .id(turno.getId())
            .vehiculoId(turno.getVehiculoId())
            .franjaId(turno.getFranjaId())
            .estado(TurnoResponse.EstadoEnum.fromValue(turno.getEstado().name()))
            .fechaCreacion(turno.getFechaCreacion().atOffset(ZoneOffset.UTC));
    }

    static FranjaDisponible aFranjaDisponible(FranjaMantenimiento franja) {
        return new FranjaDisponible()
            .id(franja.getId())
            .fechaHoraInicio(franja.getFechaHoraInicio().atOffset(ZoneOffset.UTC))
            .fechaHoraFin(franja.getFechaHoraFin().atOffset(ZoneOffset.UTC));
    }
}
