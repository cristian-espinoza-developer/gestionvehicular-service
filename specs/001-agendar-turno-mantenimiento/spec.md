# Feature Specification: Autoagendar turno de mantenimiento

**Feature Branch**: `001-agendar-turno-mantenimiento`

**Created**: 2026-07-05

**Status**: Draft

**Input**: User description: "US-02 · Autoagendar turno de mantenimiento · épica E-01 · 5 pts — Como policía, quiero agendar mi propio turno de mantenimiento eligiendo una fecha y hora disponible en el sistema, para no tener que llamar al mecánico ni ir sin cita. Dado que el policía accede al sistema con su vehículo asignado, cuando selecciona una franja disponible y confirma, entonces el turno queda registrado y el mecánico y el encargado reciben una notificación. Dado que el policía intenta agendar en una franja ya ocupada, cuando intenta confirmar, entonces el sistema muestra las próximas franjas disponibles en lugar de bloquear sin orientación."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Agendar un turno en una franja disponible (Priority: P1)

Un policía con un vehículo asignado ingresa al sistema, consulta las franjas de
mantenimiento disponibles, elige la que más le convenga y confirma. El turno
queda registrado sin necesidad de llamar al mecánico o presentarse sin cita.

**Why this priority**: Es el flujo principal de la historia de usuario: sin
esta capacidad no existe autoagendamiento y el policía sigue dependiendo de
coordinación manual por teléfono.

**Independent Test**: Puede probarse por completo iniciando sesión como
policía con un vehículo asignado, seleccionando una franja disponible y
confirmando; se verifica que el turno aparece registrado y que el mecánico y
el encargado reciben la notificación correspondiente.

**Acceptance Scenarios**:

1. **Given** el policía accede al sistema con su vehículo asignado y existen
   franjas disponibles, **When** selecciona una franja disponible y confirma,
   **Then** el turno queda registrado en el sistema asociado a su vehículo, y
   el mecánico y el encargado reciben una notificación de la creación del
   turno.
2. **Given** el policía ha confirmado un turno, **When** consulta su turno
   agendado, **Then** el sistema muestra la fecha, hora y estado ("agendado")
   del turno confirmado.

---

### User Story 2 - Orientación ante una franja ya ocupada (Priority: P2)

Un policía intenta confirmar una franja que otro usuario reservó momentos
antes. En vez de recibir solo un mensaje de bloqueo, el sistema le muestra de
inmediato las próximas franjas disponibles para que pueda continuar con el
agendamiento sin salir del flujo ni tener que empezar de nuevo la búsqueda.

**Why this priority**: Evita que el policía quede varado ante un conflicto de
concurrencia (dos personas eligiendo la misma franja); sin esto, la
experiencia de autoagendamiento se percibe como frágil y puede empujar al
usuario de vuelta a la coordinación telefónica que la historia busca eliminar.

**Independent Test**: Puede probarse provocando que dos solicitudes confirmen
la misma franja; la segunda solicitud debe rechazarse y el sistema debe
responder con una lista de próximas franjas disponibles en lugar de un error
sin alternativas.

**Acceptance Scenarios**:

1. **Given** una franja de mantenimiento ya fue reservada por otro usuario,
   **When** el policía intenta confirmar esa misma franja, **Then** el
   sistema rechaza la confirmación e inmediatamente muestra las próximas
   franjas disponibles para que el policía elija una de ellas.
2. **Given** el sistema mostró franjas alternativas tras un conflicto de
   reserva, **When** el policía selecciona una de las franjas alternativas y
   confirma, **Then** el turno queda registrado normalmente como en el flujo
   principal (incluyendo notificación al mecánico y al encargado).

---

### Edge Cases

- ¿Qué ocurre si el policía no tiene ningún vehículo asignado en el sistema?
  El sistema MUST impedir el agendamiento y explicar que se requiere un
  vehículo asignado.
- ¿Qué ocurre si no existe ninguna franja disponible en el horizonte de
  agendamiento (todas ocupadas o fuera de horario)? El sistema MUST informar
  claramente que no hay franjas disponibles en el rango consultado, sin
  presentar una lista vacía sin explicación.
