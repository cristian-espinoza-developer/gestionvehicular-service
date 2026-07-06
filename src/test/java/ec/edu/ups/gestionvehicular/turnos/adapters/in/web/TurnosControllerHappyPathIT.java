package ec.edu.ups.gestionvehicular.turnos.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.AgendarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarFranjasDisponiblesUseCase;
import ec.edu.ups.gestionvehicular.turnos.application.port.in.ConsultarTurnoUseCase;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.TurnoVigenteExistenteException;
import ec.edu.ups.gestionvehicular.turnos.domain.exception.VehiculoSinAsignacionException;
import ec.edu.ups.gestionvehicular.turnos.domain.model.EstadoFranja;
import ec.edu.ups.gestionvehicular.turnos.domain.model.FranjaMantenimiento;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Turno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TurnosController.class, TurnosExceptionHandler.class})
class TurnosControllerHappyPathIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AgendarTurnoUseCase agendarTurnoUseCase;

    @MockitoBean
    private ConsultarFranjasDisponiblesUseCase consultarFranjasDisponiblesUseCase;

    @MockitoBean
    private ConsultarTurnoUseCase consultarTurnoUseCase;

    private static final UUID POLICIA_ID = UUID.randomUUID();
    private static final UUID VEHICULO_ID = UUID.randomUUID();
    private static final UUID FRANJA_ID = UUID.randomUUID();

    @Test
    void debe_listar_franjas_disponibles() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        FranjaMantenimiento franja = new FranjaMantenimiento(FRANJA_ID, ahora.plusDays(1), ahora.plusDays(1).plusHours(1), EstadoFranja.DISPONIBLE);
        when(consultarFranjasDisponiblesUseCase.consultarDisponibles(POLICIA_ID, VEHICULO_ID)).thenReturn(List.of(franja));

        mockMvc.perform(get("/vehiculos/{vehiculoId}/franjas-disponibles", VEHICULO_ID)
                .header("X-Policia-Id", POLICIA_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.franjas[0].id").value(FRANJA_ID.toString()));
    }

    @Test
    void debe_agendar_un_turno_y_devolver_201() throws Exception {
        Turno turno = Turno.agendar(UUID.randomUUID(), VEHICULO_ID, POLICIA_ID, FRANJA_ID, LocalDateTime.now());
        when(agendarTurnoUseCase.agendar(POLICIA_ID, VEHICULO_ID, FRANJA_ID)).thenReturn(turno);

        String cuerpo = objectMapper.writeValueAsString(java.util.Map.of("vehiculoId", VEHICULO_ID, "franjaId", FRANJA_ID));

        mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("AGENDADO"))
            .andExpect(jsonPath("$.vehiculoId").value(VEHICULO_ID.toString()));
    }

    @Test
    void debe_consultar_un_turno_agendado() throws Exception {
        UUID turnoId = UUID.randomUUID();
        Turno turno = Turno.agendar(turnoId, VEHICULO_ID, POLICIA_ID, FRANJA_ID, LocalDateTime.now());
        when(consultarTurnoUseCase.consultarPorId(turnoId)).thenReturn(turno);

        mockMvc.perform(get("/turnos/{turnoId}", turnoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(turnoId.toString()))
            .andExpect(jsonPath("$.estado").value("AGENDADO"));
    }

    @Test
    void debe_devolver_403_si_el_vehiculo_no_esta_asignado_al_policia() throws Exception {
        when(agendarTurnoUseCase.agendar(any(), any(), any())).thenThrow(new VehiculoSinAsignacionException(VEHICULO_ID));

        String cuerpo = objectMapper.writeValueAsString(java.util.Map.of("vehiculoId", VEHICULO_ID, "franjaId", FRANJA_ID));

        mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andExpect(status().isForbidden());
    }

    @Test
    void debe_devolver_200_con_lista_vacia_cuando_no_hay_franjas_disponibles() throws Exception {
        when(consultarFranjasDisponiblesUseCase.consultarDisponibles(POLICIA_ID, VEHICULO_ID)).thenReturn(List.of());

        mockMvc.perform(get("/vehiculos/{vehiculoId}/franjas-disponibles", VEHICULO_ID)
                .header("X-Policia-Id", POLICIA_ID.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.franjas").isEmpty());
    }

    @Test
    void debe_devolver_422_si_ya_existe_turno_vigente() throws Exception {
        when(agendarTurnoUseCase.agendar(any(), any(), any())).thenThrow(new TurnoVigenteExistenteException(VEHICULO_ID));

        String cuerpo = objectMapper.writeValueAsString(java.util.Map.of("vehiculoId", VEHICULO_ID, "franjaId", FRANJA_ID));

        mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andExpect(status().isUnprocessableEntity());
    }
}
