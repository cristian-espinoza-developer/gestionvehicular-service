# Data Model: Autoagendar turno de mantenimiento

## Entidades del dominio

### FranjaMantenimiento

Bloque de tiempo ofrecido para mantenimiento. Gestionada externamente a
esta historia (ver Assumptions en `spec.md`), pero persistida por este
módulo al ser el primer feature del proyecto.

| Campo | Tipo | Reglas |
|---|---|---|
| id | identificador único | generado por el sistema |
| fechaHoraInicio | fecha/hora | obligatorio; debe ser futura para considerarse "disponible" (FR-008) |
| fechaHoraFin | fecha/hora | obligatorio; posterior a `fechaHoraInicio` |
| estado | `DISPONIBLE` \| `OCUPADA` | obligatorio; transiciona a `OCUPADA` cuando un `Turno` la reserva exitosamente |

**Invariantes**:
- Una franja `OCUPADA` no puede volver a reservarse (FR-005).
- Solo franjas `DISPONIBLE` con `fechaHoraInicio` futura se listan como
  disponibles (FR-001, FR-008).
- La transición `DISPONIBLE → OCUPADA` se ejecuta como un
  `UPDATE ... WHERE id = ? AND estado = 'DISPONIBLE'`; si la actualización
  afecta 0 filas, se interpreta como conflicto de reserva (lanza
  `FranjaNoDisponibleException`). Esto hace atómica la operación sin
  necesitar una transacción `SERIALIZABLE` (ver `research.md` §8).

### Turno

Cita de mantenimiento agendada por un policía para su vehículo asignado.

| Campo | Tipo | Reglas |
|---|---|---|
| id | identificador único | generado por el sistema |
| vehiculoId | referencia a vehículo asignado | obligatorio (FR-002, FR-003) |
| policiaId | referencia al solicitante | obligatorio |
| franjaId | referencia a `FranjaMantenimiento` | obligatorio; única por turno (una franja = un turno) |
| estado | `AGENDADO` \| `COMPLETADO` | obligatorio; inicia en `AGENDADO` (FR-003) |
| fechaCreacion | fecha/hora | asignada al confirmar el turno |

**Invariantes**:
- No pueden existir dos `Turno` con el mismo `franjaId` (FR-005): además
  del `UPDATE` condicional de `FranjaMantenimiento`, la columna
  `franja_id` de `turno` lleva una restricción `UNIQUE` como segunda
  barrera de integridad (ver `research.md` §8).
- Un vehículo con un `Turno` en estado `AGENDADO` se considera "con turno
  vigente" (FR-009, Edge Case) y debe advertirse antes de permitir uno
  nuevo para el mismo vehículo.

**Fuera de alcance (documentado, no implementado en esta feature)**:
- Estado `CANCELADO` y transición de cancelación/reprogramación: excluidos
  por las Assumptions de `spec.md` (YAGNI, Principio III de la
  constitución). Si se requieren en el futuro, se añadirán en una historia
  posterior sin romper el modelo actual (el campo `estado` es un enum
  extensible).

### VehiculoAsignado (referencia mínima)

Representa qué vehículo institucional tiene asignado un policía en un
momento dado. Modelo mínimo de solo lectura para esta feature; el módulo de
gestión de flota completo queda fuera de alcance (ver `research.md` §6).

| Campo | Tipo | Reglas |
|---|---|---|
| vehiculoId | identificador único | obligatorio |
| policiaId | identificador del policía | obligatorio; un policía tiene a lo sumo un vehículo asignado vigente |

### Notificacion

Registro del aviso enviado al mecánico y al encargado cuando se crea un
turno (FR-004).

| Campo | Tipo | Reglas |
|---|---|---|
| id | identificador único | generado por el sistema |
| turnoId | referencia a `Turno` | obligatorio |
| destinatarioTipo | `MECANICO` \| `ENCARGADO` | obligatorio; se genera una `Notificacion` por cada destinatario |
| estadoEnvio | `ENVIADA` \| `PENDIENTE` | `PENDIENTE` si el envío falla; el turno permanece confirmado igualmente (Edge Case) |
| fechaEnvio | fecha/hora | fecha del intento de envío |

## Relaciones

```text
VehiculoAsignado (1) ──< (0..N) Turno
FranjaMantenimiento (1) ──< (0..1) Turno   [una franja reservada tiene exactamente un turno]
Turno (1) ──< (2) Notificacion             [una por MECANICO, una por ENCARGADO]
```

## Transiciones de estado

**Turno**: `AGENDADO → COMPLETADO` (fuera del alcance operativo de esta
historia; el campo existe para soportar el Edge Case de "turno vigente",
pero el trigger de finalización no se implementa en este US).

**FranjaMantenimiento**: `DISPONIBLE → OCUPADA` (disparado exclusivamente
por la confirmación exitosa de un `Turno`, de forma atómica con su
creación, para satisfacer FR-005).

## Esquema y datos precargados

El DDL de las cuatro tablas (`franja_mantenimiento`, `turno`,
`vehiculo_asignado`, `notificacion`) se versiona explícitamente en
`src/main/resources/db/schema.sql` (ver `plan.md` § Project Structure y
`research.md` §5), en vez de dejarlo a la generación automática de
Hibernate. `src/main/resources/db/data.sql` precarga:

- Un `vehiculo_asignado` de ejemplo (vehículo + policía) para poder ejecutar
  `quickstart.md` sin pasos manuales previos.
- Varias `franja_mantenimiento` en estado `DISPONIBLE` con fechas futuras,
  suficientes para validar tanto el Escenario 1 (agendar en franja libre)
  como el Escenario 2 (conflicto + próximas franjas alternativas).

Ambos scripts se ejecutan automáticamente al arrancar la aplicación
(`spring.sql.init.mode=always`).
