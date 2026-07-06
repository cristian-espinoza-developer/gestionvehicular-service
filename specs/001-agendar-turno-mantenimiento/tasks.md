---

description: "Task list for Autoagendar turno de mantenimiento"
---

# Tasks: Autoagendar turno de mantenimiento

**Input**: Design documents from `specs/001-agendar-turno-mantenimiento/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Incluidas y obligatorias — la constitución del proyecto (Principio
II, BDD Testing Discipline) exige pruebas unitarias, de integración y
funcionales/BDD para todo comportamiento nuevo; no son opcionales en este
feature.

**Organization**: Tasks agrupadas por historia de usuario (US1, US2) para
permitir implementación y prueba independientes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario a la que pertenece la tarea (US1, US2)
- Rutas de archivo exactas incluidas en cada descripción

## Path Conventions

Proyecto único (Gradle) — ver `plan.md` § Project Structure:

- Producción: `src/main/java/ec/edu/ups/gestionvehicular/turnos/...`
- Pruebas: `src/test/java/ec/edu/ups/gestionvehicular/turnos/...`
- Features Gherkin: `src/test/resources/features/`
- Contrato OpenAPI (ya escrito): `specs/001-agendar-turno-mantenimiento/contracts/openapi.yaml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar el build para Arquitectura Limpia, API-First y cobertura JaCoCo

