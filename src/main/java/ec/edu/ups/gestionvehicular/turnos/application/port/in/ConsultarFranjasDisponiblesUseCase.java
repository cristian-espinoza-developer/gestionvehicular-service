package ec.edu.ups.gestionvehicular.turnos.application.port.in;

import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;

import java.util.List;
import java.util.UUID;

public interface ConsultarFranjasDisponiblesUseCase {

    List<FranjaMantenimiento> consultarDisponibles(UUID policiaId, UUID vehiculoId);
}
