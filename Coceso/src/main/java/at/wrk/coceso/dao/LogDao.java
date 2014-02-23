package at.wrk.coceso.dao;

import at.wrk.coceso.dao.mapper.LogMapper;
import at.wrk.coceso.entity.LogEntry;
import at.wrk.coceso.entity.enums.LogEntryType;
import at.wrk.coceso.entity.enums.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class LogDao extends CocesoDao<LogEntry> {

    // REFERENCES NOT RESOLVED BY THIS MAPPER
    @Autowired
    LogMapper logMapper;

    @Autowired
    public LogDao(DataSource dataSource) {
        super(dataSource);
    }

    private static final String getPrefix = "SELECT u.id as uid, u.call, l.*, p.id AS pid, p.sur_name, " +
            "p.given_name, p.dnr, p.contact, o.username FROM " +
            "log l " +
            "LEFT OUTER JOIN operator o ON l.operator_fk = o.id " +
            "LEFT OUTER JOIN person p ON l.operator_fk = p.id " +
            "LEFT OUTER JOIN unit u ON l.unit_fk = u.id ";

    private static final String sortSuffix = " ORDER BY timestamp DESC";

    @Override
    public LogEntry getById(int id) {
        if(id <= 0) return null;

        String q =  getPrefix + "WHERE l.id = ?";

        try {
            return jdbc.queryForObject(q, new Object[] {id}, logMapper);
        } catch(DataAccessException e) {
            return null;
        }
    }

    public List<LogEntry> getByUnitId(int id) {
        if(id <= 0) return null;

        String q = getPrefix + "WHERE l.unit_fk = ?"+sortSuffix;

        return jdbc.query(q, new Object[]{id}, logMapper);
    }

    public List<LogEntry> getByIncidentId(int id) {
        if(id <= 0) return null;

        String q = getPrefix + "WHERE l.incident_fk = ?" + sortSuffix;

        return jdbc.query(q, new Object[] {id}, logMapper);
    }


    @Override
    public List<LogEntry> getAll(int id) {
        if(id <= 0) return null;

        String q = getPrefix + "WHERE l.concern_fk = ?" + sortSuffix;

        return jdbc.query(q, new Object[] {id}, logMapper);
    }

    @Override
    public boolean update(LogEntry logEntry) {
        throw new UnsupportedOperationException();
    }

    protected boolean updateForRemoval(int unitId) {
        String q = "UPDATE log SET unit_fk = NULL, text = 'Unit created - REMOVED' WHERE type = 'UNIT_CREATE' AND unit_fk = ?";
        jdbc.update(q, unitId);
        return true;
    }

    @Override
    public int add(LogEntry l) {
        String q = "INSERT INTO log (timestamp, concern_fk, unit_fk, incident_fk, taskstate, autogenerated, " +
                "operator_fk, text, json, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        TaskState state = null;
        // Read TaskState if Unit and Incident exist
        if(l.getIncident() != null && l.getUnit() != null && l.getIncident().getUnits() != null) {
            state = l.getIncident().getUnits().get(l.getUnit().getId());
        }

        jdbc.update(q,
                new Timestamp(System.currentTimeMillis()),
                l.getConcern().getId(),
                l.getUnit() == null ? null : l.getUnit().getId(),
                l.getIncident() == null ? null : l.getIncident().getId(),
                state == null ? null : state.name(),
                l.isAutoGenerated(),
                l.getUser().getId(),
                l.getText(),
                l.getJson(),
                l.getType() == null ? null : l.getType().name());

        return 0;
    }

    public void add(int case_id, int unit_id, int incident_id, boolean auto, int uzer, LogEntryType type) {
        String q = "INSERT INTO log (timestamp, concern_fk, unit_fk, incident_fk, autogenerated, " +
                "operator_fk, text, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";



        jdbc.update(q,
                new Timestamp(System.currentTimeMillis()),
                case_id,
                unit_id > 0 ? unit_id : null,
                incident_id > 0 ? incident_id : null,
                auto,
                uzer,
                type == null ? "" : type.getMessage(),
                type == null ? null : type.name());
    }

    @Override
    public boolean remove(LogEntry logEntry) {
        throw new UnsupportedOperationException();
    }

    public List<LogEntry> getLast(int case_id, int count) {
        String q = getPrefix + "WHERE l.concern_fk = ? " + sortSuffix + "  LIMIT ?";

        return jdbc.query(q, new Object[] {case_id, count}, logMapper);
    }

    public List<LogEntry> getCustom(int concernId) {
        if(concernId <= 0) return null;

        String q = getPrefix + "WHERE type = 'CUSTOM' AND l.concern_fk = ?" + sortSuffix;

        return jdbc.query(q, new Object[] {concernId}, logMapper);
    }
}
