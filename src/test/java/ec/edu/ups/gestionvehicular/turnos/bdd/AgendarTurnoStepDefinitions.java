package ec.edu.ups.gestionvehicular.turnos.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence.NotificacionJpaEntity;
import ec.edu.ups.gestionvehicular.turnos.adapters.out.persistence.NotificacionJpaRepository;
import ec.edu.ups.gestionvehicular.turnos.domain.model.Notificacion.DestinatarioTipo;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AgendarTurnoStepDefinitions {

    private static final UUID VEHICULO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID POLICIA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OTRO_VEHICULO_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID OTRO_POLICIA_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID VEHICULO_ID_ESCENARIO_2 = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID POLICIA_ID_ESCENARIO_2 = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacionJpaRepository notificacionJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID franjaSeleccionada;
    private MvcResult respuestaAgendar;
    private UUID turnoCreadoId;
    private UUID franjaYaOcupada;
    private UUID franjaAlternativa;

    @Dado("que el policía accede al sistema con su vehículo asignado y existen franjas disponibles")
    public void dado_que_el_policia_accede_con_franjas_disponibles() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/vehiculos/{vehiculoId}/franjas-disponibles", VEHICULO_ID)
                .header("X-Policia-Id", POLICIA_ID.toString()))
            .andReturn();

        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(json.get("franjas").size()).isGreaterThan(0);
        franjaSeleccionada = UUID.fromString(json.get("franjas").get(0).get("id").asText());
    }

    @Cuando("selecciona una franja disponible y confirma")
    public void cuando_selecciona_una_franja_y_confirma() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of("vehiculoId", VEHICULO_ID, "franjaId", franjaSeleccionada));

        respuestaAgendar = mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andReturn();
    }

    @Entonces("el turno queda registrado en el sistema asociado a su vehículo")
    public void entonces_el_turno_queda_registrado() throws Exception {
        assertThat(respuestaAgendar.getResponse().getStatus()).isEqualTo(201);

        JsonNode json = objectMapper.readTree(respuestaAgendar.getResponse().getContentAsString());
        assertThat(json.get("vehiculoId").asText()).isEqualTo(VEHICULO_ID.toString());
        assertThat(json.get("estado").asText()).isEqualTo("AGENDADO");
        turnoCreadoId = UUID.fromString(json.get("id").asText());
    }

    @Entonces("el mecánico y el encargado reciben una notificación de la creación del turno")
    public void entonces_mecanico_y_encargado_reciben_notificacion() {
        List<NotificacionJpaEntity> notificaciones = notificacionJpaRepository.findAll().stream()
            .filter(n -> n.getTurnoId().equals(turnoCreadoId))
            .toList();

        assertThat(notificaciones).hasSize(2);
        assertThat(notificaciones)
            .extracting(NotificacionJpaEntity::getDestinatarioTipo)
            .containsExactlyInAnyOrder(DestinatarioTipo.MECANICO, DestinatarioTipo.ENCARGADO);
    }

    @Dado("que una franja de mantenimiento ya fue reservada por otro usuario")
    public void dado_que_una_franja_ya_fue_reservada_por_otro_usuario() throws Exception {
        MvcResult disponibles = mockMvc.perform(get("/vehiculos/{vehiculoId}/franjas-disponibles", OTRO_VEHICULO_ID)
                .header("X-Policia-Id", OTRO_POLICIA_ID.toString()))
            .andReturn();
        JsonNode json = objectMapper.readTree(disponibles.getResponse().getContentAsString());
        franjaYaOcupada = UUID.fromString(json.get("franjas").get(0).get("id").asText());

        String cuerpo = objectMapper.writeValueAsString(Map.of("vehiculoId", OTRO_VEHICULO_ID, "franjaId", franjaYaOcupada));
        MvcResult reserva = mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", OTRO_POLICIA_ID.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andReturn();
        assertThat(reserva.getResponse().getStatus()).isEqualTo(201);
    }

    @Cuando("el policía intenta confirmar esa misma franja")
    public void cuando_el_policia_intenta_confirmar_esa_misma_franja() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of("vehiculoId", VEHICULO_ID_ESCENARIO_2, "franjaId", franjaYaOcupada));
        respuestaAgendar = mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID_ESCENARIO_2.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andReturn();
    }

    @Entonces("el sistema rechaza la confirmación e inmediatamente muestra las próximas franjas disponibles")
    public void entonces_el_sistema_rechaza_y_muestra_alternativas() throws Exception {
        assertThat(respuestaAgendar.getResponse().getStatus()).isEqualTo(409);

        JsonNode json = objectMapper.readTree(respuestaAgendar.getResponse().getContentAsString());
        assertThat(json.get("proximasFranjasDisponibles").size()).isGreaterThan(0);
        franjaAlternativa = UUID.fromString(json.get("proximasFranjasDisponibles").get(0).get("id").asText());
    }

    @Cuando("el policía selecciona una de las franjas alternativas y confirma")
    public void cuando_el_policia_selecciona_una_alternativa_y_confirma() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of("vehiculoId", VEHICULO_ID_ESCENARIO_2, "franjaId", franjaAlternativa));
        respuestaAgendar = mockMvc.perform(post("/turnos")
                .header("X-Policia-Id", POLICIA_ID_ESCENARIO_2.toString())
                .contentType("application/json")
                .content(cuerpo))
            .andReturn();
    }

    @Entonces("el turno queda registrado normalmente como en el flujo principal")
    public void entonces_el_turno_queda_registrado_normalmente() throws Exception {
        assertThat(respuestaAgendar.getResponse().getStatus()).isEqualTo(201);

        JsonNode json = objectMapper.readTree(respuestaAgendar.getResponse().getContentAsString());
        assertThat(json.get("franjaId").asText()).isEqualTo(franjaAlternativa.toString());
        assertThat(json.get("estado").asText()).isEqualTo("AGENDADO");
    }
}
