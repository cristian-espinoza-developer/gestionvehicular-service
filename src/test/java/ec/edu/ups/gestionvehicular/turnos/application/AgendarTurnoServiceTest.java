package ec.edu.ups.gestionvehicular.turnos.application;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.FranjaRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.NotificadorTurnoPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.TurnoRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.VehiculoAsignadoPort;
import ec.edu.ups.gestionvehicular.turnos.application.service.AgendarTurnoService;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.FranjaNoDisponibleException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.TurnoVigenteExistenteException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.VehiculoSinAsignacionException;
import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoTurno;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import ec.edu.ups.gestionvehicular.turnos.domain.model.VehiculoAsignado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendarTurnoServiceTest {

    private static final UUID POLICIA_ID = UUID.randomUUID();
    private static final UUID VEHICULO_ID = UUID.randomUUID();
    private static final UUID FRANJA_ID = UUID.randomUUID();

    @Mock
    private FranjaRepositoryPort franjaRepositoryPort;

    @Mock
    private TurnoRepositoryPort turnoRepositoryPort;

    @Mock
    private VehiculoAsignadoPort vehiculoAsignadoPort;

    @Mock
    private NotificadorTurnoPort notificadorTurnoPort;

    private AgendarTurnoService service;

    @BeforeEach
    void setUp() {
        service = new AgendarTurnoService(franjaRepositoryPort, turnoRepositoryPort, vehiculoAsignadoPort, notificadorTurnoPort);
    }

    @Test
    void debe_agendar_un_turno_cuando_la_franja_esta_disponible() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID))
            .thenReturn(Optional.of(new VehiculoAsignado(VEHICULO_ID, POLICIA_ID)));
        when(turnoRepositoryPort.existeTurnoVigentePara(VEHICULO_ID)).thenReturn(false);
        when(franjaRepositoryPort.reservar(FRANJA_ID)).thenReturn(true);
        when(turnoRepositoryPort.guardar(any(Turno.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Turno turno = service.agendar(POLICIA_ID, VEHICULO_ID, FRANJA_ID);

        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(turno.getVehiculoId()).isEqualTo(VEHICULO_ID);
        assertThat(turno.getPoliciaId()).isEqualTo(POLICIA_ID);
        assertThat(turno.getFranjaId()).isEqualTo(FRANJA_ID);

        ArgumentCaptor<Turno> captor = ArgumentCaptor.forClass(Turno.class);
        verify(notificadorTurnoPort).notificar(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(turno.getId());
    }

    @Test
    void debe_rechazar_si_el_vehiculo_no_esta_asignado_al_policia() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendar(POLICIA_ID, VEHICULO_ID, FRANJA_ID))
            .isInstanceOf(VehiculoSinAsignacionException.class);

        verify(franjaRepositoryPort, never()).reservar(any());
        verify(notificadorTurnoPort, never()).notificar(any());
    }

    @Test
    void debe_rechazar_si_ya_existe_un_turno_vigente_para_el_vehiculo() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID))
            .thenReturn(Optional.of(new VehiculoAsignado(VEHICULO_ID, POLICIA_ID)));
        when(turnoRepositoryPort.existeTurnoVigentePara(VEHICULO_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.agendar(POLICIA_ID, VEHICULO_ID, FRANJA_ID))
            .isInstanceOf(TurnoVigenteExistenteException.class);

        verify(franjaRepositoryPort, never()).reservar(any());
    }

    @Test
    void debe_devolver_proximas_franjas_disponibles_cuando_la_franja_ya_fue_reservada() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID))
            .thenReturn(Optional.of(new VehiculoAsignado(VEHICULO_ID, POLICIA_ID)));
        when(turnoRepositoryPort.existeTurnoVigentePara(VEHICULO_ID)).thenReturn(false);
        when(franjaRepositoryPort.reservar(FRANJA_ID)).thenReturn(false);
        when(franjaRepositoryPort.buscarDisponibles(any(LocalDateTime.class), any(Integer.class)))
            .thenReturn(List.of());

        assertThatThrownBy(() -> service.agendar(POLICIA_ID, VEHICULO_ID, FRANJA_ID))
            .isInstanceOf(FranjaNoDisponibleException.class);

        verify(turnoRepositoryPort, never()).guardar(any());
        verify(notificadorTurnoPort, never()).notificar(any());
    }
}
