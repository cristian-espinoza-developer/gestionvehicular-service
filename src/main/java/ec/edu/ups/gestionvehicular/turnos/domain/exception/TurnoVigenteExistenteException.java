package ec.edu.ups.gestionvehicular.turnos.domain.exception;

import java.util.UUID;

public class TurnoVigenteExistenteException extends RuntimeException {

    public TurnoVigenteExistenteException(UUID vehiculoId) {
        super("El vehículo " + vehiculoId + " ya tiene un turno vigente (AGENDADO) pendiente de completar.");
    }
}
