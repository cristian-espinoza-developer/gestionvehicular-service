package ec.edu.ups.gestionvehicular.turnos.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.AgendarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarFranjasDisponiblesUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.FranjaNoDisponibleException;
import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TurnosController.class, TurnosExceptionHandler.class})
class TurnosControllerConflictIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendarTurnoUseCase agendarTurnoUseCase;

    @MockitoBean
    private ConsultarFranjasDisponiblesUseCase consultarFranjasDisponiblesUseCase;

    @MockitoBean
    private ConsultarTurnoUseCase consultarTurnoUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID POLICIA_ID = UUID.randomUUID();
    private static final UUID VEHICULO_ID = UUID.randomUUID();
    private static final UUID FRANJA_OCUPADA_ID = UUID.randomUUID();

    @Test
    void debe_devolver_409_con_proximas_franjas_disponibles_cuando_la_franja_ya_fue_reservada() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        FranjaMantenimiento alternativa = new FranjaMantenimiento(UUID.randomUUID(), ahora.plusDays(2), ahora.plusDays(2).plusHours(1), EstadoFranja.DISPONIBLE);
        when(agendarTurnoUseCase.agendar(any(), any(), any()))
            .thenThrow(new FranjaNoDisponibleException(List.of(alternativa)));

        String cuerpo = objectMapper.writeValueAsString(Map.of("vehiculoId", VEHICULO_ID, "franjaId", FRANJA_OCUPADA_ID));

        mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.proximasFranjasDisponibles[0].id").value(alternativa.getId().toString()));
    }
}
