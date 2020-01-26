package at.wrk.coceso.service.impl;

import at.wrk.coceso.entity.Incident;
import at.wrk.coceso.entity.Unit;
import at.wrk.coceso.entity.enums.Errors;
import at.wrk.coceso.entity.enums.IncidentType;
import at.wrk.coceso.entity.enums.LogEntryType;
import at.wrk.coceso.entity.enums.TaskState;
import at.wrk.coceso.entityevent.impl.NotifyList;
import at.wrk.coceso.exceptions.ErrorsException;
import at.wrk.coceso.repository.UnitRepository;
import at.wrk.coceso.service.IncidentService;
import at.wrk.coceso.service.LogService;
import at.wrk.coceso.service.UnitService;
import at.wrk.coceso.service.hooks.HookService;
import at.wrk.coceso.service.internal.TaskServiceInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
class TaskServiceImpl implements TaskServiceInternal {

    private final static Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private HookService hookService;

    @Autowired
    private LogService logService;

    @Override
    public void assignUnit(final int incidentId, final int unitId, final NotifyList notify) {
        Incident incident = incidentService.getById(incidentId);
        Unit unit = unitService.getById(unitId);
        if (incident.getUnits() != null && incident.getUnits().get(unit) == null) {
            changeState(incident, unit, TaskState.Assigned, notify);
        } else {
            LOG.info("Unit {} is already assigned to incident {}. Assigning the unit again is skipped.", unit, incident);
        }
    }

    @Override
    public synchronized void changeState(int incidentId, int unitId, TaskState state, NotifyList notify) {
        Incident incident = incidentService.getById(incidentId);
        Unit unit = unitService.getById(unitId);
        changeState(incident, unit, state, notify);
    }

    @Override
    public synchronized void changeState(final Incident incident, final Unit unit, final TaskState state, final NotifyList notify) {
        LOG.debug("Trying to update unit {} and incident {} to '{}'", unit, incident, state);

        if (incident == null || unit == null) {
            LOG.info("Unit or incident not found. unit: {}, incident: {}", unit, incident);
            throw new ErrorsException(Errors.EntityMissing);
        }

        if (!Objects.equals(incident.getConcern(), unit.getConcern())) {
            LOG.warn("Combination of unit {} and incident {} is in different concerns.", unit, incident);
            throw new ErrorsException(Errors.ConcernMismatch);
        }

        if (incident.getConcern().isClosed()) {
            LOG.info("Tried to change TaskState in closed concern. unit: {}, incident: {}", unit, incident);
            throw new ErrorsException(Errors.ConcernClosed);
        }

        if (incident.getType() == IncidentType.Treatment) {
            LOG.info("Tried to change TaskState for treatment incident. unit: {}, incident: {}", unit, incident);
            throw new ErrorsException(Errors.IncidentNotAllowed);
        }

        if (!incident.getType().isPossibleState(state)) {
            LOG.warn("TaskService.changeState(): New state not possible for unit {} and incident {}", unit, incident);
            throw new ErrorsException(Errors.ImpossibleTaskState);
        }

        Unit updatedUnit;
        if (incident.getUnits() == null || incident.getUnits().get(unit) == null) {
            LOG.debug("Unit {} was not assigned previously to incident {}. Assigning unit to incident with TaskState {}.", unit, incident, state);
            updatedUnit = assign(incident, unit, state, notify);
        } else {
            LOG.debug("Unit {} was already assigned to incident {}. TaskState is updated to {}.", unit, incident, state);
            LogEntryType logEntryType = state == TaskState.Detached ? LogEntryType.UNIT_DETACH : LogEntryType.TASKSTATE_CHANGED;
            updatedUnit = setState(incident, unit, state, notify, logEntryType);
        }

        if (updatedUnit != null) {
            unitRepository.saveAndFlush(updatedUnit);

            notify.addIncident(incident);
            notify.addUnit(updatedUnit.getId());
        }
    }

    @Override
    public void uncheckedChangeState(final Incident incident, final Unit unit, final TaskState state, final NotifyList notify) {
        if (state == TaskState.Detached) {
            unit.removeIncident(incident);
        } else {
            unit.addIncident(incident, state);
        }

        Unit updatedUnit = unitRepository.saveAndFlush(unit);
        notify.addIncident(incident);
        notify.addUnit(updatedUnit.getId());
    }

    private Unit assign(final Incident incident, final Unit unit, TaskState state, final NotifyList notify) {
        if (state == TaskState.Detached) {
            // We are detaching, but unit isn't assigned anyway: Nothing to do anymore
            return null;
        }

        // HoldPosition and Standby can't be assigned to multiple Units
        if (incident.getType().isSingleUnit() && incident.getUnits() != null && !incident.getUnits().isEmpty()) {
            LOG.debug("Tried to assign multiple unit {} to single unit incident {}, which has already units assigned: {}", unit, incident, incident.getUnits());
            throw new ErrorsException(Errors.MultipleUnits);
        }

        // Auto-Detach unit from all SingleUnit Incidents and Relocation
        unit.getIncidents()
                .keySet()
                .stream()
                .filter(this::isAutoDetachApplicable)
                .forEach(incidentOfUnit -> autoDetachUnitFromIncident(unit, notify, incidentOfUnit));

        setState(incident, unit, state, notify, LogEntryType.UNIT_ASSIGN);

        return unit;
    }

    private void autoDetachUnitFromIncident(final Unit unit, final NotifyList notify, final Incident incident) {
        LOG.debug("Auto-detach unit {} from incident {}", unit, incident);
        logService.logAuto(LogEntryType.UNIT_AUTO_DETACH, unit.getConcern(), unit, incident, TaskState.Detached);

        hookService.callTaskStateChanged(incident, unit, TaskState.Detached, notify);
        unit.removeIncident(incident);

        notify.addIncident(incident);
    }

    private boolean isAutoDetachApplicable(final Incident inc) {
        return inc.getType().isSingleUnit() || inc.getType() == IncidentType.Relocation;
    }

    private Unit setState(Incident incident, Unit unit, TaskState state, NotifyList notify, final LogEntryType logEntryType) {
        // Call additional hooks
        TaskState effectiveState = hookService.callTaskStateChanged(incident, unit, state, notify);

        logService.logAuto(
                logEntryType,
                incident.getConcern(),
                unit,
                incident,
                effectiveState);

        if (effectiveState == TaskState.Detached) {
            LOG.debug("Detaching unit {} from incident {}", unit, incident);
            unit.removeIncident(incident);
        } else {
            // Only save if not already done by assignUnit
            LOG.debug("Updating TaskState for unit {} in incident {} to {}", unit, incident, effectiveState);
            unit.addIncident(incident, effectiveState);
        }

        return unit;
    }

}
