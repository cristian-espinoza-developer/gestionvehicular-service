package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JpaEntitiesTest {

    @Test
    void franja_mantenimiento_jpa_entity_expone_sus_campos() {
        UUID id = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fin = inicio.plusHours(1);

        FranjaMantenimientoJpaEntity entidad = new FranjaMantenimientoJpaEntity(id, inicio, fin, EstadoFranja.DISPONIBLE);
        FranjaMantenimientoJpaEntity vacia = new FranjaMantenimientoJpaEntity();

        assertThat(entidad.getId()).isEqualTo(id);
        assertThat(entidad.getFechaHoraInicio()).isEqualTo(inicio);
        assertThat(entidad.getFechaHoraFin()).isEqualTo(fin);
        assertThat(entidad.getEstado()).isEqualTo(EstadoFranja.DISPONIBLE);
        assertThat(vacia.getId()).isNull();
    }

    @Test
    void vehiculo_asignado_jpa_entity_expone_sus_campos() {
        UUID vehiculoId = UUID.randomUUID();
        UUID policiaId = UUID.randomUUID();

        VehiculoAsignadoJpaEntity entidad = new VehiculoAsignadoJpaEntity(vehiculoId, policiaId);
        VehiculoAsignadoJpaEntity vacia = new VehiculoAsignadoJpaEntity();

        assertThat(entidad.getVehiculoId()).isEqualTo(vehiculoId);
        assertThat(entidad.getPoliciaId()).isEqualTo(policiaId);
        assertThat(vacia.getVehiculoId()).isNull();
    }

    @Test
    void vehiculo_asignado_id_implementa_equals_y_hashcode_por_valor() {
        UUID vehiculoId = UUID.randomUUID();
        UUID policiaId = UUID.randomUUID();

        VehiculoAsignadoId id1 = new VehiculoAsignadoId(vehiculoId, policiaId);
        VehiculoAsignadoId id2 = new VehiculoAsignadoId(vehiculoId, policiaId);
        VehiculoAsignadoId distinto = new VehiculoAsignadoId(UUID.randomUUID(), policiaId);
        VehiculoAsignadoId vacio = new VehiculoAsignadoId();

        assertThat(id1).isEqualTo(id1);
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).hasSameHashCodeAs(id2);
        assertThat(id1).isNotEqualTo(distinto);
        assertThat(id1).isNotEqualTo(null);
        assertThat(id1).isNotEqualTo("otro-tipo");
        assertThat(vacio).isNotNull();
    }
}
