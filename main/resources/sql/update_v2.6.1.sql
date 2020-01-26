START TRANSACTION;

CREATE OR REPLACE FUNCTION update_lastStateChangeAt_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.lastStateChangeAt = (now() at time zone 'utc');
    RETURN NEW;
END;
$$ language 'plpgsql';

ALTER TABLE task
    ADD COLUMN lastStateChangeAt TIMESTAMP DEFAULT (now() at time zone 'utc');

CREATE TRIGGER set_update_timestamp_of_task
    BEFORE UPDATE
    ON task
    FOR EACH ROW
EXECUTE PROCEDURE update_lastStateChangeAt_column();

COMMIT;
