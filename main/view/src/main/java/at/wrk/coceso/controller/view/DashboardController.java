package at.wrk.coceso.controller.view;

import at.wrk.coceso.entity.Concern;
import at.wrk.coceso.entity.Incident;
import at.wrk.coceso.entity.Patient;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.exceptions.ConcernException;
import at.wrk.coceso.exceptions.NotFoundException;
import at.wrk.coceso.service.ConcernService;
import at.wrk.coceso.service.IncidentService;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.service.PatientService;
import at.wrk.coceso.service.UnitService;
import at.wrk.coceso.utils.ActiveConcern;
import at.wrk.coceso.utils.Initializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

@Controller
@PreAuthorize("@auth.hasAccessLevel('Dashboard')")
public class DashboardController {

  private static final int MAXIMUM_LOG_LINES_TO_LOAD = 500;

  @Autowired
  private ConcernService concernService;

  @Autowired
  private LogService logService;

  @Autowired
  private UnitService unitService;

  @Autowired
  private IncidentService incidentService;

  @Autowired
  private PatientService patientService;

  @Transactional
  @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
  public String showDashboard(ModelMap map,
      @RequestParam(value = "view", defaultValue = "") String view,
      @RequestParam(value = "uid", required = false) Integer uid,
      @RequestParam(value = "iid", required = false) Integer iid,
      @RequestParam(value = "pid", required = false) Integer pid,
      @RequestParam(value = "active", defaultValue = "0") boolean active,
      @RequestParam(value = "concern", required = false) Integer concern_id,
      @ActiveConcern(required = false) Concern concern) throws NotFoundException, ConcernException {
    map.addAttribute("concerns", concernService.getAllOpen());

    if (iid != null && uid != null) {
      crossDetail(map, iid, uid);
    } else if (iid != null) {
      incidentDetail(map, iid);
    } else if (uid != null) {
      unitDetail(map, uid);
    } else if (pid != null) {
      patientDetail(map, pid);
    } else {
      if (concern_id != null) {
        concern = concernService.getById(concern_id);
      }
      if (concern == null) {
        throw new ConcernException("Concern does not exist.");
      }
      map.addAttribute("concern", concern.getId());

      switch (view) {
        case "unit":
          this.unitList(map, concern);
          break;
        case "incident":
          this.incidentList(map, concern, active);
          break;
        case "patient":
          this.patientList(map, concern);
          break;
        default:
          this.logList(map, concern);
          break;
      }
    }

    return "dashboard";
  }

  private void logList(ModelMap map, Concern concern) {
    map.addAttribute("template", "log_table");
    map.addAttribute("log_menu", "active");
    map.addAttribute("logs", logService.getLast(concern, MAXIMUM_LOG_LINES_TO_LOAD));
  }

  private void incidentList(ModelMap map, Concern concern, boolean active) {
    map.addAttribute("template", "incident_list");
    map.addAttribute("incident_menu", "active");
    map.addAttribute("incidents", active ? incidentService.getAllActive(concern) : incidentService.getAllSorted(concern));
  }

  private void unitList(ModelMap map, Concern concern) {
    map.addAttribute("template", "unit_list");
    map.addAttribute("unit_menu", "active");
    map.addAttribute("units", Initializer.init(unitService.getAllSorted(concern), Unit::getIncidents, Unit::getIncidentStateChangedAtMap));
  }

  private void patientList(ModelMap map, Concern concern) {
    map.addAttribute("template", "patient_list");
    map.addAttribute("patient_menu", "active");
    map.addAttribute("patients", Initializer.init(patientService.getAllSorted(concern), Patient::getIncidents));
  }

  private void crossDetail(ModelMap map, int incident_id, int unit_id) throws NotFoundException {
    Incident incident = incidentService.getById(incident_id);
    Unit unit = unitService.getById(unit_id);
    if (unit == null || incident == null || !Objects.equals(incident.getConcern(), unit.getConcern())) {
      throw new NotFoundException();
    }

    map.addAttribute("template", "cross_detail");
    map.addAttribute("concern", incident.getConcern().getId());
    map.addAttribute("incident", incident);
    map.addAttribute("unit", unit);
    map.addAttribute("logs", logService.getByIncidentAndUnit(incident, unit));
  }

  private void incidentDetail(ModelMap map, int incident_id) throws NotFoundException {
    Incident incident = incidentService.getById(incident_id);
    if (incident == null) {
      throw new NotFoundException();
    }

    map.addAttribute("template", "incident_detail");
    map.addAttribute("incident_menu", "active");
    map.addAttribute("concern", incident.getConcern().getId());
    map.addAttribute("incident", incident);
    map.addAttribute("units", unitService.getRelated(incident));
    map.addAttribute("logs", logService.getByIncident(incident));
  }

  private void unitDetail(ModelMap map, int unit_id) throws NotFoundException {
    Unit unit = unitService.getById(unit_id);
    if (unit == null) {
      throw new NotFoundException();
    }

    map.addAttribute("template", "unit_detail");
    map.addAttribute("unit_menu", "active");
    map.addAttribute("concern", unit.getConcern().getId());
    map.addAttribute("unit", unit);
    map.addAttribute("incidents", incidentService.getRelated(unit));
    map.addAttribute("logs", logService.getByUnit(unit));
  }

  private void patientDetail(final ModelMap map, final int patientId) throws NotFoundException {
    Patient patient = patientService.getById(patientId);
    if (patient == null) {
      throw new NotFoundException();
    }

    Initializer.init(patient, Patient::getIncidents);

    map.addAttribute("template", "patient_detail");
    map.addAttribute("patient_menu", "active");
    map.addAttribute("concern", patient.getConcern().getId());
    map.addAttribute("patient", patient);
    map.addAttribute("logs", logService.getByPatient(patient));
  }

}
