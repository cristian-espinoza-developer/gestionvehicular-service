package ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.FranjaRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FranjaRepositoryAdapter implements FranjaRepositoryPort {

    private final FranjaMantenimientoJpaRepository jpaRepository;

    public FranjaRepositoryAdapter(FranjaMantenimientoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<FranjaMantenimiento> buscarDisponibles(LocalDateTime ahora, int limite) {
        return jpaRepository.buscarDisponibles(ahora, PageRequest.of(0, limite)).stream()
            .map(this::aDominio)
            .toList();
    }

    @Override
    public Optional<FranjaMantenimiento> buscarPorId(UUID franjaId) {
        return jpaRepository.findById(franjaId).map(this::aDominio);
    }

    @Override
    @Transactional
    public boolean reservar(UUID franjaId) {
        return jpaRepository.reservar(franjaId) == 1;
    }

    private FranjaMantenimiento aDominio(FranjaMantenimientoJpaEntity entidad) {
        return new FranjaMantenimiento(entidad.getId(), entidad.getFechaHoraInicio(), entidad.getFechaHoraFin(), entidad.getEstado());
    }
}
