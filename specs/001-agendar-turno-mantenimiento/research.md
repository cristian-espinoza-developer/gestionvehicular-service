# Research: Autoagendar turno de mantenimiento

## 1. Arquitectura del módulo

**Decision**: Implementar el módulo `turnos` como monolito modular por
capas (domain/application/adapters) dentro del proyecto Gradle único
existente, en vez de crear subproyectos Gradle separados por capa.

**Rationale**: El proyecto está en su primera feature real; introducir
multi-módulo Gradle ahora sería complejidad no respaldada por un requisito
actual (viola YAGNI, Principio III de la constitución). La separación por
paquetes ya impone la regla de dependencia de Arquitectura Limpia (Principio
I) sin el costo de build/configuración de multi-módulo.

**Alternatives considered**:
- Subproyectos Gradle (`:domain`, `:application`, `:adapters`): rechazado por
  sobre-ingeniería prematura para una sola épica.
- Paquete único sin separación de capas: rechazado por violar directamente
  el Principio I (regla de dependencia) de la constitución.

## 2. Framework de pruebas BDD funcionales

**Decision**: Cucumber-JVM (`io.cucumber:cucumber-junit-platform-engine`,
`io.cucumber:cucumber-spring`) ejecutado sobre JUnit 5 Platform, con
escenarios Gherkin en `src/test/resources/features/` que replican
literalmente los Acceptance Scenarios de `spec.md`.

**Rationale**: La constitución exige BDD explícito (Given-When-Then) en el
nivel funcional/aceptación (Principio II). Cucumber es el estándar de facto
para BDD en JVM/Spring Boot y se integra con `@SpringBootTest` para pruebas
funcionales de extremo a extremo contra el contexto real de la aplicación.

**Alternatives considered**:
- Escribir los escenarios como tests JUnit con nombres descriptivos
  "given/when/then" sin Gherkin: rechazado porque no produce un artefacto
  legible por negocio ni trazabilidad 1:1 con los escenarios de la spec.
- JBehave: rechazado por menor adopción y soporte actual en el ecosistema
  Spring Boot comparado con Cucumber-JVM.

## 3. Generación de API desde contrato OpenAPI

**Decision**: Usar `org.openapi.generator` (Gradle plugin) apuntando al
contrato versionado en `specs/001-agendar-turno-mantenimiento/contracts/openapi.yaml`,
generando interfaces de servidor (`interfaceOnly`) y DTOs para el generador
`spring`, que el adaptador `adapters/in/web` implementa.

**Rationale**: Exigencia directa del Principio IV (API-First): el contrato
es la fuente de verdad y el código de interfaz/DTO se deriva de él, evitando
divergencia entre documentación e implementación.

**Alternatives considered**:
- springdoc-openapi (contract-last, genera el YAML a partir del código):
  rechazado porque invierte el flujo exigido por la constitución
  (API-First, no code-first).
- Escribir DTOs y controllers a mano en paralelo al contrato: rechazado
  explícitamente por el Principio IV.

## 4. Cobertura de pruebas con JaCoCo

**Decision**: Añadir el plugin Gradle `jacoco`, configurar
`jacocoTestCoverageVerification` con reglas de umbral por clase (>80%
instrucción y rama) y global (≥80%), enlazada a la tarea `check`. Excluir
explícitamente clases de arranque/configuración puramente declarativas
(p. ej. la clase `*Application`, clases `@ConfigurationProperties` sin
lógica) vía el bloque de exclusión de JaCoCo.

**Rationale**: Exigencia directa del Principio V (NON-NEGOTIABLE).

**Alternatives considered**: Ninguna — el principio fija la herramienta.

## 5. Persistencia de franjas y turnos

**Decision**: Modelar `FranjaMantenimiento` y `Turno` como entidades JPA
propias de este módulo, usando H2 como en el resto del proyecto. El esquema
(DDL) se define explícitamente en `src/main/resources/db/schema.sql` y los
datos precargados (vehículo(s) asignado(s) de ejemplo y un conjunto de
franjas de mantenimiento futuras) en `src/main/resources/db/data.sql`,
ambos ejecutados automáticamente por Spring Boot al iniciar (modo
`spring.sql.init.mode=always`), en vez de dejar el esquema a la generación
automática de Hibernate (`ddl-auto`). No se integra aún con un sistema
externo de programación de mecánicos.

