package ec.edu.ups.gestionvehicular.turnos.adapters.out.notification;

import ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence.NotificacionJpaEntity;
import ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence.NotificacionJpaRepository;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.NotificadorTurnoPort;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.DestinatarioTipo;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.EstadoEnvio;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class NotificadorTurnoAdapter implements NotificadorTurnoPort {

    private static final Logger log = LoggerFactory.getLogger(NotificadorTurnoAdapter.class);

    private final NotificacionJpaRepository jpaRepository;

    public NotificadorTurnoAdapter(NotificacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void notificar(Turno turno) {
        registrarNotificacion(turno, DestinatarioTipo.MECANICO);
        registrarNotificacion(turno, DestinatarioTipo.ENCARGADO);
    }

    private void registrarNotificacion(Turno turno, DestinatarioTipo destinatarioTipo) {
        EstadoEnvio estadoEnvio;
        try {
            enviar(turno, destinatarioTipo);
            estadoEnvio = EstadoEnvio.ENVIADA;
        } catch (RuntimeException fallo) {
            log.warn("Fallo al notificar turno {} a {}; queda pendiente de reintento", turno.getId(), destinatarioTipo, fallo);
            estadoEnvio = EstadoEnvio.PENDIENTE;
        }
        Notificacion notificacion = new Notificacion(UUID.randomUUID(), turno.getId(), destinatarioTipo, estadoEnvio, LocalDateTime.now());
        try {
            jpaRepository.save(aJpaEntity(notificacion));
        } catch (RuntimeException fallo) {
            log.error("No se pudo registrar la notificación de turno {} a {}", turno.getId(), destinatarioTipo, fallo);
        }
    }

    /**
     * Punto de extensión para el envío real (correo, push, etc.); una falla aquí
     * NUNCA debe revertir el turno ya confirmado (ver Edge Case en spec.md).
     */
    protected void enviar(Turno turno, DestinatarioTipo destinatarioTipo) {
        log.info("Notificando turno {} a {}", turno.getId(), destinatarioTipo);
    }

    private static NotificacionJpaEntity aJpaEntity(Notificacion notificacion) {
        return new NotificacionJpaEntity(
            notificacion.getId(),
            notificacion.getTurnoId(),
            notificacion.getDestinatarioTipo(),
            notificacion.getEstadoEnvio(),
            notificacion.getFechaEnvio()
        );
    }
}
