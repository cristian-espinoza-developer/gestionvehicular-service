package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoTurno;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import ec.edu.ups.gestionvehicular.turnos.domain.model.VehiculoAsignado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({FranjaRepositoryAdapter.class, TurnoRepositoryAdapter.class, VehiculoAsignadoRepositoryAdapter.class})
class PersistenceAdaptersIT {

    private static final UUID VEHICULO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID POLICIA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private FranjaRepositoryAdapter franjaRepositoryAdapter;

    @Autowired
    private TurnoRepositoryAdapter turnoRepositoryAdapter;

    @Autowired
    private VehiculoAsignadoRepositoryAdapter vehiculoAsignadoRepositoryAdapter;

    @Test
    void debe_encontrar_el_vehiculo_asignado_precargado() {
        Optional<VehiculoAsignado> resultado = vehiculoAsignadoRepositoryAdapter
            .buscarPorVehiculoYPolicia(VEHICULO_ID, POLICIA_ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().perteneceA(POLICIA_ID)).isTrue();
    }

    @Test
    void debe_listar_las_franjas_disponibles_precargadas_en_orden() {
        List<?> franjas = franjaRepositoryAdapter.buscarDisponibles(LocalDateTime.now(), 10);

        assertThat(franjas).isNotEmpty();
    }

    @Test
    void debe_reservar_una_franja_disponible_una_sola_vez() {
        List<ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento> franjas =
            franjaRepositoryAdapter.buscarDisponibles(LocalDateTime.now(), 1);
        UUID franjaId = franjas.get(0).getId();

        boolean primeraReserva = franjaRepositoryAdapter.reservar(franjaId);
        boolean segundaReserva = franjaRepositoryAdapter.reservar(franjaId);

        assertThat(primeraReserva).isTrue();
        assertThat(segundaReserva).isFalse();
    }

    @Test
    void debe_guardar_y_recuperar_un_turno() {
        List<ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento> franjas =
            franjaRepositoryAdapter.buscarDisponibles(LocalDateTime.now(), 10);
        UUID franjaId = franjas.get(0).getId();

        Turno turno = Turno.agendar(UUID.randomUUID(), VEHICULO_ID, POLICIA_ID, franjaId, LocalDateTime.now());
        Turno guardado = turnoRepositoryAdapter.guardar(turno);

        Optional<Turno> recuperado = turnoRepositoryAdapter.buscarPorId(guardado.getId());

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getEstado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(turnoRepositoryAdapter.existeTurnoVigentePara(VEHICULO_ID)).isTrue();
    }
}
