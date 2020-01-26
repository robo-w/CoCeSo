package at.wrk.coceso.service.internal;

import at.wrk.coceso.entity.Concern;
import at.wrk.coceso.entity.Incident;
import at.wrk.coceso.entity.Patient;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.entity.point.Point;
import at.wrk.coceso.entityevent.impl.NotifyList;
import at.wrk.coceso.service.IncidentService;

public interface IncidentServiceInternal extends IncidentService {

  Incident update(Incident incident, Concern concern, NotifyList notify);

  Incident createHoldPosition(Point position, Unit unit, TaskState state, NotifyList notify);

  void endTreatments(Patient patient, NotifyList notify);

  Incident createTreatment(Patient patient, Unit group, NotifyList notify);

  void assignPatient(int incidentId, int patientId, NotifyList notify);

  void assignPatient(Incident incident, Patient patient, NotifyList notify);
}
