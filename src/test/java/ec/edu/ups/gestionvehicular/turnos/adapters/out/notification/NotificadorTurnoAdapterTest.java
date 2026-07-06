package ec.edu.ups.gestionvehicular.turnos.adapters.out.notification;

import ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence.NotificacionJpaEntity;
import ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence.NotificacionJpaRepository;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.DestinatarioTipo;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.EstadoEnvio;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificadorTurnoAdapterTest {

    @Mock
    private NotificacionJpaRepository jpaRepository;

    @Test
    void debe_marcar_ambas_notificaciones_como_enviadas_cuando_no_hay_fallos() {
        NotificadorTurnoAdapter adapter = new NotificadorTurnoAdapter(jpaRepository);
        Turno turno = Turno.agendar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());

        adapter.notificar(turno);

        ArgumentCaptor<NotificacionJpaEntity> captor = ArgumentCaptor.forClass(NotificacionJpaEntity.class);
        verify(jpaRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
            .extracting(NotificacionJpaEntity::getEstadoEnvio)
            .containsOnly(EstadoEnvio.ENVIADA);
    }

    @Test
    void una_falla_al_enviar_no_debe_propagar_excepcion_y_debe_quedar_pendiente() {
        NotificadorTurnoAdapter adapter = spy(new NotificadorTurnoAdapter(jpaRepository));
        Turno turno = Turno.agendar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());
        doThrow(new RuntimeException("canal de notificación no disponible"))
            .when(adapter).enviar(any(Turno.class), eq(DestinatarioTipo.MECANICO));

        assertThatCode(() -> adapter.notificar(turno)).doesNotThrowAnyException();

        ArgumentCaptor<NotificacionJpaEntity> captor = ArgumentCaptor.forClass(NotificacionJpaEntity.class);
        verify(jpaRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        List<NotificacionJpaEntity> guardadas = captor.getAllValues();
        assertThat(guardadas)
            .filteredOn(n -> n.getDestinatarioTipo() == DestinatarioTipo.MECANICO)
            .extracting(NotificacionJpaEntity::getEstadoEnvio)
            .containsExactly(EstadoEnvio.PENDIENTE);
        assertThat(guardadas)
            .filteredOn(n -> n.getDestinatarioTipo() == DestinatarioTipo.ENCARGADO)
            .extracting(NotificacionJpaEntity::getEstadoEnvio)
            .containsExactly(EstadoEnvio.ENVIADA);
    }
}
