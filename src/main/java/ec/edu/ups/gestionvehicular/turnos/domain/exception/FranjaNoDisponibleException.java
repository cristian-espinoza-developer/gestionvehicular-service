package ec.edu.ups.gestionvehicular.turnos.domain.exception;

import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;

import java.util.List;

public class FranjaNoDisponibleException extends RuntimeException {

    private final List<FranjaMantenimiento> proximasFranjasDisponibles;

    public FranjaNoDisponibleException(List<FranjaMantenimiento> proximasFranjasDisponibles) {
        super("La franja seleccionada ya no está disponible.");
        this.proximasFranjasDisponibles = proximasFranjasDisponibles;
    }

    public List<FranjaMantenimiento> getProximasFranjasDisponibles() {
        return proximasFranjasDisponibles;
    }
}
