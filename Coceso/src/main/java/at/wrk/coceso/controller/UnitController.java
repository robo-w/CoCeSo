
package at.wrk.coceso.controller;

import at.wrk.coceso.dao.TaskDao;
import at.wrk.coceso.dao.UnitDao;
import at.wrk.coceso.entities.*;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.service.TaskService;
import at.wrk.coceso.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/data/unit")
public class UnitController implements IEntityController<Unit> {

    @Autowired
    private UnitDao dao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private TaskService taskService;

    @Autowired
    private LogService log;

    @Override
    @RequestMapping(value = "getAll", produces = "application/json")
    @ResponseBody
    public List<Unit> getAll(@CookieValue(value = "active_case", defaultValue = "0") String case_id) {

        try {
            return dao.getAll(Integer.parseInt(case_id));
        } catch(NumberFormatException e) {
            Logger.warning("UnitController: getAll: "+e);
            return null;
        }
    }

    @Override
    @RequestMapping(value = "get", produces = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public Unit getByPost(@RequestParam(value = "id", required = true) int id) {

        return dao.getById(id);
    }

    @Override
    @RequestMapping(value = "get/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Unit getByGet(@PathVariable("id") int id) {

        return getByPost(id);
    }

    @Override
    @RequestMapping(value = "update", produces = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public String update(@RequestBody Unit unit, BindingResult result,
                         @CookieValue(value = "active_case", defaultValue = "0") String case_id, Principal principal)
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Person user = (Person) token.getPrincipal();

        if(result.hasErrors()) {
            return "{\"success\": false, description: \"Binding Error\"}";
        }

        unit.aCase = new Case();
        unit.aCase.id = Integer.parseInt(case_id);

        if(unit.aCase.id <= 0) {
            return "{\"success\": false, \"info\":\"No active Case. Cookies enabled?\"}";
        }

        if(unit.id < 1) {
            unit.id = 0;

            unit.id = dao.add(unit);

            log.logFull(user, "Unit created", Integer.parseInt(case_id), unit, null, true);
            return "{\"success\": " + (unit.id != -1) + ", \"new\": true}";
        }

        log.logFull(user, "Unit updated", Integer.parseInt(case_id), unit, null, true);
        return "{\"success\": " + dao.update(unit) + ", \"new\": false}";
    }

    @RequestMapping(value = "sendHome/{id}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Unit sendHome(@CookieValue(value="active_case", defaultValue = "0") String case_id,
                         @PathVariable("id") int unitId, Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        Person user = (Person) token.getPrincipal();

        List<Incident> list = taskDao.getAllByUnitIdWithType(unitId);

        for(Incident i : list) {
            if(i.type != IncidentType.HoldPosition && i.type != IncidentType.Standby)
                return null;
        }

        //TODO SendHome Feature
        return null;
    }

}
