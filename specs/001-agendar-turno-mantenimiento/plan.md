# Implementation Plan: Autoagendar turno de mantenimiento

**Branch**: `001-agendar-turno-mantenimiento` | **Date**: 2026-07-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-agendar-turno-mantenimiento/spec.md`

## Summary

Permitir que un policía con vehículo asignado consulte franjas de
mantenimiento disponibles y agende un turno por sí mismo, notificando al
mecánico y al encargado, y ofreciendo franjas alternativas de inmediato
cuando la franja elegida ya fue tomada. Se implementa como un nuevo módulo
"turnos" siguiendo Arquitectura Limpia dentro del monolito Spring Boot
existente, con un contrato OpenAPI como fuente de verdad para el endpoint
REST, casos de uso desacoplados de Spring/JPA, y pruebas BDD en los tres
niveles exigidos por la constitución (unitarias, integración, funcionales).

## Technical Context

**Language/Version**: Java 25 (toolchain Gradle ya configurado)

**Primary Dependencies**: Spring Boot 4.1.0 (`spring-boot-starter-webmvc`,
`spring-boot-starter-data-jpa`), Lombok, `openapi-generator-gradle-plugin`
(genera interfaces de controller y DTOs desde `contracts/openapi.yaml`),
Cucumber-JVM (`cucumber-junit-platform-engine`, `cucumber-spring`) para las
pruebas funcionales BDD, JUnit 5 + Mockito + AssertJ para unitarias e
integración.

**Storage**: Relacional vía Spring Data JPA; H2 para desarrollo/pruebas (ya
presente en el proyecto). No se define aún un motor productivo distinto;
se asume H2/JPA como suficiente para esta feature (documentado en
`research.md`). El esquema (DDL) y los datos precargados (franjas de
mantenimiento y vehículo asignado de ejemplo) se versionan como scripts SQL
en `src/main/resources/db/` (`schema.sql` + `data.sql`), en vez de dejar el
esquema a la generación automática de Hibernate.

**Testing**: JUnit 5 + Mockito (unitarias sobre dominio/casos de uso),
`@DataJpaTest` / `@WebMvcTest` (integración sobre adaptadores JPA y web),
Cucumber-JVM con `@SpringBootTest` (funcionales/aceptación, un escenario
Gherkin por cada Acceptance Scenario de `spec.md`). JaCoCo mide cobertura de
los tres niveles.

**Target Platform**: Servicio backend Spring Boot (Tomcat embebido),
desplegable como aplicación Linux/contenedor.

**Project Type**: Web service (proyecto único, API REST).

**Performance Goals**: Sin metas de carga especificadas por el negocio; se
adopta como línea base razonable p95 < 500 ms para las operaciones de
consulta de franjas y confirmación de turno, acorde a un uso institucional
de baja concurrencia (una comisaría/flota, no tráfico masivo).

**Constraints**: Debe cumplir la constitución del proyecto: Arquitectura
Limpia, BDD en 3 niveles, SOLID/YAGNI/DRY, API-First con OpenAPI +
`openapi-generator`, cobertura JaCoCo > 80% por clase y ≥ 80% global.

**Scale/Scope**: Escala de una flota institucional (decenas de vehículos,
pocas decenas de turnos/día); un único endpoint de consulta y uno de
confirmación de turno para esta historia.

**Identidad del solicitante**: Se introduce un `PoliciaIdentityPort` mínimo
que resuelve el `policiaId` desde el encabezado HTTP `X-Policia-Id` de cada
request; el adaptador web valida, antes de despachar al caso de uso, que
ese `policiaId` corresponda al `vehiculoId` recibido (vía
`VehiculoAsignadoPort`). No se implementa autenticación real (JWT/sesión)
en esta historia (FR-010) — ver `research.md` §7.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación | Estado |
|---|---|---|
| I. Clean Architecture | El módulo `turnos` se organiza en `domain` / `application` (puertos + servicios) / `adapters` (in/web, out/persistence) / frameworks (Spring config). Dominio y casos de uso no importan Spring/JPA. | PASS |
| II. BDD Testing Discipline | Se planifican los 3 niveles: unit (dominio/casos de uso con dobles de prueba), integración (`@DataJpaTest`/`@WebMvcTest`), funcional (Cucumber, 1 feature file con los 2 escenarios Gherkin de la spec). | PASS |
| III. SOLID/YAGNI/DRY | Un puerto por responsabilidad (repositorio de turnos, de franjas, notificador, consulta de vehículo asignado); no se agregan estados/transiciones (p. ej. cancelación) no requeridos por la spec actual. | PASS |
| IV. API-First con OpenAPI | `contracts/openapi.yaml` se define en Fase 1 antes de cualquier código de controller; `openapi-generator` generará las interfaces de servidor y DTOs a partir de ese contrato. | PASS |
| V. Cobertura JaCoCo | Se añadirá el plugin `jacoco` y `jacocoTestCoverageVerification` (umbral >80% por clase, ≥80% global) como tarea de setup de esta feature, dado que el `build.gradle` actual aún no lo declara. | PASS (acción de setup, no violación) |

No se identifican violaciones que requieran justificación en
`Complexity Tracking`.

## Project Structure

### Documentation (this feature)

```text
specs/001-agendar-turno-mantenimiento/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/
│   └── openapi.yaml     # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/ec/edu/ups/gestionvehicular/
├── GestionvehicularServiceApplication.java
└── turnos/
    ├── domain/
    │   ├── model/            # Turno, FranjaMantenimiento, EstadoTurno, EstadoFranja
    │   └── exception/         # FranjaNoDisponibleException, VehiculoSinAsignacionException, etc.
    ├── application/
    │   ├── port/in/           # AgendarTurnoUseCase, ConsultarFranjasDisponiblesUseCase, ConsultarTurnoUseCase
    │   ├── port/out/          # TurnoRepositoryPort, FranjaRepositoryPort, NotificadorTurnoPort, VehiculoAsignadoPort
    │   └── service/           # AgendarTurnoService, ConsultarFranjasDisponiblesService, ConsultarTurnoService
    └── adapters/
        ├── in/web/            # TurnosController (implementa interfaz generada por openapi-generator), mappers DTO<->dominio
        └── out/
            ├── persistence/   # Entidades JPA, Spring Data Repositories, adaptadores que implementan los port/out de persistencia
            └── notification/  # Adaptador que implementa NotificadorTurnoPort

