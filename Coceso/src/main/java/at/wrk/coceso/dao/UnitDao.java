package at.wrk.coceso.dao;


import at.wrk.coceso.dao.mapper.UnitMapper;
import at.wrk.coceso.entities.Person;
import at.wrk.coceso.entities.Unit;
import at.wrk.coceso.entities.UnitState;
import at.wrk.coceso.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UnitDao extends CocesoDao<Unit> {

    @Autowired
    UnitMapper unitMapper;

    @Autowired CrewDao crewDao;

    @Autowired
    public UnitDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Unit getById(int id) {
        if(id < 1) {
            Logger.error("UnitDao.getById(int): Invalid ID: " + id);
            return null;
        }

        String q = "select * from units where id = ?";
        Unit unit;

        try {
            unit = jdbc.queryForObject(q, new Integer[] {id}, unitMapper);
        }
        catch(IncorrectResultSizeDataAccessException e) {
            Logger.error("UnitDao.getById(int): requested id: "+id
                    +"; IncorrectResultSizeDataAccessException: "+e.getMessage());
            return null;
        }
        catch(DataAccessException dae) {
            Logger.error("UnitDao.getById(int): requested id: "+id+"; DataAccessException: "+dae.getMessage());
            return null;
        }

        return unit;
    }

    @Override
    public List<Unit> getAll(int case_id) {
        if(case_id < 1) {
            Logger.warning("UnitDao.getAll: invalid case_id: "+case_id);
            return null;
        }
        String q = "SELECT * FROM units WHERE aCase = ? ORDER BY id ASC";

        try {
            return jdbc.query(q, new Object[] {case_id}, unitMapper);
        }
        catch(DataAccessException dae) {
            Logger.error("UnitDao.getAll: DataAccessException: "+dae.getMessage());
            return null;
        }
    }

    /**
     * Update Unit. Only Values state, info, position, home are changeable. All others are LOCKED!
     * To change these, use updateFull(Unit).
     *
     * @param unit Unit to write to DB
     * @return Success of Operation
     */
    @Override
    public boolean update(Unit unit) {
        if(unit == null) {
            Logger.error("UnitDao.update(Unit): unit is NULL");
            return false;
        }
        if(unit.id <= 0) {
            Logger.error("UnitDao.update(Unit): Invalid id: " + unit.id + ", call: "+unit.call);
            return false;
        }

        final String pre_q = "update units set";
        final String suf_q = " where id = " + unit.id;

        boolean first = true;
        boolean info_given = false;

        String q = pre_q;
        if(unit.state != null) {
            q += " state = '" + unit.state.name() + "'";
            first = false;
        }
        if(unit.info != null) {
            if(!first) {
                q += ",";
            }
            q += " info = '?'";
            info_given = true;
            first = false;
        }
        if(unit.position != null && unit.position.id > 0) {
            if(!first) {
                q += ",";
            }
            q += " position = " + unit.position.id;
            first = false;
        }
        if(unit.home != null && unit.home.id  > 0) {
            if(!first) {
                q += ",";
            }
            q += " home = " + unit.home.id;
            // first = false;
        }
        q += suf_q;
        try {
            if(info_given) {
                jdbc.update(q, unit.info);
            }
            else {
                jdbc.update(q);
            }
        }
        catch(DataAccessException dae) {
            Logger.error("UnitDao.update(Unit): DataAccessException: " + dae.getMessage());
            return false;
        }
        return true;
    }

    public boolean updateFull(Unit unit) {
        if(unit == null) {
            Logger.error("UnitDao.updateFull(Unit): unit is NULL");
            return false;
        }
        if(unit.id <= 0) {
            Logger.error("UnitDao.updateFull(Unit): Invalid id: " + unit.id + ", call: "+unit.call);
            return false;
        }

        String q = "UPDATE units SET state = ?, call = ?, ani = ?, withdoc = ?, " +
                "portable = ?, transportvehicle = ?, info = ?, position = ?, home = ? WHERE id = ?";

        try {
            jdbc.update(q, unit.state == null ? UnitState.AD.name() : unit.state.name(), unit.call, unit.ani, unit.withDoc, unit.portable, unit.transportVehicle,
                    unit.info, unit.position == null ? null : unit.position.id,
                    unit.home == null ? null : unit.home.id, unit.id);
        }
        catch(DataAccessException dae) {
            Logger.error("UnitDao.updateFull(Unit): DataAccessException: " + dae.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean add(Unit unit) {
        if(unit == null) {
            Logger.error("UnitDao.add(Unit): unit is NULL");
            return false;
        }
        if(unit.aCase == null || unit.aCase.id <= 0) {
            Logger.error("UnitDao.add(Unit): No aCase given. call: " + unit.call);
            return false;
        }

        unit.prepareNotNull();

        try {
            String q = "insert into units (aCase, state, call, ani, withDoc," +
                    " portable, transportVehicle, info, position, home) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbc.update(q, unit.aCase.id, unit.state == null ? UnitState.AD.name() : unit.state.name(),
                    unit.call, unit.ani, unit.withDoc,
                    unit.portable, unit.transportVehicle, unit.info,
                    unit.position == null ? null : unit.position.id,
                    unit.home == null ? null : unit.home.id);

            if(unit.crew != null) {
                for(Person p : unit.crew) {
                    crewDao.add(unit, p);
                }
            }
            return true;
        }
        catch (DataAccessException dae) {
            Logger.error("UnitDao.add(Unit): call: "+unit.call+"; DataAccessException: "+dae.getMessage());
            return false;
        }

    }

    @Override
    public boolean remove(Unit unit) {
        if(unit == null) {
            Logger.error("UnitDao.remove(Unit): unit is NULL");
            return false;
        }
        if(unit.id <= 0) {
            Logger.error("UnitDao.remove(Unit): invalid id: " + unit.id + ", call: " + unit.call);
            return false;
        }
        String q = "delete from units where id = ?";
        try {
            jdbc.update(q, unit.id);
        }
        catch (DataAccessException dae) {
            Logger.error("UnitDao.remove(Unit): id: "+unit.id+"; DataAccessException: "+dae.getMessage());
            return false;
        }

        return true;
    }
}