- ¿Qué ocurre si el policía ya tiene un turno vigente sin completar para su
  vehículo? El sistema MUST advertir de la existencia de un turno vigente
  antes de permitir agendar uno adicional para el mismo vehículo.
- ¿Qué ocurre si la notificación al mecánico o al encargado no puede
  enviarse? El turno MUST quedar registrado igualmente y el sistema MUST
  dejar constancia de que la notificación quedó pendiente de reintento, sin
  revertir el agendamiento ya confirmado.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir a un policía con vehículo asignado
  consultar las franjas de mantenimiento disponibles (fecha y hora) antes de
  agendar.
- **FR-002**: El sistema MUST permitir al policía seleccionar una franja
  disponible y confirmarla para crear un turno de mantenimiento asociado a su
  vehículo asignado.
- **FR-003**: El sistema MUST registrar el turno confirmado con estado
  "agendado", incluyendo vehículo, policía solicitante, fecha y hora de la
  franja.
- **FR-004**: El sistema MUST notificar al mecánico y al encargado cada vez
  que se registra un nuevo turno.
- **FR-005**: El sistema MUST impedir que dos turnos distintos queden
  registrados sobre la misma franja (no se permite doble reserva).
- **FR-006**: Cuando una confirmación falla porque la franja ya fue tomada,
  el sistema MUST responder mostrando las próximas franjas disponibles en
  lugar de únicamente un mensaje de error.
- **FR-007**: El sistema MUST restringir el agendamiento a policías que
  tengan un vehículo asignado; un policía sin vehículo asignado no puede
  crear un turno.
- **FR-008**: El sistema MUST mostrar únicamente franjas futuras (no
  vencidas) como disponibles para agendar.
- **FR-009**: El sistema MUST advertir al policía si ya existe un turno
  vigente (no completado) para su vehículo antes de permitir agendar uno
  adicional.

### Key Entities *(include if feature involves data)*

- **Turno de mantenimiento**: representa una cita agendada; atributos clave:
  vehículo asociado, policía solicitante, franja (fecha/hora), estado
  (agendado, completado, cancelado), fecha de creación.
- **Franja disponible**: representa un bloque de tiempo ofrecido para
  mantenimiento; atributos clave: fecha/hora de inicio y fin, estado
  (disponible/ocupada).
- **Vehículo asignado**: vehículo institucional vinculado a un policía;
  atributo clave usado aquí: policía responsable actual.
- **Notificación**: mensaje enviado al mecánico y al encargado cuando se crea
  un turno; atributos clave: destinatario (mecánico/encargado), turno
  asociado, estado de envío.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un policía con vehículo asignado puede completar el
  agendamiento de un turno (desde consultar franjas hasta confirmar) en
  menos de 2 minutos.
- **SC-002**: El 100% de los turnos confirmados exitosamente generan una
  notificación visible para el mecánico y para el encargado.
- **SC-003**: El 0% de los turnos registrados corresponden a franjas
  duplicadas (doble reserva sobre la misma franja).
- **SC-004**: Cuando ocurre un conflicto de franja ya ocupada, el sistema
  presenta franjas alternativas en el mismo intento, sin que el policía
  tenga que reiniciar la búsqueda desde cero.
- **SC-005**: El 90% de los policías que enfrentan una franja ocupada logran
  completar el agendamiento con una franja alternativa en el mismo intento
  de uso.

## Assumptions

- Las franjas de mantenimiento disponibles son definidas y mantenidas por
  fuera del alcance de esta historia (por el encargado o el mecánico); esta
  historia solo cubre la consulta y reserva de franjas ya existentes.
- Cada policía tiene, en un momento dado, un único vehículo institucional
  asignado, y solo puede agendar turnos para ese vehículo.
- Un turno agendado puede quedar en estado "agendado" y avanzar a
  "completado"; la cancelación o reprogramación de turnos no forma parte del
  alcance de esta historia.
- El canal y formato exacto de la notificación (correo, notificación
  in-app, etc.) no está definido por esta historia; solo se exige que el
  mecánico y el encargado reciban aviso del nuevo turno.
- Se asume una única franja por turno (no se agendan múltiples franjas en
  una sola confirmación).