- [ ] T001 Añadir el plugin `jacoco` a `build.gradle` con una tarea `jacocoTestCoverageVerification` (umbral >80% por clase, ≥80% global) enlazada a `check`, excluyendo explícitamente `GestionvehicularServiceApplication` y clases de configuración puramente declarativas (Principio V de la constitución)
- [ ] T002 [P] Añadir y configurar `org.openapi.generator` en `build.gradle` para generar interfaces de servidor (`interfaceOnly`, generador `spring`) y DTOs a partir de `specs/001-agendar-turno-mantenimiento/contracts/openapi.yaml`, enlazado a la tarea `compileJava`
- [ ] T003 [P] Añadir dependencias de test `io.cucumber:cucumber-junit-platform-engine`, `io.cucumber:cucumber-spring` y `org.junit.platform:junit-platform-suite` a `build.gradle`
- [ ] T004 [P] Crear el esqueleto de paquetes del módulo `turnos` bajo `src/main/java/ec/edu/ups/gestionvehicular/turnos/` (`domain/model`, `domain/exception`, `application/port/in`, `application/port/out`, `application/service`, `adapters/in/web`, `adapters/out/persistence`, `adapters/out/notification`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Dominio, puertos y adaptadores de persistencia/notificación compartidos por ambas historias

**⚠️ CRITICAL**: Ninguna historia de usuario puede implementarse hasta completar esta fase

- [ ] T005 [P] Crear enum `EstadoFranja` (`DISPONIBLE`, `OCUPADA`) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/model/EstadoFranja.java`
- [ ] T006 [P] Crear enum `EstadoTurno` (`AGENDADO`, `COMPLETADO`) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/model/EstadoTurno.java`
- [ ] T007 [P] Crear entidad de dominio `FranjaMantenimiento` (id, fechaHoraInicio, fechaHoraFin, estado) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/model/FranjaMantenimiento.java` según `data-model.md`
- [ ] T008 [P] Crear entidad de dominio `Turno` (id, vehiculoId, policiaId, franjaId, estado, fechaCreacion) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/model/Turno.java` según `data-model.md`
- [ ] T009 [P] Crear entidad de dominio `VehiculoAsignado` (vehiculoId, policiaId) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/model/VehiculoAsignado.java`
- [ ] T010 [P] Crear entidad de dominio `Notificacion` (id, turnoId, destinatarioTipo, estadoEnvio, fechaEnvio) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/model/Notificacion.java`
- [ ] T011 [P] Crear excepciones de dominio `FranjaNoDisponibleException`, `VehiculoSinAsignacionException`, `TurnoVigenteExistenteException` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/domain/exception/`
- [ ] T012 Definir los puertos de salida `FranjaRepositoryPort`, `TurnoRepositoryPort`, `VehiculoAsignadoPort`, `NotificadorTurnoPort` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/application/port/out/` (depende de T005-T010)
- [ ] T013 [P] Crear entidades JPA y repositorios Spring Data para `FranjaMantenimiento`, `Turno`, `VehiculoAsignado`, `Notificacion` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/adapters/out/persistence/` (depende de T012)
- [ ] T014 Implementar los adaptadores de persistencia que mapean entidades JPA↔dominio e implementan `FranjaRepositoryPort`, `TurnoRepositoryPort` y `VehiculoAsignadoPort` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/adapters/out/persistence/` (depende de T013)
- [ ] T015 [P] Implementar el adaptador `NotificadorTurnoAdapter` que implementa `NotificadorTurnoPort` (persiste `Notificacion` vía repositorio; ver Edge Case de fallo de envío) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/adapters/out/notification/NotificadorTurnoAdapter.java` (depende de T012, T013)
- [ ] T016 [P] Prueba de integración `@DataJpaTest` de los adaptadores de persistencia (franjas, turnos, vehículo asignado, notificación) en `src/test/java/ec/edu/ups/gestionvehicular/turnos/adapters/out/persistence/PersistenceAdaptersIT.java` (depende de T014)

**Checkpoint**: Dominio, puertos, persistencia y notificación listos — las historias de usuario pueden comenzar

---

## Phase 3: User Story 1 - Agendar un turno en una franja disponible (Priority: P1) 🎯 MVP

**Goal**: Un policía con vehículo asignado consulta franjas disponibles, confirma una, el turno queda registrado y mecánico/encargado son notificados

**Independent Test**: Iniciar sesión como policía con vehículo asignado, `GET` franjas disponibles, `POST` turno sobre una de ellas y verificar `201` + notificaciones generadas; luego `GET` el turno y verificar sus datos

### Tests for User Story 1 (BDD, escritas antes de la implementación) ⚠️

> **NOTE: Escribir estas pruebas PRIMERO y verificar que fallan antes de implementar**

- [ ] T017 [P] [US1] Unit test de `AgendarTurnoService` (camino feliz, dobles de prueba para los port/out) en `src/test/java/ec/edu/ups/gestionvehicular/turnos/application/AgendarTurnoServiceTest.java`
- [ ] T018 [P] [US1] Unit test de `ConsultarFranjasDisponiblesService` en `src/test/java/ec/edu/ups/gestionvehicular/turnos/application/ConsultarFranjasDisponiblesServiceTest.java`
- [ ] T019 [P] [US1] Unit test de `ConsultarTurnoService` en `src/test/java/ec/edu/ups/gestionvehicular/turnos/application/ConsultarTurnoServiceTest.java`
- [ ] T020 [P] [US1] Integration test `@WebMvcTest` del camino feliz de `TurnosController` (consultar franjas, agendar, consultar turno) en `src/test/java/ec/edu/ups/gestionvehicular/turnos/adapters/in/web/TurnosControllerHappyPathIT.java`
- [ ] T021 [US1] Escenario funcional/BDD (Gherkin, Acceptance Scenario 1 de `spec.md`) y sus step definitions + runner Cucumber en `src/test/resources/features/agendar_turno_mantenimiento.feature` y `src/test/java/ec/edu/ups/gestionvehicular/turnos/bdd/AgendarTurnoRunner.java` / `AgendarTurnoStepDefinitions.java`

### Implementation for User Story 1

- [ ] T022 [US1] Definir los puertos de entrada `AgendarTurnoUseCase`, `ConsultarFranjasDisponiblesUseCase`, `ConsultarTurnoUseCase` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/application/port/in/` (depende de T012)
- [ ] T023 [US1] Implementar `AgendarTurnoService` (camino feliz: valida vehículo asignado, reserva franja de forma atómica, crea turno, dispara notificación) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/application/service/AgendarTurnoService.java` (depende de T022)
- [ ] T024 [P] [US1] Implementar `ConsultarFranjasDisponiblesService` (solo franjas `DISPONIBLE` con inicio futuro, FR-008) en `src/main/java/ec/edu/ups/gestionvehicular/turnos/application/service/ConsultarFranjasDisponiblesService.java` (depende de T022)
- [ ] T025 [P] [US1] Implementar `ConsultarTurnoService` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/application/service/ConsultarTurnoService.java` (depende de T022)
- [ ] T026 [US1] Implementar `TurnosController` (implementa la interfaz de servidor generada por openapi-generator) y los mappers DTO↔dominio para los 3 endpoints en `src/main/java/ec/edu/ups/gestionvehicular/turnos/adapters/in/web/TurnosController.java` (depende de T023, T024, T025)

**Checkpoint**: User Story 1 funcional y probable de forma independiente (MVP)

---

## Phase 4: User Story 2 - Orientación ante una franja ya ocupada (Priority: P2)

**Goal**: Si la franja elegida ya fue tomada, el sistema responde con las próximas franjas disponibles en vez de solo bloquear

**Independent Test**: Provocar que dos solicitudes confirmen la misma franja; verificar que la segunda recibe `409` con `proximasFranjasDisponibles` no vacío, y que confirmar una de esas alternativas produce un `201` normal

### Tests for User Story 2 (BDD, escritas antes de la implementación) ⚠️

- [ ] T027 [P] [US2] Unit test de `AgendarTurnoService`: conflicto de franja ya ocupada devuelve próximas franjas disponibles, en `src/test/java/ec/edu/ups/gestionvehicular/turnos/application/AgendarTurnoServiceConflictTest.java`
- [ ] T028 [P] [US2] Integration test `@WebMvcTest` del escenario de conflicto (`409`) de `TurnosController` en `src/test/java/ec/edu/ups/gestionvehicular/turnos/adapters/in/web/TurnosControllerConflictIT.java`
- [ ] T029 [US2] Escenario funcional/BDD (Gherkin, Acceptance Scenario 2 de `spec.md`) añadido a `src/test/resources/features/agendar_turno_mantenimiento.feature` y a los step definitions en `src/test/java/ec/edu/ups/gestionvehicular/turnos/bdd/AgendarTurnoStepDefinitions.java` (depende de T021)

### Implementation for User Story 2

- [ ] T030 [US2] Extender `AgendarTurnoService` para capturar el conflicto de franja ya ocupada (chequeo atómico/optimista sobre la reserva) y calcular las próximas franjas disponibles vía `FranjaRepositoryPort` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/application/service/AgendarTurnoService.java` (depende de T023)
- [ ] T031 [US2] Añadir un manejador de excepciones que traduzca `FranjaNoDisponibleException` a una respuesta `409` con `proximasFranjasDisponibles` en `src/main/java/ec/edu/ups/gestionvehicular/turnos/adapters/in/web/TurnosExceptionHandler.java` (depende de T026, T030)

**Checkpoint**: User Story 1 y 2 funcionan de forma independiente y en conjunto

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Edge cases de `spec.md`, verificación de cobertura y validación manual

- [ ] T032 [P] Test y guarda de negocio: vehículo sin asignación devuelve `404` (`VehiculoSinAsignacionException`) — unit test en `application/AgendarTurnoServiceTest.java` e integración en `adapters/in/web/TurnosControllerHappyPathIT.java`
- [ ] T033 [P] Test y guarda de negocio: turno vigente ya existente para el vehículo devuelve `422` (`TurnoVigenteExistenteException`) — unit test en `application/AgendarTurnoServiceTest.java` e integración en `adapters/in/web/`
- [ ] T034 [P] Test y manejo: sin franjas disponibles en el rango consultado → respuesta con lista vacía y mensaje explicativo (no una lista vacía sin contexto) en `ConsultarFranjasDisponiblesService` y su test
- [ ] T035 [P] Test y manejo: fallo al notificar no revierte el turno (`Notificacion` queda `PENDIENTE`, `Turno` permanece `AGENDADO`) — unit test de `AgendarTurnoService` y de `NotificadorTurnoAdapter`
- [ ] T036 Ejecutar `./gradlew check` y revisar `build/reports/jacoco/test/html/index.html`; ajustar pruebas o exclusiones hasta cumplir >80% por clase y ≥80% global (Principio V)
- [ ] T037 Ejecutar manualmente los pasos de `quickstart.md` contra el servicio levantado con `./gradlew bootRun` para validar ambos escenarios de aceptación end-to-end

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — puede iniciar de inmediato
- **Foundational (Phase 2)**: Depende de Setup — BLOQUEA ambas historias
- **User Story 1 (Phase 3)**: Depende de Foundational
- **User Story 2 (Phase 4)**: Depende de Foundational y de la implementación del camino feliz de US1 (T023, T026), porque extiende el mismo `AgendarTurnoService`/`TurnosController`
- **Polish (Phase 5)**: Depende de que US1 y US2 estén completas

### User Story Dependencies

- **User Story 1 (P1)**: Sin dependencia de otras historias — es el MVP
- **User Story 2 (P2)**: Extiende el flujo de confirmación de turno de US1 (mismo servicio/endpoint); no es implementable antes de que exista T023/T026, pero sí es independientemente probable una vez implementada (escenario de conflicto vs. escenario feliz)

### Within Each User Story

- Pruebas (unit + integración + BDD) se escriben antes que la implementación y deben fallar primero
- Puertos de entrada antes que servicios de aplicación
- Servicios de aplicación antes que el controller
- Historia completa (checkpoint) antes de pasar a la siguiente

### Parallel Opportunities

- Todas las tareas [P] de Setup (T002-T004) en paralelo
- Todas las tareas [P] de Foundational (T005-T011, T013, T015, T016) en paralelo dentro de sus dependencias
- Dentro de US1: T017-T020 en paralelo (tests, archivos distintos); T024-T025 en paralelo (servicios distintos)
- Dentro de US2: T027-T028 en paralelo
- Todas las tareas [P] de Polish (T032-T035) en paralelo

---

## Parallel Example: User Story 1

```bash
# Lanzar en paralelo las pruebas de User Story 1:
Task: "Unit test de AgendarTurnoService en src/test/java/ec/edu/ups/gestionvehicular/turnos/application/AgendarTurnoServiceTest.java"
Task: "Unit test de ConsultarFranjasDisponiblesService en src/test/java/ec/edu/ups/gestionvehicular/turnos/application/ConsultarFranjasDisponiblesServiceTest.java"
Task: "Unit test de ConsultarTurnoService en src/test/java/ec/edu/ups/gestionvehicular/turnos/application/ConsultarTurnoServiceTest.java"
Task: "Integration test @WebMvcTest en src/test/java/ec/edu/ups/gestionvehicular/turnos/adapters/in/web/TurnosControllerHappyPathIT.java"

# Lanzar en paralelo los servicios de User Story 1 que no dependen entre sí:
Task: "Implementar ConsultarFranjasDisponiblesService en src/main/java/ec/edu/ups/gestionvehicular/turnos/application/service/ConsultarFranjasDisponiblesService.java"
Task: "Implementar ConsultarTurnoService en src/main/java/ec/edu/ups/gestionvehicular/turnos/application/service/ConsultarTurnoService.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 solamente)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational (CRÍTICO — bloquea ambas historias)
3. Completar Phase 3: User Story 1
4. **DETENER y VALIDAR**: probar User Story 1 de forma independiente (quickstart.md, Escenario 1)
5. Desplegar/demostrar si está listo

### Incremental Delivery

1. Setup + Foundational → base lista
2. Añadir User Story 1 → probar independientemente → demo (MVP)
3. Añadir User Story 2 → probar independientemente → demo
4. Añadir Polish (edge cases + cobertura) → verificación final de constitución

---

## Notes

- [P] = archivos distintos, sin dependencias pendientes
- Cada tarea de historia lleva su etiqueta [US1]/[US2] para trazabilidad
- Verificar que las pruebas fallan antes de implementar (BDD/TDD, Principio II)
- Confirmar cobertura JaCoCo (>80% por clase, ≥80% global) antes de dar por cerrada la feature (Principio V)
- Evitar: tareas vagas, conflictos de archivo en tareas [P], dependencias cruzadas entre historias que rompan su independencia de prueba
