package ec.edu.ups.gestionvehicular.turnos.domain;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranjaMantenimientoTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    @Test
    void rechaza_una_franja_cuyo_fin_no_es_posterior_al_inicio() {
        assertThatThrownBy(() -> new FranjaMantenimiento(UUID.randomUUID(), ahora, ahora, EstadoFranja.DISPONIBLE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void una_franja_disponible_con_inicio_futuro_esta_disponible() {
        FranjaMantenimiento franja = new FranjaMantenimiento(UUID.randomUUID(), ahora.plusDays(1), ahora.plusDays(1).plusHours(1), EstadoFranja.DISPONIBLE);

        assertThat(franja.estaDisponible(ahora)).isTrue();
    }

    @Test
    void una_franja_ocupada_no_esta_disponible_aunque_su_inicio_sea_futuro() {
        FranjaMantenimiento franja = new FranjaMantenimiento(UUID.randomUUID(), ahora.plusDays(1), ahora.plusDays(1).plusHours(1), EstadoFranja.OCUPADA);

        assertThat(franja.estaDisponible(ahora)).isFalse();
    }

    @Test
    void una_franja_disponible_con_inicio_pasado_no_esta_disponible() {
        FranjaMantenimiento franja = new FranjaMantenimiento(UUID.randomUUID(), ahora.minusDays(1), ahora.minusDays(1).plusHours(1), EstadoFranja.DISPONIBLE);

        assertThat(franja.estaDisponible(ahora)).isFalse();
    }
}
