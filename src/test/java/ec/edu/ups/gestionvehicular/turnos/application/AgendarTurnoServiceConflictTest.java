package ec.edu.ups.gestionvehicular.turnos.application;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.FranjaRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.NotificadorTurnoPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.TurnoRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.VehiculoAsignadoPort;
import ec.edu.ups.gestionvehicular.turnos.application.service.AgendarTurnoService;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.FranjaNoDisponibleException;
import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import ec.edu.ups.gestionvehicular.turnos.domain.model.VehiculoAsignado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AgendarTurnoServiceConflictTest {

    private static final UUID POLICIA_ID = UUID.randomUUID();
    private static final UUID VEHICULO_ID = UUID.randomUUID();
    private static final UUID FRANJA_OCUPADA_ID = UUID.randomUUID();

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
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID))
            .thenReturn(Optional.of(new VehiculoAsignado(VEHICULO_ID, POLICIA_ID)));
        when(turnoRepositoryPort.existeTurnoVigentePara(VEHICULO_ID)).thenReturn(false);
    }

    @Test
    void cuando_la_franja_ya_fue_reservada_debe_lanzar_excepcion_con_proximas_franjas_disponibles() {
        LocalDateTime ahora = LocalDateTime.now();
        FranjaMantenimiento alternativa = new FranjaMantenimiento(UUID.randomUUID(), ahora.plusDays(2), ahora.plusDays(2).plusHours(1), EstadoFranja.DISPONIBLE);
        when(franjaRepositoryPort.reservar(FRANJA_OCUPADA_ID)).thenReturn(false);
        when(franjaRepositoryPort.buscarDisponibles(any(LocalDateTime.class), any(Integer.class))).thenReturn(List.of(alternativa));

        assertThatThrownBy(() -> service.agendar(POLICIA_ID, VEHICULO_ID, FRANJA_OCUPADA_ID))
            .isInstanceOf(FranjaNoDisponibleException.class)
            .satisfies(ex -> assertThat(((FranjaNoDisponibleException) ex).getProximasFranjasDisponibles())
                .containsExactly(alternativa));

        verify(turnoRepositoryPort, never()).guardar(any());
        verify(notificadorTurnoPort, never()).notificar(any());
    }

    @Test
    void debe_agendar_normalmente_al_elegir_una_de_las_franjas_alternativas() {
        UUID franjaAlternativa = UUID.randomUUID();
        when(franjaRepositoryPort.reservar(franjaAlternativa)).thenReturn(true);
        when(turnoRepositoryPort.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertThat(service.agendar(POLICIA_ID, VEHICULO_ID, franjaAlternativa).getFranjaId()).isEqualTo(franjaAlternativa);

        verify(notificadorTurnoPort).notificar(any());
    }
}
