# language: es
Característica: Autoagendar turno de mantenimiento

  Escenario: Agendar un turno en una franja disponible
    Dado que el policía accede al sistema con su vehículo asignado y existen franjas disponibles
    Cuando selecciona una franja disponible y confirma
    Entonces el turno queda registrado en el sistema asociado a su vehículo
    Y el mecánico y el encargado reciben una notificación de la creación del turno

  Escenario: Orientación ante una franja ya ocupada
    Dado que una franja de mantenimiento ya fue reservada por otro usuario
    Cuando el policía intenta confirmar esa misma franja
    Entonces el sistema rechaza la confirmación e inmediatamente muestra las próximas franjas disponibles
    Cuando el policía selecciona una de las franjas alternativas y confirma
    Entonces el turno queda registrado normalmente como en el flujo principal