src/main/resources/
├── openapi/
│   └── (contrato copiado/consumido por el plugin openapi-generator en build time)
└── db/
    ├── schema.sql          # DDL de franja_mantenimiento, turno, vehiculo_asignado, notificacion
    └── data.sql            # Datos precargados: vehículo(s) asignado(s) y franjas de mantenimiento de ejemplo

src/test/java/ec/edu/ups/gestionvehicular/turnos/
├── domain/                    # unit tests de entidades y reglas de negocio
├── application/                # unit tests de casos de uso con Mockito (dobles de los port/out)
├── adapters/
│   ├── in/web/                 # integration tests @WebMvcTest del controller
│   └── out/persistence/        # integration tests @DataJpaTest de los adaptadores JPA
└── bdd/
    ├── AgendarTurnoRunner.java     # runner Cucumber-JUnit-Platform
    └── AgendarTurnoStepDefinitions.java

src/test/resources/features/
└── agendar_turno_mantenimiento.feature   # escenarios Gherkin de spec.md (US1 y US2)
```

**Structure Decision**: Proyecto único (Gradle, ya existente) con el módulo
de negocio `turnos` organizado por capas de Arquitectura Limpia dentro del
mismo árbol `src/main/java`, siguiendo el paquete base
`ec.edu.ups.gestionvehicular` ya definido en `build.gradle`. Se elige esta
opción (monolito modular por capas) en vez de módulos Gradle separados
porque el proyecto es de escala pequeña (una sola épica activa) y separar
en subproyectos Gradle hoy violaría YAGNI (Principio III).

## Complexity Tracking

*Sin violaciones que justificar; tabla omitida intencionalmente.*
