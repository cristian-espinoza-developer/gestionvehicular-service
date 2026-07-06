package ec.edu.ups.gestionvehicular.turnos.application.port.out;

import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;

public interface NotificadorTurnoPort {

    void notificar(Turno turno);
}
