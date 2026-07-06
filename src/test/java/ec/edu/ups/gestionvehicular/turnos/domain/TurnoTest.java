package ec.edu.ups.gestionvehicular.turnos.domain;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoTurno;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TurnoTest {

    @Test
    void agendar_crea_un_turno_en_estado_agendado() {
        UUID id = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();
        UUID policiaId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        LocalDateTime ahora = LocalDateTime.now();

        Turno turno = Turno.agendar(id, vehiculoId, policiaId, franjaId, ahora);

        assertThat(turno.getId()).isEqualTo(id);
        assertThat(turno.getVehiculoId()).isEqualTo(vehiculoId);
        assertThat(turno.getPoliciaId()).isEqualTo(policiaId);
        assertThat(turno.getFranjaId()).isEqualTo(franjaId);
        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.AGENDADO);
        assertThat(turno.getFechaCreacion()).isEqualTo(ahora);
        assertThat(turno.estaVigente()).isTrue();
    }

    @Test
    void un_turno_completado_no_esta_vigente() {
        Turno turno = new Turno(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            EstadoTurno.COMPLETADO, LocalDateTime.now());

        assertThat(turno.estaVigente()).isFalse();
    }
}
