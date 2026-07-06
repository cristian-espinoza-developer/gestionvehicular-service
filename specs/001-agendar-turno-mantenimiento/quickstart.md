# Quickstart: Autoagendar turno de mantenimiento

Guía de validación end-to-end para US-02. Referencias: [data-model.md](./data-model.md),
[contracts/openapi.yaml](./contracts/openapi.yaml).

## Prerrequisitos

- JDK 25 instalado (o toolchain resuelto automáticamente por Gradle).
- Sin servicios externos: el proyecto usa H2 en memoria.

## Levantar el servicio

```bash
./gradlew bootRun
```

La aplicación queda disponible en `http://localhost:8080`.

## Ejecutar las pruebas del feature

```bash
# Unitarias + integración + funcionales (Cucumber) + verificación JaCoCo
./gradlew check

# Solo el reporte de cobertura HTML
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Escenario 1: agendar un turno en una franja disponible (US1)

1. Consultar franjas disponibles para el vehículo asignado:

   ```bash
   curl -s http://localhost:8080/api/v1/vehiculos/{vehiculoId}/franjas-disponibles
   ```

   **Resultado esperado**: `200 OK` con al menos una franja en estado
   implícito "disponible" (`franjas: [...]`).

2. Confirmar el turno sobre una de las franjas devueltas:

   ```bash
   curl -s -X POST http://localhost:8080/api/v1/turnos \
     -H "Content-Type: application/json" \
     -d '{"vehiculoId":"<vehiculoId>","franjaId":"<franjaId>"}'
   ```

   **Resultado esperado**: `201 Created` con el turno en estado `AGENDADO`.
   Se debe poder verificar (vía logs de la aplicación o el registro de
   `Notificacion` en la base H2) que se generó una notificación para
   `MECANICO` y otra para `ENCARGADO`.

3. Consultar el turno creado:

   ```bash
   curl -s http://localhost:8080/api/v1/turnos/{turnoId}
   ```

   **Resultado esperado**: `200 OK` con los mismos datos confirmados en el
   paso 2.

## Escenario 2: franja ya ocupada devuelve alternativas (US2)

1. Repetir el paso 2 del Escenario 1 usando el mismo `franjaId` ya
   reservado (simulando una segunda solicitud concurrente).

   **Resultado esperado**: `409 Conflict` con un cuerpo que incluye
   `proximasFranjasDisponibles` (no vacío si existen otras franjas futuras
   disponibles), en vez de un mensaje de error sin alternativas.

2. Confirmar el turno usando una de las franjas alternativas devueltas.

   **Resultado esperado**: `201 Created`, igual que en el Escenario 1.

## Validación automatizada equivalente

Los mismos dos escenarios están codificados como Gherkin en
`src/test/resources/features/agendar_turno_mantenimiento.feature` y se
ejecutan como parte de `./gradlew check` (Cucumber + `@SpringBootTest`).
