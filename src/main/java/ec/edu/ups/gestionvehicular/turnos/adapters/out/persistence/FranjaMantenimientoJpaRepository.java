package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FranjaMantenimientoJpaRepository extends JpaRepository<FranjaMantenimientoJpaEntity, UUID> {

    @Query("SELECT f FROM FranjaMantenimientoJpaEntity f "
        + "WHERE f.estado = ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja.DISPONIBLE "
        + "AND f.fechaHoraInicio > :ahora ORDER BY f.fechaHoraInicio ASC")
    List<FranjaMantenimientoJpaEntity> buscarDisponibles(@Param("ahora") LocalDateTime ahora, Pageable pageable);

    @Modifying
    @Query("UPDATE FranjaMantenimientoJpaEntity f SET f.estado = "
        + "ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja.OCUPADA "
        + "WHERE f.id = :id AND f.estado = ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja.DISPONIBLE")
    int reservar(@Param("id") UUID id);
}
