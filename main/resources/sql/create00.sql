START TRANSACTION;
CREATE TYPE E_DIRECTION AS ENUM ('RX', 'RX_ACK', 'RX_EMG', 'TX', 'TX_FAILED');
CREATE TYPE E_UNITSTATE AS ENUM ('AD', 'EB', 'NEB');
CREATE TYPE E_UNITTYPE AS ENUM ('Portable', 'Triage', 'Treatment', 'Postprocessing', 'Info', 'Officer');
CREATE TYPE E_ROLE AS ENUM ('Dashboard', 'MLS', 'Kdt', 'Root');
CREATE TYPE E_INCIDENTSTATE AS ENUM ('Open', 'Demand', 'InProgress', 'Done');
CREATE TYPE E_INCIDENTTYPE AS ENUM ('HoldPosition', 'Standby', 'Relocation', 'ToHome', 'Treatment', 'Task', 'Transport');
CREATE TYPE E_LOGTYPE AS ENUM ('CONCERN_CREATE', 'CONCERN_UPDATE', 'CONCERN_REMOVE', 'CONCERN_CLOSE', 'CONCERN_REOPEN',
    'INCIDENT_CREATE', 'INCIDENT_UPDATE', 'INCIDENT_DELETE', 'INCIDENT_AUTO_STATE', 'INCIDENT_AUTO_DONE',
    'UNIT_CREATE', 'UNIT_CREATE_REMOVED', 'UNIT_UPDATE', 'UNIT_ASSIGN', 'UNIT_DETACH', 'UNIT_AUTO_DETACH', 'UNIT_AUTOSET_POSITION',
    'TASKSTATE_CHANGED', 'PATIENT_CREATE', 'PATIENT_UPDATE', 'PATIENT_ASSIGN', 'CUSTOM');
CREATE TYPE E_TASKSTATE AS ENUM ('Assigned', 'ZBO', 'ABO', 'ZAO', 'AAO', 'Detached');
CREATE TYPE E_NACA AS ENUM ('I', 'II', 'III', 'IV', 'V', 'VI', 'VII');
CREATE TYPE E_SEX AS ENUM ('Male', 'Female');

CREATE OR REPLACE FUNCTION update_lastStateChangeAt_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.lastStateChangeAt = (now() at time zone 'utc');
    RETURN NEW;
END;
$$ language 'plpgsql';

COMMIT;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS selcall
(
    id        SERIAL PRIMARY KEY,
    ani       VARCHAR(10) NOT NULL,
    port      VARCHAR(20),
    ts        TIMESTAMP   NOT NULL,
    direction E_DIRECTION NOT NULL
);

CREATE TABLE IF NOT EXISTS concern
(
    id     SERIAL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    info   TEXT         NOT NULL DEFAULT (''),
    closed BOOLEAN      NOT NULL DEFAULT (false)
);

CREATE TABLE IF NOT EXISTS sections
(
    concern_fk INTEGER REFERENCES concern ON DELETE CASCADE,
    name       VARCHAR(30) NOT NULL,
    PRIMARY KEY (concern_fk, name)
);

CREATE TABLE IF NOT EXISTS unit
(
    id               SERIAL PRIMARY KEY,
    concern_fk       INTEGER     NOT NULL REFERENCES concern ON DELETE CASCADE,
    state            E_UNITSTATE NOT NULL,
    call             VARCHAR(64) NOT NULL,
    ani              VARCHAR(64) NOT NULL,
    withDoc          BOOLEAN     NOT NULL,
    portable         BOOLEAN     NOT NULL,
    transportVehicle BOOLEAN     NOT NULL,
    type             E_UNITTYPE,
    info             TEXT        NOT NULL,
    position         JSONB,
    home             JSONB,
    capacity         INTEGER,
    imgsrc           VARCHAR(30),
    section_fk       VARCHAR(30),
    FOREIGN KEY (concern_fk, section_fk) REFERENCES sections
);

CREATE TABLE IF NOT EXISTS users
(
    id          SERIAL PRIMARY KEY,
    firstname   VARCHAR(64) NOT NULL,
    lastname    VARCHAR(64) NOT NULL,
    personnelId INTEGER     NOT NULL,
    contact     TEXT        NOT NULL,
    info        TEXT        NOT NULL,
    concern_fk  INTEGER     REFERENCES concern ON DELETE SET NULL,
    allowLogin  BOOLEAN     NOT NULL,
    username    VARCHAR(32),
    hashedPW    TEXT
);

