package ec.edu.ups.gestionvehicular.turnos.domain;

import ec.edu.ups.gestionvehicular.turnos.domain.model.VehiculoAsignado;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehiculoAsignadoTest {

    @Test
    void pertenece_al_policia_asignado() {
        UUID vehiculoId = UUID.randomUUID();
        UUID policiaId = UUID.randomUUID();
        VehiculoAsignado vehiculo = new VehiculoAsignado(vehiculoId, policiaId);

        assertThat(vehiculo.perteneceA(policiaId)).isTrue();
        assertThat(vehiculo.getVehiculoId()).isEqualTo(vehiculoId);
        assertThat(vehiculo.getPoliciaId()).isEqualTo(policiaId);
    }

    @Test
    void no_pertenece_a_un_policia_distinto() {
        VehiculoAsignado vehiculo = new VehiculoAsignado(UUID.randomUUID(), UUID.randomUUID());

        assertThat(vehiculo.perteneceA(UUID.randomUUID())).isFalse();
    }
}
