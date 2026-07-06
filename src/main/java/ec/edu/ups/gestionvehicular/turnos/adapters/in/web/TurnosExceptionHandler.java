package ec.edu.ups.gestionvehicular.turnos.adapters.in.web;

import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.model.ErrorResponse;
import ec.edu.ups.gestionvehicular.turnos.adapters.in.web.generated.model.FranjaOcupadaResponse;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.FranjaNoDisponibleException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.TurnoVigenteExistenteException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.VehiculoSinAsignacionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class TurnosExceptionHandler {

    @ExceptionHandler(VehiculoSinAsignacionException.class)
    public ResponseEntity<ErrorResponse> manejarVehiculoSinAsignacion(VehiculoSinAsignacionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TurnoVigenteExistenteException.class)
    public ResponseEntity<ErrorResponse> manejarTurnoVigenteExistente(TurnoVigenteExistenteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(FranjaNoDisponibleException.class)
    public ResponseEntity<FranjaOcupadaResponse> manejarFranjaNoDisponible(FranjaNoDisponibleException ex) {
        FranjaOcupadaResponse respuesta = new FranjaOcupadaResponse().mensaje(ex.getMessage());
        ex.getProximasFranjasDisponibles().forEach(franja ->
            respuesta.addProximasFranjasDisponiblesItem(TurnosController.aFranjaDisponible(franja)));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }
}
