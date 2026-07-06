<!--
Sync Impact Report
==================
Version change: [TEMPLATE] → 1.0.0 (initial ratification)
Modified principles: N/A (first concrete version, all principles newly defined)
Added sections:
  - Core Principles: I. Clean Architecture, II. BDD Testing Discipline,
    III. SOLID / YAGNI / DRY, IV. API-First with OpenAPI Contracts,
    V. Coverage Gates via JaCoCo
  - Technology & Tooling Constraints
  - Quality Gates & Development Workflow
  - Governance
Removed sections: none (template placeholders replaced)
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ no changes needed (Constitution Check
    section is generic and reads gates from this file at plan-generation time)
  - .specify/templates/spec-template.md ✅ no changes needed (no hardcoded
    architecture/testing/API references that conflict with the principles above)
  - .specify/templates/tasks-template.md ✅ no changes needed (task phases are
    generic; /speckit-tasks and /speckit-plan should populate contract/OpenAPI,
    BDD-layer test tasks, and JaCoCo verification tasks per-feature based on this
    constitution and the plan's Constitution Check)
Follow-up TODOs:
  - TODO(RATIFICATION_DATE): original adoption date unknown; set to date of this
    constitution's first commit if a more precise historical date is not supplied.
-->

# gestionvehicular-service Constitution
<!-- Servicio de gestión vehicular — Spring Boot / Java, arquitectura limpia -->

## Core Principles

### I. Clean Architecture (Robert C. Martin)
El sistema MUST organizarse en capas concéntricas independientes del framework,
siguiendo la Arquitectura Limpia: **Entidades** (reglas de negocio empresariales),
**Casos de Uso** (reglas de negocio de la aplicación), **Adaptadores de Interfaz**
(controllers, presenters, gateways) y **Frameworks & Drivers** (Spring Boot, JPA,
Web, DB). La regla de dependencia es NON-NEGOTIABLE: el código fuente solo puede
depender hacia adentro; ninguna capa interna puede conocer detalles de una capa
externa. Las entidades y casos de uso MUST permanecer libres de anotaciones o
tipos de Spring/JPA/Web; la comunicación entre capas se realiza mediante
interfaces (puertos) definidas por la capa interna e implementadas por la
externa (inversión de dependencias). Cada módulo/paquete MUST reflejar esta
separación en su estructura de directorios (p. ej. `domain`, `application`,
`adapters`/`infrastructure`, `frameworks`/`entrypoints`).
**Razón**: aislar las reglas de negocio de detalles técnicos permite que el
negocio evolucione sin acoplarse a Spring, la base de datos o el transporte
HTTP, y facilita pruebas unitarias rápidas sin infraestructura.

### II. BDD Testing Discipline (Unit, Integration, Functional)
Todo comportamiento MUST especificarse y verificarse usando el enfoque BDD
(Given-When-Then) en sus tres niveles obligatorios:
- **Pruebas unitarias**: verifican entidades y casos de uso en aislamiento
  (sin Spring context, con dobles de prueba para los puertos).
- **Pruebas de integración**: verifican adaptadores concretos (repositorios
  JPA, clientes HTTP, mappers) contra dependencias reales o equivalentes
  (p. ej. H2, contenedores de prueba).
- **Pruebas funcionales/aceptación**: verifican el comportamiento end-to-end
  expuesto por la API, expresado en escenarios BDD (dado/cuando/entonces)
  trazables a los criterios de aceptación de la especificación.
Las pruebas MUST escribirse antes o junto con el código que las satisface, y
todo escenario de aceptación de una spec MUST tener al menos un test funcional
correspondiente. Se prohíbe fusionar código sin sus pruebas asociadas en los
tres niveles cuando el cambio lo amerite (nueva entidad, nuevo caso de uso,
nuevo endpoint).
**Razón**: BDD conecta el comportamiento observable del sistema con el lenguaje
de negocio, hace explícitos los criterios de aceptación y evita que las pruebas
degeneren en verificación de implementación en vez de comportamiento.

### III. SOLID, YAGNI, DRY
Todo el código MUST cumplir los principios SOLID (responsabilidad única,
abierto/cerrado, sustitución de Liskov, segregación de interfaces, inversión
de dependencias). Se aplica YAGNI: no se implementa funcionalidad, capas de
abstracción o configuración especulativa que no esté respaldada por un
requisito actual. Se aplica DRY: la duplicación de lógica de negocio o reglas
de validación MUST eliminarse mediante abstracciones compartidas dentro de la
capa correspondiente, sin violar la regla de dependencia de la Arquitectura
Limpia. Ninguna de estas prácticas justifica romper el Principio I; DRY y
SOLID se aplican dentro de cada capa y en los contratos entre capas, no como
excusa para acoplar capas internas y externas.
**Razón**: mantener el código simple, cohesivo y sin duplicación reduce el
costo de cambio y facilita el cumplimiento sostenido de la arquitectura y las
métricas de cobertura.

### IV. API-First con Contrato OpenAPI
Toda API pública MUST diseñarse API-First: el contrato OpenAPI (`openapi.yml`
o `openapi.yaml`) se define y revisa ANTES de escribir la implementación del
endpoint, y vive versionado en el repositorio (p. ej. `contracts/` o
`src/main/resources/openapi/`). El contrato es la fuente de verdad; los DTOs,
interfaces de controller y clientes se generan a partir de él usando
`openapi-generator` (plugin de Gradle/Maven), no se escriben a mano en
paralelo al contrato. Cualquier cambio de comportamiento observable de la API
MUST reflejarse primero en el contrato OpenAPI, seguido de la regeneración de
código y la implementación. Se prohíbe mergear un endpoint sin su contrato
OpenAPI correspondiente.
**Razón**: un contrato explícito y generado automáticamente evita la deriva
entre documentación e implementación, habilita consumidores externos desde el
día uno y refuerza el desacoplamiento entre la capa de adaptadores y los
casos de uso.

### V. Métricas de Cobertura con JaCoCo (NON-NEGOTIABLE)
JaCoCo MUST estar configurado como plugin de build y ejecutarse en cada build
de verificación (`check`/CI). Se exigen los siguientes umbrales mínimos, y el
build MUST fallar si no se cumplen:
- **Cobertura por clase**: > 80% (instrucción y rama) para cada clase de
  producción sujeta a pruebas (excluyendo clases de configuración/arranque
  puramente declarativas, que MUST estar explícitamente excluidas en la
  configuración de JaCoCo, no ignoradas tácitamente).
- **Cobertura global del proyecto**: ≥ 80%.
Los reportes de JaCoCo MUST generarse en cada ejecución de verificación y
publicarse como artefacto de build. Una reducción de cobertura por debajo de
los umbrales bloquea el merge hasta que se corrija con pruebas adicionales
(no reduciendo el umbral configurado).
**Razón**: umbrales de cobertura verificables automáticamente son la señal
objetiva de que la disciplina BDD (Principio II) se está aplicando de forma
consistente y no solo declarada.

## Technology & Tooling Constraints

- **Stack**: Java (toolchain Java 25) y Spring Boot sobre Gradle. Las
  dependencias de framework (Spring, JPA, Web) MUST confinarse a las capas
  de adaptadores/frameworks; el dominio y los casos de uso permanecen en
  Java puro.
- **Generación de código API**: el plugin de Gradle de `openapi-generator`
  MUST integrarse en el build para generar interfaces/DTOs a partir del
  contrato OpenAPI como parte de la tarea de compilación.
- **Cobertura**: el plugin de Gradle `jacoco` MUST estar declarado en
  `build.gradle` con una tarea `jacocoTestCoverageVerification` enlazada a
  `check`, aplicando los umbrales del Principio V.
- **Persistencia y pruebas**: las pruebas de integración pueden usar H2 (ya
  presente en el proyecto) u otro medio equivalente, siempre que verifiquen
  el adaptador real, no un doble de prueba.

## Quality Gates & Development Workflow

- Ningún Pull Request se aprueba si: (a) falta el contrato OpenAPI para un
  endpoint nuevo o modificado, (b) faltan pruebas en alguno de los tres
  niveles BDD requeridos por el cambio, o (c) la verificación de JaCoCo
  (por clase y global) no pasa.
- Toda revisión de código MUST verificar explícitamente el cumplimiento de
  la regla de dependencia de Arquitectura Limpia (Principio I): no se
  aceptan imports de Spring/JPA/Web dentro de `domain`/`application`.
- Cualquier violación deliberada de un principio MUST documentarse y
  justificarse (ver `Complexity Tracking` en los planes de feature) antes de
  mergear.

## Governance

Esta constitución prevalece sobre cualquier otra práctica, guía o convención
en conflicto dentro del repositorio. Las enmiendas requieren: (1) propuesta
documentada del cambio y su motivación, (2) actualización de esta constitución
mediante el flujo de `/speckit-constitution`, incluyendo el Sync Impact Report,
y (3) propagación de los cambios a las plantillas dependientes
(`plan-template.md`, `spec-template.md`, `tasks-template.md`) en el mismo
cambio o como tarea de seguimiento explícita.

El versionado sigue semver: MAJOR para eliminación o redefinición incompatible
de principios; MINOR para principios o secciones nuevas o expansión material
de guía existente; PATCH para aclaraciones o correcciones de redacción sin
cambio de sentido.

Toda PR y revisión de código MUST verificar cumplimiento de esta constitución.
La complejidad no justificada por un requisito real MUST rechazarse (YAGNI,
Principio III). Para guía de desarrollo en tiempo de ejecución, usar
`CLAUDE.md` y los planes de feature en `specs/`.

**Version**: 1.0.0 | **Ratified**: TODO(RATIFICATION_DATE): fecha de adopción original no confirmada por el usuario | **Last Amended**: 2026-07-05
