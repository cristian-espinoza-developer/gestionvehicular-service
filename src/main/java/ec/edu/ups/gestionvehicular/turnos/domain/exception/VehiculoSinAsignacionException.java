package ec.edu.ups.gestionvehicular.turnos.domain.exception;

import java.util.UUID;

public class VehiculoSinAsignacionException extends RuntimeException {

    public VehiculoSinAsignacionException(UUID vehiculoId) {
        super("El vehículo " + vehiculoId + " no está asignado al policía solicitante.");
    }
}
