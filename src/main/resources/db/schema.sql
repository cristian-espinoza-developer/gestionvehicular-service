CREATE TABLE IF NOT EXISTS vehiculo_asignado (
    vehiculo_id UUID NOT NULL,
    policia_id UUID NOT NULL,
    PRIMARY KEY (vehiculo_id, policia_id)
);

CREATE TABLE IF NOT EXISTS franja_mantenimiento (
    id UUID PRIMARY KEY,
    fecha_hora_inicio TIMESTAMP NOT NULL,
    fecha_hora_fin TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS turno (
    id UUID PRIMARY KEY,
    vehiculo_id UUID NOT NULL,
    policia_id UUID NOT NULL,
    franja_id UUID NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    CONSTRAINT uq_turno_franja_id UNIQUE (franja_id)
);

CREATE TABLE IF NOT EXISTS notificacion (
    id UUID PRIMARY KEY,
    turno_id UUID NOT NULL,
    destinatario_tipo VARCHAR(20) NOT NULL,
    estado_envio VARCHAR(20) NOT NULL,
    fecha_envio TIMESTAMP NOT NULL
);
