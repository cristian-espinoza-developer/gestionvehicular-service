package ec.edu.ups.gestionvehicular.turnos.application;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.FranjaRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.port.out.VehiculoAsignadoPort;
import ec.edu.ups.gestionvehicular.turnos.application.service.ConsultarFranjasDisponiblesService;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.VehiculoSinAsignacionException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarFranjasDisponiblesServiceTest {

    private static final UUID POLICIA_ID = UUID.randomUUID();
    private static final UUID VEHICULO_ID = UUID.randomUUID();

    @Mock
    private FranjaRepositoryPort franjaRepositoryPort;

    @Mock
    private VehiculoAsignadoPort vehiculoAsignadoPort;

    private ConsultarFranjasDisponiblesService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarFranjasDisponiblesService(franjaRepositoryPort, vehiculoAsignadoPort);
    }

    @Test
    void debe_listar_las_franjas_disponibles_del_vehiculo_asignado() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID))
            .thenReturn(Optional.of(new VehiculoAsignado(VEHICULO_ID, POLICIA_ID)));
        LocalDateTime ahora = LocalDateTime.now();
        FranjaMantenimiento franja = new FranjaMantenimiento(UUID.randomUUID(), ahora.plusDays(1), ahora.plusDays(1).plusHours(1), EstadoFranja.DISPONIBLE);
        when(franjaRepositoryPort.buscarDisponibles(any(LocalDateTime.class), any(Integer.class))).thenReturn(List.of(franja));

        List<FranjaMantenimiento> resultado = service.consultarDisponibles(POLICIA_ID, VEHICULO_ID);

        assertThat(resultado).containsExactly(franja);
    }

    @Test
    void debe_rechazar_si_el_vehiculo_no_esta_asignado_al_policia() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarDisponibles(POLICIA_ID, VEHICULO_ID))
            .isInstanceOf(VehiculoSinAsignacionException.class);
    }

    @Test
    void debe_devolver_lista_vacia_cuando_no_hay_franjas_disponibles() {
        when(vehiculoAsignadoPort.buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID))
            .thenReturn(Optional.of(new VehiculoAsignado(VEHICULO_ID, POLICIA_ID)));
        when(franjaRepositoryPort.buscarDisponibles(any(LocalDateTime.class), any(Integer.class))).thenReturn(List.of());

        List<FranjaMantenimiento> resultado = service.consultarDisponibles(POLICIA_ID, VEHICULO_ID);

        assertThat(resultado).isEmpty();
    }
}
