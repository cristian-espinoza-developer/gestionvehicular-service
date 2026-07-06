package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.TurnoRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoTurno;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TurnoRepositoryAdapter implements TurnoRepositoryPort {

    private final TurnoJpaRepository jpaRepository;

    public TurnoRepositoryAdapter(TurnoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Turno guardar(Turno turno) {
        TurnoJpaEntity guardado = jpaRepository.save(new TurnoJpaEntity(
            turno.getId(),
            turno.getVehiculoId(),
            turno.getPoliciaId(),
            turno.getFranjaId(),
            turno.getEstado(),
            turno.getFechaCreacion()
        ));
        return aDominio(guardado);
    }

    @Override
    public Optional<Turno> buscarPorId(UUID turnoId) {
        return jpaRepository.findById(turnoId).map(this::aDominio);
    }

    @Override
    public boolean existeTurnoVigentePara(UUID vehiculoId) {
        return jpaRepository.existsByVehiculoIdAndEstado(vehiculoId, EstadoTurno.AGENDADO);
    }

    private Turno aDominio(TurnoJpaEntity entidad) {
        return new Turno(
            entidad.getId(),
            entidad.getVehiculoId(),
            entidad.getPoliciaId(),
            entidad.getFranjaId(),
            entidad.getEstado(),
            entidad.getFechaCreacion()
        );
    }
}
