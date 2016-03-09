START TRANSACTION;

DROP TABLE IF EXISTS selcall CASCADE;
DROP TABLE IF EXISTS point CASCADE;
DROP TABLE IF EXISTS concern CASCADE;
DROP TABLE IF EXISTS sections CASCADE;
DROP TABLE IF EXISTS unit CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS crew CASCADE;
DROP TABLE IF EXISTS medinfo CASCADE;
DROP TABLE IF EXISTS patient CASCADE;
DROP TABLE IF EXISTS incident CASCADE;
DROP TABLE IF EXISTS log CASCADE;
DROP TABLE IF EXISTS task CASCADE;
DROP TABLE IF EXISTS container CASCADE;
DROP TABLE IF EXISTS unit_in_container CASCADE;

COMMIT;

START TRANSACTION;

DROP TYPE E_DIRECTION;
DROP TYPE E_UNITSTATE;
DROP TYPE E_UNITTYPE;
DROP TYPE E_ROLE;
DROP TYPE E_INCIDENTSTATE;
DROP TYPE E_INCIDENTTYPE;
DROP TYPE E_LOGTYPE;
DROP TYPE E_TASKSTATE;
DROP TYPE E_NACA;
DROP TYPE E_SEX;

COMMIT;
