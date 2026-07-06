package ec.edu.ups.gestionvehicular.turnos.application;

import ec.edu.ups.gestionvehicular.turnos.application.port.out.TurnoRepositoryPort;
import ec.edu.ups.gestionvehicular.turnos.application.service.ConsultarTurnoService;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarTurnoServiceTest {

    @Mock
    private TurnoRepositoryPort turnoRepositoryPort;

    private ConsultarTurnoService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarTurnoService(turnoRepositoryPort);
    }

    @Test
    void debe_devolver_el_turno_cuando_existe() {
        UUID turnoId = UUID.randomUUID();
        Turno turno = Turno.agendar(turnoId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());
        when(turnoRepositoryPort.buscarPorId(turnoId)).thenReturn(Optional.of(turno));

        Turno resultado = service.consultarPorId(turnoId);

        assertThat(resultado).isEqualTo(turno);
    }

    @Test
    void debe_lanzar_excepcion_cuando_el_turno_no_existe() {
        UUID turnoId = UUID.randomUUID();
        when(turnoRepositoryPort.buscarPorId(turnoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarPorId(turnoId))
            .isInstanceOf(NoSuchElementException.class);
    }
}