**Rationale**: La spec asume que las franjas ya existen (gestionadas fuera
del alcance de esta historia de UI), pero como este es el primer feature
del proyecto no existe aún ningún módulo que las persista; crear las
entidades mínimas necesarias y precargar datos de ejemplo es la única forma
de satisfacer los criterios de aceptación y permitir validar `quickstart.md`
sin depender de un sistema externo inexistente. Versionar el esquema como
script SQL explícito (en vez de `ddl-auto: update/create`) hace el esquema
auditable y reproducible entre entornos, y evita que Hibernate infiera un
DDL implícito no revisado. Se documenta como supuesto en `spec.md` y
`data-model.md`.

**Alternatives considered**:
- Servicio externo de calendario/mecánicos: rechazado por falta de
  información sobre su existencia y por introducir una integración no
  solicitada (viola YAGNI).
- `spring.jpa.hibernate.ddl-auto=update`/`create` sin scripts SQL versionados:
  rechazado porque el esquema quedaría implícito en las anotaciones JPA, sin
  un artefacto auditable ni forma directa de precargar datos de ejemplo.
- Flyway/Liquibase (migraciones versionadas con historial): considerado para
  una evolución futura del esquema, pero rechazado por ahora al no existir
  todavía una segunda migración que gestionar (YAGNI); `schema.sql` +
  `data.sql` es suficiente para el alcance actual de una sola feature.

## 6. Asignación de vehículo al policía

**Decision**: Exponer un puerto de salida `VehiculoAsignadoPort` con una
única operación de consulta (vehículo asignado a un policía dado). La
implementación inicial es un adaptador JPA sobre una tabla mínima
`vehiculo_asignado` (vehiculoId, policiaId), a reemplazar cuando exista un
módulo de gestión de flota dedicado.

**Rationale**: Mantiene el caso de uso de agendamiento desacoplado del
origen real de la asignación (Principio I e inversión de dependencias de
SOLID), permitiendo sustituir el adaptador sin tocar dominio/aplicación
cuando el módulo de vehículos se construya.

**Alternatives considered**:
- Hardcodear la validación de vehículo en el caso de uso sin puerto:
  rechazado por violar SOLID (inversión de dependencias) y por acoplar el
  caso de uso a JPA.

## 7. Identidad del policía solicitante

**Decision**: Resolver `policiaId` vía un puerto de entrada mínimo
(`PoliciaIdentityPort`) que lee el encabezado HTTP `X-Policia-Id` de cada
request, en vez de implementar autenticación real.

**Rationale**: No existe aún módulo de autenticación en el proyecto
(greenfield); introducirlo ahora sería alcance no solicitado (viola YAGNI,
Principio III). Un encabezado explícito mantiene el caso de uso
desacoplado del mecanismo real de identidad (SOLID, inversión de
dependencias) y es reemplazable sin tocar dominio/aplicación cuando exista
autenticación real (FR-010).

**Alternatives considered**:
- Spring Security completo (JWT/sesión): rechazado, fuera de alcance de
  esta historia.
- Asumir un único policía fijo de prueba: rechazado, no permite validar el
  escenario de conflicto de US2 con múltiples usuarios concurrentes.

## 8. Atomicidad en la reserva de una franja

**Decision**: La reserva atómica de una franja se implementa mediante una
actualización condicional (`UPDATE franja_mantenimiento SET
estado='OCUPADA' WHERE id=? AND estado='DISPONIBLE'`) combinada con una
restricción `UNIQUE` sobre `turno.franja_id`, en vez de bloqueo optimista
vía `@Version` de JPA.

**Rationale**: Es el mecanismo más simple y explícito para garantizar
FR-005 sin depender de reintentos de `OptimisticLockException`; si la
actualización condicional afecta 0 filas, se interpreta directamente como
conflicto de reserva (FR-006).

**Alternatives considered**:
- `@Version` (bloqueo optimista) + reintento automático: rechazado, más
  complejo de lo necesario para el volumen de esta feature.
- `SELECT ... FOR UPDATE` (bloqueo pesimista): rechazado, requiere
  gestión explícita de transacciones largas sin beneficio adicional aquí.

## Resumen

No quedan `NEEDS CLARIFICATION` pendientes del Technical Context; todas las
decisiones anteriores están resueltas y listas para Fase 1 (Design &
Contracts).