CREATE TABLE IF NOT EXISTS user_role
(
    user_fk INTEGER NOT NULL REFERENCES users ON DELETE CASCADE,
    urole   E_ROLE  NOT NULL,
    PRIMARY KEY (user_fk, urole)
);

CREATE TABLE IF NOT EXISTS crew
(
    unit_fk integer NOT NULL REFERENCES unit ON DELETE CASCADE,
    user_fk integer NOT NULL REFERENCES users ON DELETE CASCADE,
    PRIMARY KEY (unit_fk, user_fk)
);

CREATE TABLE IF NOT EXISTS patient
(
    id         SERIAL PRIMARY KEY,
    concern_fk INTEGER     NOT NULL REFERENCES concern ON DELETE CASCADE,
    lastname   VARCHAR(64) NOT NULL DEFAULT '',
    firstname  VARCHAR(64) NOT NULL DEFAULT '',
    externalId VARCHAR(40) NOT NULL DEFAULT '',
    sex        E_SEX,
    insurance  VARCHAR(40) NOT NULL DEFAULT '',
    birthday   DATE,
    naca       E_NACA,
    diagnosis  TEXT        NOT NULL DEFAULT '',
    info       TEXT        NOT NULL DEFAULT '',
    ertype     VARCHAR(40) NOT NULL DEFAULT '',
    done       BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS incident
(
    id          SERIAL PRIMARY KEY,
    concern_fk  INTEGER         NOT NULL REFERENCES concern ON DELETE CASCADE,
    state       E_INCIDENTSTATE NOT NULL,
    type        E_INCIDENTTYPE  NOT NULL,
    priority    BOOLEAN         NOT NULL,
    blue        BOOLEAN         NOT NULL,
    bo          JSONB,
    ao          JSONB,
    info        TEXT,
    caller      VARCHAR(100),
    casusNr     VARCHAR(100),
    patient_fk  INTEGER         REFERENCES patient ON DELETE SET NULL,
    section_fk  VARCHAR(30),
    created     TIMESTAMP       NOT NULL,
    arrival     TIMESTAMP,
    stateChange TIMESTAMP,
    ended       TIMESTAMP,
    FOREIGN KEY (concern_fk, section_fk) REFERENCES sections
);

CREATE TABLE IF NOT EXISTS log
(
    id          SERIAL PRIMARY KEY,
    concern_fk  INTEGER   NOT NULL REFERENCES concern ON DELETE NO ACTION,
    timestamp   TIMESTAMP NOT NULL,
    type        E_LOGTYPE,
    incident_fk INTEGER REFERENCES incident ON DELETE NO ACTION,
    unit_fk     INTEGER REFERENCES unit ON DELETE NO ACTION,
    patient_fk  INTEGER REFERENCES patient ON DELETE NO ACTION,
    taskstate   E_TASKSTATE,
    user_fk     INTEGER   NOT NULL REFERENCES users ON DELETE NO ACTION,
    text        TEXT,
    changes     JSON
);

CREATE TABLE IF NOT EXISTS task
(
    incident_fk       INTEGER     NOT NULL REFERENCES incident ON DELETE CASCADE,
    unit_fk           INTEGER     NOT NULL REFERENCES unit ON DELETE CASCADE,
    state             E_TASKSTATE NOT NULL,
    lastStateChangeAt TIMESTAMP DEFAULT (now() at time zone 'utc'),
    PRIMARY KEY (incident_fk, unit_fk)
);

CREATE TRIGGER set_update_timestamp_of_task
    BEFORE UPDATE
    ON task
    FOR EACH ROW
EXECUTE PROCEDURE update_lastStateChangeAt_column();

CREATE TABLE IF NOT EXISTS container
(
    id         SERIAL PRIMARY KEY,
    concern_fk INTEGER          NOT NULL REFERENCES concern ON DELETE CASCADE,
    parent     INTEGER REFERENCES container ON DELETE CASCADE,
    name       VARCHAR(60)      NOT NULL,
    ordering   DOUBLE PRECISION NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS container_fk_index ON container (concern_fk) WHERE parent IS NULL;

CREATE TABLE IF NOT EXISTS unit_in_container
(
    unit_fk      INTEGER PRIMARY KEY REFERENCES unit ON DELETE CASCADE,
    container_fk INTEGER          NOT NULL REFERENCES container ON DELETE CASCADE,
    ordering     DOUBLE PRECISION NOT NULL
);

COMMIT;
